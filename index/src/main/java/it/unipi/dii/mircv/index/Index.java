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
    private static Logs log;

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

    public static String convertMillisecondsToHMmSs(long milliseconds) {
        long seconds = milliseconds / 1000;
        long hours = seconds / 3600;
        seconds = seconds % 3600;
        long minutes = seconds / 60;
        seconds = seconds % 60;

        return hours + "h " + minutes + "m " + seconds + "s";
    }

    public static void main(String[] args) throws IOException {

        Logs log = new Logs();
        long start, end;

        Index index = new Index();
        Spimi spimi = new Spimi(COMPRESSED_COLLECTION_PATH);
        start = System.currentTimeMillis();
        spimi.execute();
        end = System.currentTimeMillis();
        log.addLog("spimi", start, end);
//        System.out.println(spimi.getIndexCounter());
        Merger merger = new Merger(INDEX_PATH, spimi.getIndexCounter());
        start = System.currentTimeMillis();
        merger.execute();
        end = System.currentTimeMillis();
        log.addLog("merger", start, end);

        index.getLexicon().readLexiconFromDisk(-1);
        // per ogni chiave del lexicon, leggi il posting list dal file
        for (String key : index.getLexicon().getLexicon().keySet()) {
            //get lexicon elem
            LexiconElem lexiconElem = index.getLexicon().getLexiconElem(key);
            PostingList postingList = new PostingList();
            postingList.readPostingList(-1, lexiconElem.getDf(), lexiconElem.getOffset());
//            System.out.println(lexiconElem);
//            System.out.println(postingList);
        }
        index.setDocuments(Document.readDocumentsFromDisk(-1));
//        System.out.println(index.getDocuments());

    }
}














