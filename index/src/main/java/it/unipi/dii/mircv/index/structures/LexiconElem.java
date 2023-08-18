package it.unipi.dii.mircv.index.structures;

import java.util.Random;

public class LexiconElem {
    private String term;
    private int df = 0; // document frequency, is the number of documents containing the term
    private long cf = 0; // collection frequency, is the number of occurrences of the term in the entire collection
    private long offset = new Random().nextLong(); // offset of posting list in the inverted index file

    public LexiconElem(String term) {
        this.term = term;
        this.df = df;
        this.cf = cf;
        this.offset = offset;
    }

    public String getTerm() {
        return this.term;
    }

    public int getDf() {
        return this.df;
    }

    public long getCf() {
        return this.cf;
    }

    public long getOffset() {
        return this.offset;
    }

    public void incrementDf() {
        this.df += 1;
    }

    public void incrementCf() {
        this.cf += 1;
    }
}
