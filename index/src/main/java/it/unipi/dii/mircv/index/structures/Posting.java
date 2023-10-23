package it.unipi.dii.mircv.index.structures;


/**
 * The Posting class implements a posting in the posting list for a term in the inverted index.
 * It stores the document ID and the frequency of the term in the document.
 */
public class Posting {
    private int docID;
    private int freq;

    /**
     * Constructs a new Posting object.
     *
     * @param docID The document ID.
     * @param freq  The frequency of the term in the document.
     */
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
