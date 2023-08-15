package it.unipi.dii.mircv.index.structures;

import java.util.ArrayList;

public class PostingList {


    private String term;
    private ArrayList<Posting> postings = new ArrayList<>();

    public PostingList(String term, Document document) {
        this.term = term;
        this.postings.add(new Posting(document.getDocID(), 1));
    }


    public void printPostingList() {
        StringBuilder output = new StringBuilder();
        output.append("Posting List for Term: ").append(this.term).append("\n");
        output.append("[");

        for (int i = 0; i < this.postings.size(); i++) {
            Posting posting = this.postings.get(i);
            output.append("(").append(posting.getDocID()).append(", ").append(posting.getFreq()).append(")");

            if (i < this.postings.size() - 1) {
                output.append(" -> ");
            }
        }
        output.append("]\n");
        System.out.println(output);
        System.out.println("**************************************");
    }



    // TODO manage updating of frequency for the same document
    public void updatePostingList(String token, Document doc) {
        // TODO Implemented only adding in the posting list, not updating frequency
        Posting newPosting = new Posting(doc.getDocID(), 1);
        this.postings.add(newPosting);
    }

    public int getPostingListSize() {
        return this.postings.size();
    }
}
