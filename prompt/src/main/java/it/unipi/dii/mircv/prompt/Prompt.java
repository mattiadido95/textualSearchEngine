package it.unipi.dii.mircv.prompt;

import it.unipi.dii.mircv.index.structures.Document;
import it.unipi.dii.mircv.index.structures.Lexicon;
import it.unipi.dii.mircv.index.utility.Logs;
import it.unipi.dii.mircv.prompt.dynamicPruning.DynamicPruning;
import it.unipi.dii.mircv.prompt.query.Query;
import it.unipi.dii.mircv.prompt.query.Searcher;
import it.unipi.dii.mircv.prompt.trec_eval.EvaluatorMultiThread;

import java.io.File;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Prompt {
    private static int n_results = 10; // number of documents to return for a query
    private static int n_results_eval = 15; // number of documents to return for evaluation
    private static final String DOCUMENTS_PATH = "data/index/documents.bin";
    private static final String LEXICON_PATH = "data/index/lexicon.bin";

    public static void main(String[] args) throws InterruptedException {

        int[] options = processOptions(args);

        int scoringFunctionOption = options[0];
        int dynamicPruning = options[1];
        int conjunctive = options[2];
        int porterStemmer = options[3];

        processOptions(args);

        System.out.println("Scoring Function Option: " + scoringFunctionOption);
        System.out.println("Dynamic Pruning: " + dynamicPruning);
        System.out.println("Conjunctive: " + conjunctive);
        System.out.println("Porter: " + porterStemmer);

        Logs log = new Logs();
        long start, end;

        System.out.println("Loading index ...");
        // load main structure in memory
        Lexicon lexicon = new Lexicon();
        start = System.currentTimeMillis();
        lexicon.readLexiconFromDisk(-1, LEXICON_PATH);
        end = System.currentTimeMillis();
        log.addLog("load_lexicon", start, end);
        start = System.currentTimeMillis();
        ArrayList<Document> documents = Document.readDocumentsFromDisk(-1, DOCUMENTS_PATH);
        end = System.currentTimeMillis();
        log.addLog("load_documents", start, end);

        Scanner scanner = new Scanner(System.in);
        Searcher searcher = new Searcher(lexicon, documents);

        while (true) {
            System.out.println("--------------------------------------------------");
            System.out.println("Welcome to the search engine!");
            System.out.println("MENU: \n - insert 1 to search \n - insert 2 to evaluate searchEngine \n - insert 3 to run dynamic pruning \n - insert 10 to exit");
            int userInput = 0;
            try {
                userInput = scanner.nextInt(); // Tentativo di lettura dell'intero
            } catch (InputMismatchException e) {
                System.out.println("Wrong input");
                scanner.nextLine(); // to consume the \n character left by nextInt()
                continue;
            }
            scanner.nextLine(); // to consume the \n character left by nextInt()
            if (userInput == 1) {
                System.out.println("Insert your query ...");
                String queryInput = scanner.nextLine();

                Query query = new Query(queryInput);
                ArrayList<String> queryTerms = query.getQueryTerms();

                System.out.println("DAAT");
                start = System.currentTimeMillis();
                searcher.DAAT(queryTerms, n_results, "disjunctive", "BM25");
                end = System.currentTimeMillis();
                searcher.printResults(end - start);
                log.addLog("query", start, end);

                System.out.println("MaxScore");
                start = System.currentTimeMillis();
                searcher.maxScore(queryTerms, n_results, "disjunctive", "BM25");
                end = System.currentTimeMillis();
                searcher.printResults(end - start);
                log.addLog("query", start, end);

            } else if (userInput == 2) {
                EvaluatorMultiThread evaluatorMT = new EvaluatorMultiThread(searcher, lexicon, documents, n_results_eval, "disjunctive");
                evaluatorMT.execute();
//                Evaluator evaluator = new Evaluator(searcher, lexicon, documents, n_results_eval, "disjunctive");
//                evaluator.execute();
            } else if (userInput == 3) {
                // call to dynamic pruning process
                DynamicPruning dinamicPruning = new DynamicPruning(lexicon, documents);
                dinamicPruning.TUB_processing("BM25");
                dinamicPruning.TUB_processing("TFIDF");
                lexicon = new Lexicon();
                lexicon.readLexiconFromDisk(-1, LEXICON_PATH);
            } else if (userInput == 10) {
                System.out.println("Bye!");
                scanner.close();
                break;
            } else {
                System.out.println("Wrong input, please insert 1 or 2");
            }
        }
    }

    private static int[] processOptions(String[] args) {
        int scoringFunctionOption = 0;
        int dynamicPruning = 0;
        int conjunctive = 0;
        int porterStemmer = 0;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-scoring")) { //scoring function
                if (i + 1 < args.length) {
                    String scoring = args[i + 1];
                    if (scoring.equals("TFIDF"))
                        scoringFunctionOption = 0;
                    else if (scoring.equals("BM25"))
                        scoringFunctionOption = 1;
                    else {
                        System.err.println("L'opzione -sf richiede un valore tra TFIDF e BM25.");
                        System.exit(1);
                    }
                    i++;
                } else {
                    System.err.println("L'opzione -scoring richiede un valore.");
                    System.exit(1);
                }
            } else if (args[i].equals("-dynamic")) { // dynamic pruning
                dynamicPruning = 1;
            } else if (args[i].equals("-conjunctive")) { // conjunctive
                conjunctive = 1;
            } else if (args[i].equals("-stemmer")) { // porterStemmer
                porterStemmer = 1;
            } else if (args[i].equals("-help")) {
                // Se viene specificata l'opzione -help, mostra un messaggio di aiuto
                System.out.println("Uso del programma:");
                System.out.println("-scoring <valore>: Specifica la scoring function [BM25, TFIDF].");
                System.out.println("-dynamic: Abilita il pruning dinamico usando il MAXSCORE.");
                System.out.println("-disjunctive: Abilita la modalità disgiuntiva.");
                System.out.println("-stemmer: Abilita il PorterStemming nel preprocessing della query\n NB:DEVE ESSERE UGUALE ALL'OPZIONE USATA IN index.java.");
                System.out.println("-help: Mostra questo messaggio di aiuto.");
                System.exit(0);
            } else {
                System.err.println("Opzione non riconosciuta: " + args[i]);
                System.exit(1);
            }
        }

        return new int[]{scoringFunctionOption, dynamicPruning, conjunctive, porterStemmer};
    }
}

