package it.unipi.dii.mircv.index.structures;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class PostingListTest {

    @Test
    public void testPostingListReadWrite() {
        System.out.println("Test PostingList read/write");

        Document doc = new Document("1\tadcaj ndcanpd jnqpvjnap", 1);

        PostingList postingList = new PostingList(doc);
        ArrayList<Posting> postings = postingList.getPostings();
        postings.add(new Posting(1, 3));
        postings.add(new Posting(2, 2));
        postings.add(new Posting(3, 5));

        // Salvataggio della posting list su file
        long startOffset = postingList.savePostingListToDisk(1);

        // Lettura della posting list da file
        ArrayList<Posting> loadedPostingList = postingList.readPostingList(1, startOffset);

        // Verifica che i dati siano stati letti correttamente
        assertEquals(3, loadedPostingList.size());
        assertEquals(1, loadedPostingList.get(0).getDocID());
        assertEquals(2, loadedPostingList.get(1).getDocID());
        assertEquals(3, loadedPostingList.get(2).getDocID());

        // Puoi eseguire ulteriori verifiche sui dati letti, come le frequenze dei documenti, ecc.
    }



    @Test
    void savePostingListToDisk() {
    }

    @Test
    void readPostingList() {
    }
}