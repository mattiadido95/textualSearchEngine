package it.unipi.dii.mircv.index.structures;

import java.io.Serializable;

public class Posting implements Serializable {

    private String docID;
    private int freq;

    public Posting(String docID, int freq) {
        this.docID = docID;
        this.freq = freq;
    }

    public String getDocID() {
        return docID;
    }

    public int getFreq() {
        return freq;
    }

    public void updateFreq() {
        this.freq++;
    }
}
