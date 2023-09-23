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
    private static final String COLLECTION_PATH = "data/collection/collection.tsv";
    private static final String COMPRESSED_COLLECTION_PATH = "data/collection/collection.tar.gz";
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
        Boolean compressed_reading = false;
        Boolean porterStemmer = false;

        processOptions(args, compressed_reading, porterStemmer);

        Spimi spimi = new Spimi(COMPRESSED_COLLECTION_PATH);
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

    private static void processOptions(String[] args, Boolean compressed_reading, Boolean porterStemmer) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-compressed")) { //compressed_reading
            } else if (args[i].equals("-stemmer")) { // porterStemmer
                porterStemmer = true;
            } else {
                System.err.println("Opzione non riconosciuta: " + args[i]);
                System.exit(1);
            }
        }

        System.out.println("Compressed: " + compressed_reading);
        System.out.println("Porter: " + porterStemmer);
    }

}














