package it.unipi.dii.mircv.index;

import it.unipi.dii.mircv.index.algorithms.Merger;
import it.unipi.dii.mircv.index.algorithms.Spimi;
import it.unipi.dii.mircv.index.preprocessing.Preprocessing;
import it.unipi.dii.mircv.index.structures.*;
import it.unipi.dii.mircv.index.utility.Logs;
import it.unipi.dii.mircv.index.utility.MemoryManager;

import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Index {
    //https://microsoft.github.io/msmarco/TREC-Deep-Learning-2020
    private static final String INDEX_PATH = "data/index";
    private Lexicon lexicon;
    private ArrayList<Document> documents;

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

    public static void main(String[] args) throws IOException {
        Logs log = new Logs();
        long start, end;

        boolean[] options = processOptions(args);
        boolean compressed_reading = options[0];
        boolean porterStemmer = options[1];
        String COLLECTION_PATH;

        if (compressed_reading)
            COLLECTION_PATH = "data/collection/collection.tar.gz";
        else
            COLLECTION_PATH = "data/collection/collection.tsv";

        Spimi spimi = new Spimi(COLLECTION_PATH, porterStemmer, compressed_reading);
        start = System.currentTimeMillis();
        spimi.execute();
        end = System.currentTimeMillis();
        log.addLog("spimi", start, end);
        Merger merger = new Merger(INDEX_PATH, spimi.getIndexCounter());
        start = System.currentTimeMillis();
        merger.execute();
        end = System.currentTimeMillis();
        log.addLog("merger", start, end);
    }

    private static boolean[] processOptions(String[] args) {
        boolean compressed_reading = false;
        boolean porterStemmer = false;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-compressed")) {
                compressed_reading = true;
            } else if (args[i].equals("-stemmer")) {
                porterStemmer = true;
            } else if (args[i].equals("-help")) {
                System.out.println("Uso del programma:");
                System.out.println("-compressed : Abilita la lettura compressa della collezione, nel formato tar.gz.");
                System.out.println("-stemmer: Abilita il PorterStemming nel preprocessing dei documenti.");
                System.out.println("-help: Mostra questo messaggio di aiuto.");
                System.exit(0);
            } else {
                System.err.println("Opzione non riconosciuta: " + args[i]);
                System.exit(1);
            }
        }

        // Restituisci le opzioni aggiornate come array di booleani
        return new boolean[]{compressed_reading, porterStemmer};
    }


}














