package it.unipi.dii.mircv.prompt;

import it.unipi.dii.mircv.index.structures.Document;
import it.unipi.dii.mircv.index.structures.Lexicon;
import it.unipi.dii.mircv.index.utility.Logs;
import it.unipi.dii.mircv.prompt.query.Query;
import it.unipi.dii.mircv.prompt.query.Searcher;

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Prompt {
    private static int n_results = 10; // number of documents to return for a query

    public static void main(String[] args) {

        Logs log = new Logs();
        long start, end;

        // load main structure in memory
        Lexicon lexicon = new Lexicon();
        start = System.currentTimeMillis();
        lexicon.readLexiconFromDisk(-1);
        end = System.currentTimeMillis();
        log.addLog("load_lexicon", start, end);

        start = System.currentTimeMillis();
        ArrayList<Document> documents = Document.readDocumentsFromDisk(-1);
        end = System.currentTimeMillis();
        log.addLog("load_documents", start, end);

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("--------------------------------------------------");
            System.out.println("Welcome to the search engine!");
            System.out.println("MENU: \n - insert 1 to search \n - insert 2 to exit");
            int userInput = 0;
            try {
                userInput = scanner.nextInt(); // Tentativo di lettura dell'intero
            } catch (InputMismatchException e) {
                System.out.println("Wrong input, please insert 1 or 2");
                scanner.nextLine(); // to consume the \n character left by nextInt()
                continue;
            }
            scanner.nextLine(); // to consume the \n character left by nextInt()
            if (userInput == 1) {
                System.out.println("Insert your query ...");
                String queryInput = scanner.nextLine();

                Query query = new Query(queryInput);
                ArrayList<String> queryTerms = query.getQueryTerms();

                Searcher searcher = new Searcher();

                System.out.println("disjunctive");
                start = System.currentTimeMillis();
                searcher.DAAT_disk(queryTerms, lexicon, documents, n_results, "disjunctive");
                end = System.currentTimeMillis();
                searcher.printResults(end - start);

                System.out.println("conjunctive");
                start = System.currentTimeMillis();
                searcher.DAAT_disk(queryTerms, lexicon, documents, n_results, "conjunctive");
                end = System.currentTimeMillis();
                searcher.printResults(end - start);

                log.addLog("query", start, end);

            } else if (userInput == 2) {
                System.out.println("Bye!");
                scanner.close();
                break;
            } else {
                System.out.println("Wrong input, please insert 1 or 2");
            }


        }


    }
}

