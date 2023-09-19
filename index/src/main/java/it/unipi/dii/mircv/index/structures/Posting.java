package it.unipi.dii.mircv.index.structures;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

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
