package it.unipi.dii.mircv.index.structures;


public class Posting{
    private int docID;
    private int freq;

    public Posting(int docID, int freq) {
        this.docID = docID;
        this.freq = freq;
    }

    @Override
    public String toString() {
        return "Posting{" +
                "docID=" + docID +
                ", freq=" + freq +
                '}';
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
