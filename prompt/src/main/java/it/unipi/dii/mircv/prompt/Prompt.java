package it.unipi.dii.mircv.prompt;

import it.unipi.dii.mircv.index.preprocessing.Preprocessing;
import it.unipi.dii.mircv.index.structures.Document;
import it.unipi.dii.mircv.index.structures.Lexicon;
import it.unipi.dii.mircv.index.utility.Logs;
import it.unipi.dii.mircv.prompt.dynamicPruning.DynamicPruning;
import it.unipi.dii.mircv.prompt.query.Query;
import it.unipi.dii.mircv.prompt.query.Searcher;
import it.unipi.dii.mircv.prompt.trec_eval.EvaluatorMultiThread;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;


/**
 * The Prompt class implements the main program for querying the inverted index. It is a command-line interface that allows
 * the user to perform queries on the index. Moreover, it allows the user to evaluate the search engine using the trec_eval tool.
 */
public class Prompt {
    private static final String DOCUMENTS_PATH = "data/index/documents.bin";
    private static final String LEXICON_PATH = "data/index/lexicon.bin";

    /**
     * Executes the main program to run the search engine.
     *
     * @param args The command-line arguments.
     *             The list of accepted arguments is:
     *             -scoring <value>: Specify the scoring function [BM25, TFIDF]. Default: TFIDF.
     *             -topK <value>: Specify the number of documents to return. Default: 10.
     *             -dynamic: Enable dynamic pruning using MAXSCORE. Default: disabled.
     *             -conjunctive: Enable conjunctive mode. Default: disjunctive.
     *             -stemmer: Enable Porter Stemming in query preprocessing NOTE: MUST MATCH THE OPTION USED IN index.java. Default: disabled.
     */
    public static void main(String[] args) throws InterruptedException {
        // create folder logs if not exists
        File logsFolder = new File("data/logs");
        if (!logsFolder.exists())
            logsFolder.mkdir();
        // create folder trec_eval if not exists
        File trec_evalFolder = new File("data/trec_eval");
        if (!trec_evalFolder.exists())
            trec_evalFolder.mkdir();

        int[] options = processOptions(args); // process parameters from command line

        String scoringFunction = options[0] == 1 ? "BM25" : "TFIDF";
        boolean dynamicPruningOption = options[1] == 1 ? true : false;
        String mode = options[2] == 1 ? "conjunctive" : "disjunctive";
        int K = options[3];

        String COLLECTION_PATH = null;
        boolean compressed_reading = false;
        boolean porterStemmerOption = false;

        Logs log = new Logs();
        long start, end;

        // check if index exists
        if (!checkIndexing()) {
            System.out.println("Indexing not found, please run Index first");
            System.out.println("Bye!");
            System.exit(0);
        }

        //read from configuration file
        try (BufferedReader br = new BufferedReader(new FileReader("data/configuration.txt"))) {
            COLLECTION_PATH = br.readLine();
            compressed_reading = Boolean.parseBoolean(br.readLine());
            porterStemmerOption = Boolean.parseBoolean(br.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        }

        printOptions(scoringFunction, dynamicPruningOption, mode, porterStemmerOption, K, compressed_reading);

        // load main structure in memory
        System.out.println("Loading index ...");
        // load lexicon
        Lexicon lexicon = new Lexicon();
        start = System.currentTimeMillis();
        lexicon.readLexiconFromDisk(-1, LEXICON_PATH);
        end = System.currentTimeMillis();
        log.addLog("load_lexicon", start, end);
        // load documents
        start = System.currentTimeMillis();
        ArrayList<Document> documents = Document.readDocumentsFromDisk(-1, DOCUMENTS_PATH);
        end = System.currentTimeMillis();
        log.addLog("load_documents", start, end);

        Scanner scanner = new Scanner(System.in);
        Searcher searcher = new Searcher(lexicon, documents);

        Preprocessing preprocessing = new Preprocessing();
        Query query = new Query(porterStemmerOption, preprocessing);

        while (true) {
            System.out.println("--------------------------------------------------");
            System.out.println("Welcome to the search engine!");
            System.out.println("MENU: \n - insert 1 to search \n - insert 2 to evaluate searchEngine \n - insert 3 calculate upper bounds for dynamic pruning \n - insert 10 to exit");
            int userInput = 0;
            try {
                userInput = scanner.nextInt(); // try to read an integer from the console
            } catch (InputMismatchException e) {
                System.out.println("Wrong input");
                scanner.nextLine(); // to consume the \n character left by nextInt()
                continue;
            }
            scanner.nextLine(); // to consume the \n character left by nextInt()
            if (userInput == 1) {
                // first option: search
                System.out.println("Insert your query ...");
                String queryInput = scanner.nextLine();
                query.setQuery(queryInput);
                //Query query = new Query(queryInput, porterStemmerOption, preprocessing);
                ArrayList<String> queryTerms = query.getQueryTerms();
                if (dynamicPruningOption) {
                    start = System.currentTimeMillis();
                    searcher.maxScore(queryTerms, K, mode, scoringFunction);
                    end = System.currentTimeMillis();
                } else {
                    start = System.currentTimeMillis();
                    searcher.DAAT(queryTerms, K, mode, scoringFunction);
                    end = System.currentTimeMillis();
                }
                searcher.printResults(end - start);
                log.addLog("query", start, end);
            } else if (userInput == 2) {
                // second option: evaluate search engine with trec_eval
                EvaluatorMultiThread evaluatorMT = new EvaluatorMultiThread(lexicon, documents, K, mode, scoringFunction, porterStemmerOption, dynamicPruningOption);
                evaluatorMT.execute();
//                Evaluator evaluator = new Evaluator(searcher, lexicon, documents, K, mode, scoringFunction, porterStemmerOption, dynamicPruning);
//                evaluator.execute();
            } else if (userInput == 3) {
                // third option: calculate TUBs and DUBs for dynamic pruning
                DynamicPruning dynamicPruning = new DynamicPruning(lexicon, documents, COLLECTION_PATH, compressed_reading, porterStemmerOption);
                dynamicPruning.execute();
                searcher = new Searcher(lexicon, documents);
            } else if (userInput == 10) {
                System.out.println("Bye!");
                scanner.close();
                break;
            } else {
                System.out.println("Wrong input");
            }
        }
    }

    /**
     * Checks if the index exists.
     *
     * @return True if the index exists, false otherwise.
     */
    private static boolean checkIndexing() {
        return (new File(DOCUMENTS_PATH).exists() && new File(LEXICON_PATH).exists() && new File("data/index/index.bin").exists() && new File("data/index/documentinfo.bin").exists() && new File("data/index/blockdescriptor.bin").exists());
    }

    /**
     * Processes the command-line arguments.
     *
     * @param args The command-line arguments.
     * @return An array of integers indicating the options selected.
     */
    private static int[] processOptions(String[] args) {
        int scoringFunctionOption = 0;
        int dynamicPruning = 0;
        int conjunctive = 0;
        int K = 10;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-scoring")) { // Scoring function
                if (i + 1 < args.length) {
                    String scoring = args[i + 1];
                    if (scoring.equals("TFIDF"))
                        scoringFunctionOption = 0;
                    else if (scoring.equals("BM25"))
                        scoringFunctionOption = 1;
                    else {
                        System.err.println("The -scoring option requires a value of either TFIDF or BM25.");
                        System.exit(1);
                    }
                    i++;
                } else {
                    System.err.println("The -scoring option requires a value.");
                    System.exit(1);
                }
            } else if (args[i].equals("-topK")) { // Top K results from the query
                if (i + 1 < args.length) {
                    K = Integer.parseInt(args[i + 1]);
                    if (K < 1) {
                        System.err.println("The number must be greater than 0.");
                        System.exit(1);
                    }
                    i++;
                } else {
                    System.err.println("The -topK option requires a value.");
                    System.exit(1);
                }
            } else if (args[i].equals("-dynamic")) { // Dynamic pruning
                dynamicPruning = 1;
            } else if (args[i].equals("-conjunctive")) { // Conjunctive mode
                conjunctive = 1;
//            } else if (args[i].equals("-help")) {
//                // If the -help option is specified, display a help message
//                System.out.println("Program usage:");
//                System.out.println("-scoring <value>: Specify the scoring function [BM25, TFIDF]. Default: TFIDF.");
//                System.out.println("-topK <value>: Specify the number of documents to return. Default: 10.");
//                System.out.println("-dynamic: Enable dynamic pruning using MAXSCORE. Default: disabled.");
//                System.out.println("-conjunctive: Enable conjunctive mode. Default: disjunctive.");
//                System.out.println("-help: Show this help message.");
//                System.exit(0);
            } else {
                System.err.println("Unrecognized option: " + args[i]);
                System.exit(1);
            }
        }

        return new int[]{scoringFunctionOption, dynamicPruning, conjunctive, K};
    }

    /**
     * Prints the options selected by the user.
     *
     * @param scoringFunction The scoring function selected by the user.
     * @param dynamicPruning  The dynamic pruning option selected by the user.
     * @param mode            The mode selected by the user.
     * @param K               The number of documents to return.
     */
    private static void printOptions(String scoringFunction, boolean dynamicPruning, String mode, boolean porterStemmerOption, int K, boolean compressed_reading) {
        System.out.println("Options:");
        System.out.println("------------------------------------");
        System.out.println("|   scoring     |   " + scoringFunction + "          |");
        System.out.println("|   dynamic     |   " + dynamicPruning + "          |");
        System.out.println("|   mode        |   " + mode + "    |");
        System.out.println("|   stemmer     |   " + porterStemmerOption + "          |");
        System.out.println("|   topK        |   " + K + "             |");
        System.out.println("|   compressed  |   " + compressed_reading + "          |");
        System.out.println("------------------------------------");

    }
}

