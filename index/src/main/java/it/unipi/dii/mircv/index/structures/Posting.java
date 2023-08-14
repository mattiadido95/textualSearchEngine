package it.unipi.dii.mircv.index.structures;

public class Posting {

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
}
