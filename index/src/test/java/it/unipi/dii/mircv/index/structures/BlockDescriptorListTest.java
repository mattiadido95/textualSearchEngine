package it.unipi.dii.mircv.index.structures;

import static org.junit.jupiter.api.Assertions.*;

import it.unipi.dii.mircv.index.algorithms.Merger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.ArrayList;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BlockDescriptorListTest {

    private BlockDescriptorList blockDescriptorList;

    @BeforeAll
    public void setUp() {
        LexiconElem le = new LexiconElem();
        PostingList pl = new PostingList();
        for (int i = 0; i < 26; i++) {
            pl.getPostings().add(new Posting(i, i));
        }
        Merger.saveBlockPosting(pl, le);
    }

    @Test
    public void testBlockDescriptorListIterator() {
        PostingList pl = new PostingList();
        int i = 0;
        blockDescriptorList = new BlockDescriptorList(0,3);
        // Test per verificare il funzionamento dell'iteratore di BlockDescriptorList
        blockDescriptorList.openBlock();

        assertTrue(blockDescriptorList.hasNext());

        BlockDescriptor blockDescriptor1 = blockDescriptorList.next();
        assertNotNull(blockDescriptor1);
        assertEquals(9, blockDescriptor1.getMaxDocID()); // Sostituisci con i valori corretti
        pl.readPostingList(-1, blockDescriptor1.getNumPosting(), blockDescriptor1.getPostingListOffset());

        for (Posting p : pl.getPostings()) {
            assertEquals(i, p.getDocID());
            assertEquals(i, p.getFreq());
            i++;
        }

        BlockDescriptor blockDescriptor2 = blockDescriptorList.next();
        assertNotNull(blockDescriptor2);
        assertEquals(19, blockDescriptor2.getMaxDocID()); // Sostituisci con i valori corretti
        pl.readPostingList(-1, blockDescriptor2.getNumPosting(), blockDescriptor2.getPostingListOffset());

        for (Posting p : pl.getPostings()) {
            assertEquals(i, p.getDocID());
            assertEquals(i, p.getFreq());
            i++;
        }
        BlockDescriptor blockDescriptor3 = blockDescriptorList.next();
        assertNotNull(blockDescriptor3);
        assertEquals(25, blockDescriptor3.getMaxDocID()); // Sostituisci con i valori corretti
        pl.readPostingList(-1, blockDescriptor3.getNumPosting(), blockDescriptor3.getPostingListOffset());

        for (Posting p : pl.getPostings()) {
            assertEquals(i, p.getDocID());
            assertEquals(i, p.getFreq());
            i++;
        }
        assertFalse(blockDescriptorList.hasNext());
        assertNull(blockDescriptorList.next());

        blockDescriptorList.closeBlock();
    }

    // Aggiungi altri test se necessario

}

