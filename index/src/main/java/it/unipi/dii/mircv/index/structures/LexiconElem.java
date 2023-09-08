package it.unipi.dii.mircv.index.structures;

import java.util.Random;

public class LexiconElem {
    private int df; // document frequency, is the number of documents containing the term
    private long cf; // collection frequency, is the number of occurrences of the term in the entire collection
    private long offset; // offset of posting list in SPIMI / offset of first block descriptor in MERGER
    private int numBlock; // number of blocks

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
                '}';
    }


    public Integer getBlocksNumber() {
        return this.numBlock;
    }
}
