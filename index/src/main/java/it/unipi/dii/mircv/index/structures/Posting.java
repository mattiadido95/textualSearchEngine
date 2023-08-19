package it.unipi.dii.mircv.index.structures;

import java.io.Serializable;

public class Posting implements Serializable {

    private int docID;
    private int freq;

    public Posting(int docID, int freq) {
        this.docID = docID;
        this.freq = freq;
    }

    public int getDocID() {
        return docID;
    }

    public int getFreq() {
        return freq;
    }

    public void updateFreq() {
        this.freq++;
    }
}
