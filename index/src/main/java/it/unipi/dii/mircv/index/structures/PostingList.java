package it.unipi.dii.mircv.index.structures;

import java.util.ArrayList;

public class PostingList {

    private String term;
    private ArrayList<Posting> postings = new ArrayList<>();

    public PostingList(String term, Document document) {
        this.term = term;
        this.postings.add(new Posting(document.getDocID(), 1));
    }


}
