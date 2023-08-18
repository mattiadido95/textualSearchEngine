package it.unipi.dii.mircv.index.structures;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PostingList implements Serializable {
    private ArrayList<Posting> postings;

    public PostingList(Document doc) {
        postings = new ArrayList<>();
        postings.add(new Posting(doc.getDocID(), 1));
    }

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder();
        output.append("[");
        for (int i = 0; i < postings.size(); i++) {
            Posting posting = postings.get(i);
            int docID = posting.getDocID();
            int freq = posting.getFreq();

            output.append("(").append(docID).append(", ").append(freq).append(")");

            if (i < postings.size() - 1) {
                output.append(" -> ");
            }
        }
        output.append("]\n");
        System.out.println(output);
        System.out.println("**************************************");
        return output.toString();
    }

    // binary search on posting list to find the document return the posting
    public void updatePostingList(Document doc) {
        int docID = doc.getDocID();
        int low = 0;
        int high = this.postings.size() - 1;

        while (low <= high) {
            int mid = (low + high) / 2;
            Posting posting = this.postings.get(mid);
            int postingDocID = posting.getDocID();

            if (postingDocID == docID) {
                posting.updateFreq();
            } else if (postingDocID < docID) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        // posting list doesn't contain the document, create new posting
        Posting newPosting = new Posting(doc.getDocID(), 1); // create new posting
        this.postings.add(newPosting); // add posting to posting list
    }

    public int getPostingListSize() {
        return this.postings.size();
    }
}
