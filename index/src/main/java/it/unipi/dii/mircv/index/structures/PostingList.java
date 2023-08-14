package it.unipi.dii.mircv.index.structures;

import java.util.ArrayList;

public class PostingList {

    // TODO implement method to add posting to existing posting list

    private String term;
    private ArrayList<Posting> postings = new ArrayList<>();

    public PostingList(String term, Document document) {
        this.term = term;
        this.postings.add(new Posting(document.getDocID(), 1));
    }

    public void printPostingList() {
        System.out.println("Term: " + this.term);
        for (Posting posting : this.postings) {
            System.out.println("DocID: " + posting.getDocID() + " Freq: " + posting.getFreq());
        }
    }

}
