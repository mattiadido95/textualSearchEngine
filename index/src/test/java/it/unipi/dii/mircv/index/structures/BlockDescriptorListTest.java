package it.unipi.dii.mircv.index.structures;

import static org.junit.jupiter.api.Assertions.*;

import it.unipi.dii.mircv.index.algorithms.Merger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.ArrayList;

public class BlockDescriptorListTest {

    private BlockDescriptorList blockDescriptorList;
    private static final Integer NUMBER_OF_POSTING = 10;
    private static final Integer BLOCK_POSTING_LIST_SIZE = (4 * 2) * NUMBER_OF_POSTING; // 4 byte per docID, 4 byte per freq and postings
    private static final String BLOCK_DESCRIPTOR_PATH = "src/test/data/blockDescriptorTest.bin";
    private static final String POSTING_LIST_PATH = "src/test/data/indexTest.bin";

    @BeforeAll
    public static void setUp() {
        LexiconElem le = new LexiconElem();
        PostingList pl = new PostingList();
        for (int i = 0; i < 26; i++) {
            pl.getPostings().add(new Posting(i, i));
        }
        saveBlockPosting(pl, le);
    }

    // TODO  cambiare il caricamento delle posting list prendendole dai file.bin generati da lexicontest e poi cambiare
    //  il codice del test adenguando i valori da leggere come in postinglisttest

    @Test
    public void testBlockDescriptorListIterator() {
        PostingList pl = new PostingList();
        int i = 0;
        blockDescriptorList = new BlockDescriptorList(0, 3, BLOCK_DESCRIPTOR_PATH);
        // Test per verificare il funzionamento dell'iteratore di BlockDescriptorList
        blockDescriptorList.openBlock();

        assertTrue(blockDescriptorList.hasNext());

        BlockDescriptor blockDescriptor1 = blockDescriptorList.next();
        assertNotNull(blockDescriptor1);
        assertEquals(9, blockDescriptor1.getMaxDocID()); // Sostituisci con i valori corretti
        pl.readPostingList(-1, blockDescriptor1.getNumPosting(), blockDescriptor1.getPostingListOffset(),POSTING_LIST_PATH);

        for (Posting p : pl.getPostings()) {
            assertEquals(i, p.getDocID());
            assertEquals(i, p.getFreq());
            i++;
        }

        BlockDescriptor blockDescriptor2 = blockDescriptorList.next();
        assertNotNull(blockDescriptor2);
        assertEquals(19, blockDescriptor2.getMaxDocID()); // Sostituisci con i valori corretti
        pl.readPostingList(-1, blockDescriptor2.getNumPosting(), blockDescriptor2.getPostingListOffset(),POSTING_LIST_PATH);

        for (Posting p : pl.getPostings()) {
            assertEquals(i, p.getDocID());
            assertEquals(i, p.getFreq());
            i++;
        }
        BlockDescriptor blockDescriptor3 = blockDescriptorList.next();
        assertNotNull(blockDescriptor3);
        assertEquals(25, blockDescriptor3.getMaxDocID()); // Sostituisci con i valori corretti
        pl.readPostingList(-1, blockDescriptor3.getNumPosting(), blockDescriptor3.getPostingListOffset(),POSTING_LIST_PATH);

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

    public static int saveBlockPosting(PostingList mergePostingList, LexiconElem newLexiconElem) {
        // scrittura newPostingList nel file index
        long postingOffsetStart = mergePostingList.savePostingListToDisk(-1,POSTING_LIST_PATH);

        //scorri la newPostingList e ogni NUMBER_OF_POSTING elementi salva il block descriptor
        BlockDescriptor blockDescriptor;
        int blockCounter = 0;
        long blockDescriptorOffset;
        for (int i = 0; i < mergePostingList.getPostingListSize(); i++) {
            if ((i + 1) % NUMBER_OF_POSTING == 0) {
                //salva il block descriptor
                blockDescriptor = new BlockDescriptor(postingOffsetStart, mergePostingList.getPostings().subList(i + 1 - NUMBER_OF_POSTING, i + 1));
                postingOffsetStart += BLOCK_POSTING_LIST_SIZE;
                blockDescriptorOffset = blockDescriptor.saveBlockDescriptorToDisk(BLOCK_DESCRIPTOR_PATH);
                if (blockCounter == 0)
                    //salva inzio del block descriptor nel newLexiconElem
                    newLexiconElem.setOffset(blockDescriptorOffset);
                blockCounter++;
            } else if ((mergePostingList.getPostingListSize() - (blockCounter * NUMBER_OF_POSTING)) < NUMBER_OF_POSTING) {
                //salva il block descriptor
                blockDescriptor = new BlockDescriptor(postingOffsetStart, mergePostingList.getPostings().subList(i, mergePostingList.getPostingListSize()));
                blockDescriptorOffset = blockDescriptor.saveBlockDescriptorToDisk(BLOCK_DESCRIPTOR_PATH);
                if (blockCounter == 0)
                    //salva inzio del block descriptor nel newLexiconElem
                    newLexiconElem.setOffset(blockDescriptorOffset);
                blockCounter++;
                break;
            }

        }
        return blockCounter;

    }
}

