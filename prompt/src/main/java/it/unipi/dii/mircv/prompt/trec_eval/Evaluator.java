package it.unipi.dii.mircv.prompt.trec_eval;

import it.unipi.dii.mircv.index.structures.Document;
import it.unipi.dii.mircv.index.structures.Lexicon;
import it.unipi.dii.mircv.prompt.query.Query;
import it.unipi.dii.mircv.prompt.query.Searcher;
import it.unipi.dii.mircv.prompt.structure.QueryResult;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class Evaluator {
    private Searcher searcher;
    private Lexicon lexicon;
    private ArrayList<Document> documents;
    private int n_results;
    private String mode;
    private Query query;
    private ArrayList<String> queryIDs;
    private ArrayList<ArrayList<QueryResult>> arrayQueryResults;
    private static final String QUERY_PATH = "data/collection/queries.dev.tsv";
    private static final String Q_REL_PATH = "data/collection/qrels.dev.tsv";
    private static final String RESULTS_PATH = "data/collection/results.test";
    private static final String EVALUATION_PATH = "data/collection/evaluation.txt";

    public Evaluator(Searcher searcher, Lexicon lexicon, ArrayList<Document> documents, int n_results, String mode) {
        this.searcher = searcher;
        this.lexicon = lexicon;
        this.documents = documents;
        this.n_results = n_results;
        this.mode = mode;
        arrayQueryResults = new ArrayList<>();
        queryIDs = new ArrayList<>();
    }

    public void execute() {
        // leggi una query e sottomettila al motore di ricerca
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(QUERY_PATH)))) {
            String line; // start reading query by query

            int queryCounter = 0;

            while ((line = br.readLine()) != null) {
                String[] input = line.split("\t");
                String queryId = input[0];
                String queryText = input[1];

                queryIDs.add(queryId);
                query = new Query(queryText);
                ArrayList<String> queryTerms = query.getQueryTerms();
                // esegui la query
                searcher.DAAT_block(queryTerms, lexicon, documents, n_results, mode);
                arrayQueryResults.add(searcher.getQueryResults());
                queryCounter++;

                if (queryCounter % 10000 == 0) {
                    System.out.println("Query " + queryCounter + " processed");
                    break;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //salvare in un file i risultati results.test
        saveResults();
        //avviare trec eval
        trecEvalLaucher();
    }

    //topicid   Q0  docno   rank    score   STANDARD
    private void saveResults() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(RESULTS_PATH))) {
            for (int i = 0; i < queryIDs.size(); i++) {
                for (int j = 0; j < arrayQueryResults.get(i).size(); j++) {
                    String line = queryIDs.get(i) + "\tQ0\t" + arrayQueryResults.get(i).get(j).getDocNo() + "\t" + (j + 1) + "\t" + arrayQueryResults.get(i).get(j).getScoring() + "\tSTANDARD";
                    bw.write(line);
                    bw.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void trecEvalLaucher() {
        try {
            // Costruisci il comando come una lista di stringhe
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "../trec_eval/trec_eval",
                    "-q",
                    "-c",
                    "-M1000",
                    Q_REL_PATH,
                    RESULTS_PATH
            );

            // Avvia il processo
            Process process = processBuilder.start();

            // Attendere che il processo termini
            int exitCode = process.waitFor();

            // Utilizza un BufferedReader per leggere l'output del processo
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedWriter bw = new BufferedWriter(new FileWriter(RESULTS_PATH));
            String line;
            while ((line = reader.readLine()) != null) {
                bw.write(line);
            }

            // Chiudi i buffer
            reader.close();
            bw.close();

            if (exitCode == 0) {
                System.out.println("Il comando è stato eseguito con successo.");
            } else {
                System.err.println("Il comando ha restituito un codice di uscita diverso da zero.");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void printResults() {
        File file = new File(EVALUATION_PATH);
        try {
            if (!file.exists())
                Files.createFile(Path.of(EVALUATION_PATH));
            BufferedReader reader = new BufferedReader(new FileReader(new File(EVALUATION_PATH)));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}