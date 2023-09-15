package it.unipi.dii.mircv.index.structures;

import java.util.Random;

public class LexiconElem {
    private int df; // document frequency, is the number of documents containing the term
    private long cf; // collection frequency, is the number of occurrences of the term in the entire collection
    private long offset; // offset of posting list in SPIMI / offset of first block descriptor in MERGER
    private int numBlock; // number of blocks
    private double TUB_bm25; // term upper bound for bm25
    private double TUB_tfidf; // term upper bound for tfidf


    public LexiconElem(){
        this.cf = 0;
        this.df = 0;
    }

    public LexiconElem(int df, long cf, long offset, int numBlock) {
        this.df = df;
        this.cf = cf;
        this.offset = offset;
        this.numBlock = numBlock;
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

    public double getTUB_bm25() {
        return TUB_bm25;
    }

    public void setTUB_bm25(double TUB_bm25) {
        this.TUB_bm25 = TUB_bm25;
    }

    public double getTUB_tfidf() {
        return TUB_tfidf;
    }

    public void setTUB_tfidf(double TUB_tfidf) {
        this.TUB_tfidf = TUB_tfidf;
    }

    public void mergeLexiconElem(LexiconElem lexiconElem) {
        this.df += lexiconElem.getDf();
        this.cf += lexiconElem.getCf();
    }

    @Override
    public String toString() {
        return "LexiconElem{" +
                "df=" + df +
                ", cf=" + cf +
                ", offset=" + offset +
                ", numBlock=" + numBlock +
                ", TUB_bm25=" + TUB_bm25 +
                ", TUB_tfidf=" + TUB_tfidf +
                '}';
    }

    public int compareBM25(LexiconElem lexiconElem) {
        if (this.TUB_bm25 > lexiconElem.getTUB_bm25()) {
            return 1;
        } else if (this.TUB_bm25 < lexiconElem.getTUB_bm25()) {
            return -1;
        } else {
            return 0;
        }
    }

    public int compareTFIDF(LexiconElem lexiconElem) {
        if (this.TUB_tfidf > lexiconElem.getTUB_tfidf()) {
            return 1;
        } else if (this.TUB_tfidf < lexiconElem.getTUB_tfidf()) {
            return -1;
        } else {
            return 0;
        }
    }

    public Integer getBlocksNumber() {
        return this.numBlock;
    }
}
