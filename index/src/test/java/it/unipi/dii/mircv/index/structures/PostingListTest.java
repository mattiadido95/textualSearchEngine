package it.unipi.dii.mircv.index.structures;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class PostingListTest {

    private static final String INDEX_PATH = "src/test/data/index.bin";
    private static final String LEXICON_PATH = "src/test/data/lexicon.bin";

    private static final String BLOCK_DESCRIPTOR_PATH = "src/test/data/blockDescriptorTest.bin";

    @Test
    public void testPostingListReadWrite() {
        System.out.println("Test PostingList read/write");

        Lexicon lexicon = new Lexicon();
        lexicon.readLexiconFromDisk(-1,LEXICON_PATH);

        LexiconElem le =  lexicon.getLexicon().get("cane");
        PostingList pl = new PostingList();
        pl.readPostingList(-2, le.getDf(), BlockDescriptor.readFirstBlock(le.getOffset(), BLOCK_DESCRIPTOR_PATH).getPostingListOffset(),INDEX_PATH);
        // assert that the posting list is read correctly
        assertEquals(77, pl.getPostings().size());
        for(int i = 0; i < 77; i++) {
            assertEquals(i, pl.getPostings().get(i).getDocID());
            assertEquals(i, pl.getPostings().get(i).getFreq());
        }

        le =  lexicon.getLexicon().get("gatto");
        pl = new PostingList();
        pl.readPostingList(-2, le.getDf(), BlockDescriptor.readFirstBlock(le.getOffset(), BLOCK_DESCRIPTOR_PATH).getPostingListOffset(),INDEX_PATH);
        // assert that the posting list is read correctly
        assertEquals(56, pl.getPostings().size());
        for(int i = 21; i < 77; i++) {
            assertEquals(i, pl.getPostings().get(i-21).getDocID());
            assertEquals(i, pl.getPostings().get(i-21).getFreq());
        }

        le =  lexicon.getLexicon().get("topo");
        pl = new PostingList();
        pl.readPostingList(-2, le.getDf(), BlockDescriptor.readFirstBlock(le.getOffset(), BLOCK_DESCRIPTOR_PATH).getPostingListOffset(),INDEX_PATH);
        // assert that the posting list is read correctly
        assertEquals(44, pl.getPostings().size());
        for(int i = 33; i < 77; i++) {
            assertEquals(i, pl.getPostings().get(i-33).getDocID());
            assertEquals(i, pl.getPostings().get(i-33).getFreq());
        }


    }

    @Test
    public void testNextGEQ(){
        Lexicon lexicon = new Lexicon();
        lexicon.readLexiconFromDisk(-1,LEXICON_PATH);

        LexiconElem le =  lexicon.getLexicon().get("cane");


    }

}