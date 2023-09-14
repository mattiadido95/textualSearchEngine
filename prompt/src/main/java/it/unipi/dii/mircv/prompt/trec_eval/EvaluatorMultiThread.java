package it.unipi.dii.mircv.prompt.trec_eval;

import it.unipi.dii.mircv.index.structures.Document;
import it.unipi.dii.mircv.index.structures.Lexicon;
import it.unipi.dii.mircv.index.utility.Logs;
import it.unipi.dii.mircv.prompt.query.Query;
import it.unipi.dii.mircv.prompt.query.Searcher;
import it.unipi.dii.mircv.prompt.structure.QueryResult;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EvaluatorMultiThread {
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
    private static final int NUM_THREADS = 4; // Numero di thread o job paralleli
    private static final int NUM_QUERIES = 100000; // Numero totale di query
    private static final String OUTPUT_FILE = "results.txt"; // File di output
    public static boolean[] t_main = new boolean[NUM_THREADS];

    public EvaluatorMultiThread(Searcher searcher, Lexicon lexicon, ArrayList<Document> documents, int n_results, String mode) {
        this.searcher = searcher;
        this.lexicon = lexicon;
        this.documents = documents;
        this.n_results = n_results;
        this.mode = mode;
        arrayQueryResults = new ArrayList<>();
        queryIDs = new ArrayList<>();
    }

    private List<String> loadAllQueries() {

        List<String> queries = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(QUERY_PATH)))) {
            String line; // start reading query by query
            int queryCounter = 0;
            while ((line = br.readLine()) != null) {

                queries.add(line);
                queryCounter++;
                if (queryCounter == 1000) {
                    break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return queries;
    }

    private static List<List<String>> splitQueries(List<String> queries, int numThreads) {
        List<List<String>> subsets = new ArrayList<>();
        int subsetSize = queries.size() / numThreads;
        int startIndex = 0;

        for (int i = 0; i < numThreads; i++) {
            int endIndex = startIndex + subsetSize;
            if (i == numThreads - 1) {
                endIndex = queries.size(); // L'ultimo thread prende tutte le query rimanenti
            }
            List<String> subset = queries.subList(startIndex, endIndex);
            subsets.add(subset);
            startIndex = endIndex;
        }
        return subsets;
    }

    private static class QueryProcessor implements Runnable {
        private final int threadId;
        private final List<String> thread_queries;
        private ArrayList<String> thread_queryIDs;
        private Searcher thread_searcher;
        private Lexicon thread_lexicon;
        private ArrayList<Document> thread_documents;
        private int thread_n_results;
        private String thread_mode;
        private ArrayList<ArrayList<QueryResult>> thread_arrayQueryResults;
        private boolean[] t;

        public QueryProcessor(int threadId, List<String> queries, Searcher searcher, Lexicon lexicon, ArrayList<Document> documents, int n_results, String mode, boolean[] t) {
            this.threadId = threadId;
            this.thread_queries = queries;
            this.thread_searcher = searcher;
            this.thread_lexicon = lexicon;
            this.thread_documents = documents;
            this.thread_n_results = n_results;
            this.thread_mode = mode;
            this.thread_arrayQueryResults = new ArrayList<>();
            this.thread_queryIDs = new ArrayList<>();
            this.t = t;
        }

        public void run() {
            long start, end;
            try {
                start = System.currentTimeMillis();
                // Elabora le query e scrivi i risultati su un file specifico per il thread
                String outputFile = "data/collection/results_thread_" + threadId + ".txt";
                BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

                for (String query : thread_queries) {

                    String[] split = query.split("\t");
                    String queryId = split[0];
                    String queryText = split[1];

                    this.thread_queryIDs.add(queryId);
                    Query queryObj = new Query(queryText);
                    ArrayList<String> queryTerms = queryObj.getQueryTerms();

                    this.thread_searcher.DAAT(queryTerms, this.thread_lexicon, this.thread_documents, this.thread_n_results, this.thread_mode,"BM25");
                    this.thread_arrayQueryResults.add(new ArrayList<>(this.thread_searcher.getQueryResults()));

                    for (int i = 0; i < this.thread_arrayQueryResults.size(); i++) {
                        for (int j = 0; j < this.thread_arrayQueryResults.get(i).size(); j++) {
                            String line = this.thread_queryIDs.get(i) + "\tQ0\t" + this.thread_arrayQueryResults.get(i).get(j).getDocNo() + "\t" + (j + 1) + "\t" + this.thread_arrayQueryResults.get(i).get(j).getScoring() + "\tSTANDARD\n";
                            writer.write(line);
                        }
                    }
                }

                synchronized (t) {
                    t[threadId] = true;
                }

                writer.close();
                Logs log = new Logs();
                log.getLog("Thread " + threadId + " ha completato l'elaborazione.");
                end = System.currentTimeMillis();
                log.addLog("Thread_" + threadId, start, end);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void execute() throws InterruptedException {
        List<String> allQueries = loadAllQueries();

        // Divide le query in sottoinsiemi per i thread
        List<List<String>> querySubsets = splitQueries(allQueries, NUM_THREADS);

        ExecutorService executorService = Executors.newFixedThreadPool(NUM_THREADS);
        for (int i = 0; i < NUM_THREADS; i++) {
            List<String> subset = querySubsets.get(i);
            Searcher thread_searcher = new Searcher();
            executorService.submit(new QueryProcessor(i, subset, thread_searcher, this.lexicon, this.documents, this.n_results, this.mode, this.t_main));
        }
        executorService.shutdown();

        while (!allThreadEnds(t_main)) {
            // Aspetta che tutti i thread abbiano terminato
            Thread.sleep(100);
        }
        concatenateFileResults("results.test", "results_thread_0.txt", "results_thread_1.txt", "results_thread_2.txt", "results_thread_3.txt");

//        trecEvalLauncher();
    }

    private boolean allThreadEnds(boolean[] array) {
        for (boolean b : array) {
            if (!b) {
                return false;
            }
        }
        return true;
    }


    private void concatenateFileResults(String outputFileName, String... inputFiles) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("data/collection/" + outputFileName))) {
            for (String inputFile : inputFiles) {
                System.out.println("Concatenazione del file " + inputFile + " in corso...");
                try (BufferedReader reader = new BufferedReader(new FileReader("data/collection/" + inputFile))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        writer.write(line);
                        writer.newLine();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("File concatenati con successo.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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

    private void trecEvalLauncher() {
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
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                 BufferedWriter bw = new BufferedWriter(new FileWriter(EVALUATION_PATH))) {

                String line;
                StringBuilder output = new StringBuilder();

                while ((line = reader.readLine()) != null) {
                    output.append(line).append(System.lineSeparator()); // Aggiungi una nuova riga
                }

                // Scrivi l'output nel file
                bw.write(output.toString());
            }

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