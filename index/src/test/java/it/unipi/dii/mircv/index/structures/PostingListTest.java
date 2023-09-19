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
        lexicon.readLexiconFromDisk(-1,LEXICON_PATH);

        LexiconElem le =  lexicon.getLexicon().get("cane");
        PostingList pl = new PostingList();
        pl.readPostingList(-1, le.getDf(), BlockDescriptor.readFirstBlock(le.getOffset(), BLOCK_DESCRIPTOR_PATH).getPostingListOffset(),POSTING_LIST_PATH);
        // assert that the posting list is read correctly
        assertEquals(77, pl.getPostings().size());
        pl.openList();
        int i = 0;
        while(pl.hasNext()){
            pl.next();
            assertEquals(i,pl.getDocId());
            assertEquals(i,pl.getFreq());
            i++;
        }
        assertEquals(77 , i);

        le =  lexicon.getLexicon().get("gatto");
        pl = new PostingList();
        pl.readPostingList(-1, le.getDf(), BlockDescriptor.readFirstBlock(le.getOffset(), BLOCK_DESCRIPTOR_PATH).getPostingListOffset(),POSTING_LIST_PATH);
        // assert that the posting list is read correctly
        assertEquals(56, pl.getPostings().size());
        pl.openList();
        i = 21;
        while(pl.hasNext()){
            pl.next();
            assertEquals(i,pl.getDocId());
            assertEquals(i,pl.getFreq());
            i++;
        }
        assertEquals(77 , i);

        le =  lexicon.getLexicon().get("topo");
        pl = new PostingList();
        pl.readPostingList(-1, le.getDf(), BlockDescriptor.readFirstBlock(le.getOffset(), BLOCK_DESCRIPTOR_PATH).getPostingListOffset(),POSTING_LIST_PATH);
        // assert that the posting list is read correctly
        assertEquals(44, pl.getPostings().size());
        pl.openList();
        i = 33;
        while(pl.hasNext()){
            pl.next();
            assertEquals(i,pl.getDocId());
            assertEquals(i,pl.getFreq());
            i++;
        }
        assertEquals(77 , i);

    }

    @Test
    public void testNextGEQ(){
        Lexicon lexicon = new Lexicon();
        lexicon.readLexiconFromDisk(-1,LEXICON_PATH);

        LexiconElem le =  lexicon.getLexicon().get("gatto");
        HashMap<String, LexiconElem> queryTermsMap = new HashMap<>();
        queryTermsMap.put("gatto", le);
        ArrayList<Integer> blocksNumber = new ArrayList<>();
        blocksNumber.add(le.getBlocksNumber());
        ArrayList<BlockDescriptorList> blockDescriptorList = new ArrayList<>();
        ArrayList<PostingList> postingLists = new ArrayList<>();

        initializePostingListForQueryTerms(queryTermsMap,blocksNumber,lexicon,blockDescriptorList,postingLists);

        PostingList pl = postingLists.get(0);
        Posting p = pl.nextGEQ(21, blockDescriptorList.get(0), le.getBlocksNumber());
        assertEquals(21, p.getDocID());

    }

    private void initializePostingListForQueryTerms(HashMap<String, LexiconElem> queryTermsMap, ArrayList<Integer> blocksNumber, Lexicon lexicon, ArrayList<BlockDescriptorList> blockDescriptorList, ArrayList<PostingList> postingLists) {
        int i = 0;
        long firstBlockOffset;
        for (String term : queryTermsMap.keySet()) {
            firstBlockOffset = lexicon.getLexiconElem(term).getOffset();
            blocksNumber.add(lexicon.getLexiconElem(term).getBlocksNumber());
            //read all blocks
            blockDescriptorList.add(new BlockDescriptorList(firstBlockOffset, blocksNumber.get(i),BLOCK_DESCRIPTOR_PATH));
            blockDescriptorList.get(i).openBlock();
            blockDescriptorList.get(i).next();
            //load first posting list for the term
            PostingList postingList = new PostingList();
            postingList.readPostingList(-1, blockDescriptorList.get(i).getNumPosting(), blockDescriptorList.get(i).getPostingListOffset(),POSTING_LIST_PATH);
            postingList.openList();
            postingLists.add(postingList); // add postinglist of the term to postingListIterators
            postingLists.get(i).next();
            i++;
        }
    }

}