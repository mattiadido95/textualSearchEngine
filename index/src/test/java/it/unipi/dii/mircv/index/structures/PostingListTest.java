package it.unipi.dii.mircv.index.structures;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class PostingListTest {

    private static final String POSTING_LIST_PATH = "src/test/data/postingListTest.bin";
    private static final String LEXICON_PATH = "src/test/data/lexicon.bin";
    private static final String BLOCK_DESCRIPTOR_PATH = "src/test/data/blockDescriptorTest.bin";

    // HA UNA DIPENDENZA A LEXICONTEST, CHE DEVE ESSERE ESEGUITO PRIMA DI QUESTO TEST
    @Test
    public void testPostingListReadWrite() {
        System.out.println("Test PostingList read/write");

        Lexicon lexicon = new Lexicon();
        lexicon.readLexiconFromDisk(-1, LEXICON_PATH);

        LexiconElem le = lexicon.getLexicon().get("cane");
        PostingList pl = new PostingList();
        pl.readPostingList(-1, le.getDf(), BlockDescriptor.readFirstBlock(le.getOffset(), BLOCK_DESCRIPTOR_PATH).getPostingListOffset(), POSTING_LIST_PATH);
        // assert that the posting list is read correctly
        assertEquals(78, pl.getPostings().size());
        pl.openList();
        int i = 0;
        while (pl.hasNext()) {
            if (i == 77)
                break;
            pl.next();
            assertEquals(i, pl.getDocId());
            assertEquals(i, pl.getFreq());
            i++;
        }
        assertEquals(77, i);

        le = lexicon.getLexicon().get("gatto");
        pl = new PostingList();
        pl.readPostingList(-1, le.getDf(), BlockDescriptor.readFirstBlock(le.getOffset(), BLOCK_DESCRIPTOR_PATH).getPostingListOffset(), POSTING_LIST_PATH);
        // assert that the posting list is read correctly
        assertEquals(57, pl.getPostings().size());
        pl.openList();
        i = 21;
        while (pl.hasNext()) {
            if (i == 77)
                break;
            pl.next();
            assertEquals(i, pl.getDocId());
            assertEquals(i, pl.getFreq());
            i++;
        }
        assertEquals(77, i);

        le = lexicon.getLexicon().get("topo");
        pl = new PostingList();
        pl.readPostingList(-1, le.getDf(), BlockDescriptor.readFirstBlock(le.getOffset(), BLOCK_DESCRIPTOR_PATH).getPostingListOffset(), POSTING_LIST_PATH);
        // assert that the posting list is read correctly
        assertEquals(45, pl.getPostings().size());
        pl.openList();
        i = 33;
        while (pl.hasNext()) {
            if (i == 77)
                break;
            pl.next();
            assertEquals(i, pl.getDocId());
            assertEquals(i, pl.getFreq());
            i++;
        }
        assertEquals(77, i);

    }

    @Test
    public void testNextGEQ() {
        Lexicon lexicon = new Lexicon();
        lexicon.readLexiconFromDisk(-1, LEXICON_PATH);

        LexiconElem le = lexicon.getLexicon().get("cane");
        HashMap<String, LexiconElem> queryTermsMap = new HashMap<>();
        queryTermsMap.put("cane", le);
        ArrayList<Integer> blocksNumber = new ArrayList<>();
        blocksNumber.add(le.getBlocksNumber()); //TODO FACENDO DEBUG ho visto che è a size 2 e non 1
        ArrayList<BlockDescriptorList> blockDescriptorList = new ArrayList<>();
        ArrayList<PostingList> postingLists = new ArrayList<>();

        initializePostingListForQueryTerms(queryTermsMap, blocksNumber, lexicon, blockDescriptorList, postingLists);

        // 0 1 2 3 4 5 6 7 8 9 blocco 0
        // 10 11 12 13 14 15 16 17 18 19 blocco 1
        // 20 21 22 23 24 25 26 27 28 29 blocco 2
        // 30 31 32 33 34 35 36 37 38 39 blocco 3
        // 40 41 42 43 44 45 46 47 48 49 blocco 4
        // 50 51 52 53 54 55 56 57 58 59 blocco 5
        // 60 61 62 63 64 65 66 67 68 69 blocco 6
        // 70 71 72 73 74 75 76  blocco 7
        PostingList pl = postingLists.get(0);
        Posting p = pl.nextGEQ(0, blockDescriptorList.get(0), le.getBlocksNumber(), POSTING_LIST_PATH);
        assertEquals(0, p.getDocID());
        assertEquals(0, pl.getDocId());
        p = pl.nextGEQ(7, blockDescriptorList.get(0), le.getBlocksNumber(), POSTING_LIST_PATH);
        assertEquals(7, p.getDocID());
        assertEquals(7, pl.getDocId());
        p = pl.nextGEQ(56, blockDescriptorList.get(0), le.getBlocksNumber(), POSTING_LIST_PATH);
        assertEquals(56, p.getDocID());
        assertEquals(56, pl.getDocId());
        p = pl.nextGEQ(33, blockDescriptorList.get(0), le.getBlocksNumber(), POSTING_LIST_PATH);
        assertEquals(33, p.getDocID());
        assertEquals(33, pl.getDocId());
        p = pl.nextGEQ(101, blockDescriptorList.get(0), le.getBlocksNumber(), POSTING_LIST_PATH);
        assertEquals(null, p);

        p = pl.nextGEQ(99, blockDescriptorList.get(0), le.getBlocksNumber(), POSTING_LIST_PATH);

        assertEquals(100, p.getDocID());
        assertEquals(100, pl.getDocId());

    }

    private void initializePostingListForQueryTerms(HashMap<String, LexiconElem> queryTermsMap, ArrayList<Integer> blocksNumber, Lexicon lexicon, ArrayList<BlockDescriptorList> blockDescriptorList, ArrayList<PostingList> postingLists) {
        int i = 0;
        long firstBlockOffset;
        for (String term : queryTermsMap.keySet()) {
            firstBlockOffset = lexicon.getLexiconElem(term).getOffset();
            blocksNumber.add(lexicon.getLexiconElem(term).getBlocksNumber());
            //read all blocks
            blockDescriptorList.add(new BlockDescriptorList(firstBlockOffset, blocksNumber.get(i), BLOCK_DESCRIPTOR_PATH));
            blockDescriptorList.get(i).openBlock();
            blockDescriptorList.get(i).next();
            //load first posting list for the term
            PostingList postingList = new PostingList();
            postingList.readPostingList(-1, blockDescriptorList.get(i).getNumPosting(), blockDescriptorList.get(i).getPostingListOffset(), POSTING_LIST_PATH);
            postingList.openList();
            postingLists.add(postingList); // add postinglist of the term to postingListIterators
            postingLists.get(i).next();
            i++;
        }
    }

}