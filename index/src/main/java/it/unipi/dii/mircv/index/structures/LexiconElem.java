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
    public LexiconElem(String term, int df, long cf, long offset) {
        this.term = term; // TODO rimuovere ridondanza, forse era stata inserita per fare la sort del lexicon(ma non serve piu)
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

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public void setDf(int newDf) {
        this.df = newDf;
    }

    public void incrementCf() {
        this.cf += 1;
    }

    public void mergeLexiconElem(LexiconElem lexiconElem) {
        if (lexiconElem.getTerm().equals("â\u0080¦")) {
            System.out.println("Error: terms are different");

        }
        this.df += lexiconElem.getDf();
        this.cf += lexiconElem.getCf();
    }

    @Override
    public String toString() {
        return "LexiconElem{" +
                "term='" + term + '\'' +
                ", df=" + df +
                ", cf=" + cf +
                ", offset=" + offset +
                '}';
    }
}
