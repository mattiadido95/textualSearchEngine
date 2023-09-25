package it.unipi.dii.mircv.index;

import it.unipi.dii.mircv.index.algorithms.Merger;
import it.unipi.dii.mircv.index.algorithms.Spimi;
import it.unipi.dii.mircv.index.structures.*;
import it.unipi.dii.mircv.index.utility.Logs;

import java.io.*;
import java.util.ArrayList;


/**
 * The Index class implements the main program for building an inverted index from a collection of documents.
 */
public class Index {
    //https://microsoft.github.io/msmarco/TREC-Deep-Learning-2020
    private static final String INDEX_PATH = "data/index";
    private Lexicon lexicon;
    private ArrayList<Document> documents;

    /**
     * Constructs a new Index object.
     */
    public Index() {
        this.lexicon = new Lexicon();
        this.documents = new ArrayList<>();
    }

    public Lexicon getLexicon() {
        return lexicon;
    }

    public void setLexicon(Lexicon lexicon) {
        this.lexicon = lexicon;
    }

    public ArrayList<Document> getDocuments() {
        return documents;
    }

    public void setDocuments(ArrayList<Document> documents) {
        this.documents = documents;
    }

    /**
     * Executes the main program for building an inverted index from a collection of documents.
     *
     * @param args The command-line arguments.
     * The list of accepted arguments is:
     *             -compressed: Enable compressed reading of the collection in the tar.gz format. Default: uncompressed reading.
     *             -stemmer: Enable PorterStemming in document preprocessing. Default: disabled.
     *             -help: Show the help message.
     */
    public static void main(String[] args) throws IOException {
        // create folder logs if not exists
        File logsFolder = new File("data/logs");
        if (!logsFolder.exists())
            logsFolder.mkdir();
        //create folder trec_eval if not exists
        File trec_evalFolder = new File("data/trec_eval");
        if (!trec_evalFolder.exists())
            trec_evalFolder.mkdir();

        Logs log = new Logs();
        long start, end;

        // process options from command line
        boolean[] options = processOptions(args);
        boolean compressed_reading = options[0];
        boolean porterStemmer = options[1];
        printOptions(options);
        String COLLECTION_PATH;
        if (compressed_reading)
            COLLECTION_PATH = "data/collection/collection.tar.gz";
        else
            COLLECTION_PATH = "data/collection/collectionTest.tsv";

        // spimi algorithm
        Spimi spimi = new Spimi(COLLECTION_PATH, porterStemmer, compressed_reading);
        start = System.currentTimeMillis();
        spimi.execute();
        end = System.currentTimeMillis();
        log.addLog("spimi", start, end);

        // merger algorithm
        Merger merger = new Merger(INDEX_PATH, spimi.getIndexCounter());
        start = System.currentTimeMillis();
        merger.execute();
        end = System.currentTimeMillis();
        log.addLog("merger", start, end);
    }

    private static void printOptions(boolean[] options) {
        System.out.println("Options:");
        System.out.println("----------------------------");
        System.out.println("|   compressed  |   " + options[0] + "  |");
        System.out.println("|   stemmer     |   " + options[1] + "  |");
        System.out.println("----------------------------");
    }

    /**
     * Processes the command-line arguments.
     *
     * @param args The command-line arguments.
     * @return An array of booleans indicating the options selected.
     */
    private static boolean[] processOptions(String[] args) {
        boolean compressed_reading = false;
        boolean porterStemmer = false;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-compressed")) {
                compressed_reading = true;
            } else if (args[i].equals("-stemmer")) {
                porterStemmer = true;
            } else if (args[i].equals("-help")) {
                System.out.println("Program usage:");
                System.out.println("-compressed: Enable compressed reading of the collection in the tar.gz format. Default: uncompressed reading.");
                System.out.println("-stemmer: Enable PorterStemming in document preprocessing. Default: disabled.");
                System.out.println("-help: Show this help message."); // TODO forse non serve se lo si mette nel bash script
                System.exit(0);
            } else {
                System.err.println("Unrecognized option: " + args[i]);
                System.exit(1);
            }
        }
        // Restituisci le opzioni aggiornate come array di booleani
        return new boolean[]{compressed_reading, porterStemmer};
    }

}














