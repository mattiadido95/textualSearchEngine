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
    private static final String COLLECTION_PATH = "data/collection/collection.tsv";
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

    public static void main(String[] args) {
        Index index = new Index();
        Spimi spimi = new Spimi(COLLECTION_PATH);
        spimi.execute();
        System.out.println(spimi.getIndexCounter());
        Merger merger = new Merger(INDEX_PATH, spimi.getIndexCounter());
        System.out.println(merger);
        merger.execute();

        index.getLexicon().readLexiconFromDisk(-1);
        // per ogni chiave del lexicon, leggi il posting list dal file
        for (String key : index.getLexicon().getLexicon().keySet()) {
            //get lexicon elem
            LexiconElem lexiconElem = index.getLexicon().getLexiconElem(key);
            PostingList postingList = new PostingList();
            postingList.readPostingList(-1, lexiconElem.getDf(), lexiconElem.getOffset());
            System.out.println(lexiconElem);
            System.out.println(postingList);
        }
        index.setDocuments(Document.readDocumentsFromDisk(-1));
        System.out.println(index.getDocuments());

    }
}














