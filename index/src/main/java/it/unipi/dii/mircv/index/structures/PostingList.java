package it.unipi.dii.mircv.index.structures;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PostingList implements Serializable {
    private String term;

    // TODO VA MESSO ARRAYLIST
    private HashMap<Integer, Posting> postings = new HashMap<>();

    public PostingList(String term, Document document) {
        this.term = term;
        this.postings.put(Integer.valueOf(document.getDocID()), new Posting(document.getDocID(), 1));
    }


    public void printPostingList() {
        StringBuilder output = new StringBuilder();
        output.append("Posting List for Term: ").append(this.term).append("\n");
        output.append("[");

        int count = 0;
        for (Map.Entry<Integer, Posting> entry : postings.entrySet()) {
            int docID = Integer.parseInt(entry.getValue().getDocID());
            int freq = entry.getValue().getFreq();

            output.append("(").append(docID).append(", ").append(freq).append(")");

            if (count < postings.size() - 1) {
                output.append(" -> ");
            }
            count++;
        }
        output.append("]\n");
        System.out.println(output);
        System.out.println("**************************************");
    }


    public void updatePostingList(String token, Document doc) {
        // check if posting list already contains the document
        if (this.postings.containsKey(Integer.valueOf(doc.getDocID()))) {
            Posting posting = this.postings.get(Integer.valueOf(doc.getDocID())); // get existing posting from posting list
            posting.updateFreq(); // update frequency of the posting, increment by 1
        } else {
            // posting list doesn't contain the document, create new posting
            Posting newPosting = new Posting(doc.getDocID(), 1); // create new posting
            this.postings.put(Integer.valueOf(doc.getDocID()), newPosting); // add posting to posting list
        }
    }

    public int getPostingListSize() {
        return this.postings.size();
    }
}
