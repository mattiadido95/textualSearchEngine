package it.unipi.dii.mircv.index.structures;

import it.unipi.dii.mircv.index.algorithms.Merger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;

import java.util.TreeMap;

public class LexiconTest {
    Lexicon lexicon;
    private static final int POSTING_SIZE = (4 * 2); // 4 byte per docID, 4 byte per freq into one posting
    private static final int BLOCK_DESCRIPTOR_SIZE = (4 * 2 + 8); // 4 byte per docID, 4 byte per freq, 8 byte per offset
    private static final String POSTING_LIST_PATH = "src/test/data/postingListTest.bin";
    private static final String BLOCK_DESCRIPTOR_PATH = "src/test/data/blockDescriptorTest.bin";
    private static final String LEXICON_PATH = "src/test/data/lexicon.bin";

    @BeforeEach
    public void setUp() {
        // Inizializza l'oggetto BlockDescriptor qui, se necessario
        lexicon = new Lexicon();
        addLexiconElem("cane"); // doc ID [0, 588]
        addLexiconElem("gatto"); // doc ID [121, 588]
        addLexiconElem("topo"); // doc ID [233, 588]
    }

    private void addLexiconElem(String term) {
        PostingList pl = createPostinList(term);
        LexiconElem elem = new LexiconElem(pl.getPostingListSize(), 0, 0, 0, 0, 0);
        int blocks = saveBlockPosting(pl, elem);
        elem.setNumBlock(blocks);
        lexicon.getLexicon().put(term, elem);

    }

    private PostingList createPostinList(String term) {
        PostingList pl = new PostingList();
        int init = -1;
        switch (term) {
            case "cane":
                init = 0;
                break;
            case "gatto":
                init = 121;
                break;
            case "topo":
                init = 233;
                break;
        }
        for (int i = init; i < 589; i++) {
            Posting p = new Posting(i, i);
            pl.getPostings().add(p);
        }
//        pl.getPostings().add(new Posting(100, 100));
        return pl;
    }

    @Test
    public void testAddLexiconElem() {
        lexicon.saveLexiconToDisk(-1, LEXICON_PATH);
        lexicon = new Lexicon();
        lexicon.readLexiconFromDisk(-1, LEXICON_PATH);
        // Test per verificare che l'aggiunta di elementi al lexicon funzioni correttamente
        // Assicurati di implementare questa logica nel tuo Lexicon
        // Verifica che gli elementi siano stati aggiunti correttamente al lexicon
        assertTrue(lexicon.getLexicon().containsKey("cane"));
        assertTrue(lexicon.getLexicon().containsKey("gatto"));
        assertTrue(lexicon.getLexicon().containsKey("topo"));
    }

    public static int saveBlockPosting(PostingList mergePostingList, LexiconElem newLexiconElem) {
        // scrittura newPostingList nel file index
        long postingOffsetStart = mergePostingList.savePostingListToDisk(-1, POSTING_LIST_PATH);

        //scorri la newPostingList e ogni NUMBER_OF_POSTING elementi salva il block descriptor
        BlockDescriptor blockDescriptor;
        int blockCounter = 0;
        long blockDescriptorOffset;
        int numBlocks = mergePostingList.getPostingListSize() > 200 ? (int) Math.sqrt(mergePostingList.getPostingListSize()) : 1; // get number of block in which the posting list will be divided;
        int numPostingInBlock = (int) Math.ceil(mergePostingList.getPostingListSize() / (double) numBlocks); // get number of posting in each block

        for (int i = 0; i < mergePostingList.getPostingListSize(); i++) {
            if ((i + 1) % numPostingInBlock == 0) {
                //salva il block descriptor
                blockDescriptor = new BlockDescriptor(postingOffsetStart, mergePostingList.getPostings().subList(i + 1 - numPostingInBlock, i + 1));
                postingOffsetStart += numPostingInBlock * POSTING_SIZE;
                blockDescriptorOffset = blockDescriptor.saveBlockDescriptorToDisk(BLOCK_DESCRIPTOR_PATH);
                if (blockCounter == 0)
                    //salva inzio del block descriptor nel newLexiconElem
                    newLexiconElem.setOffset(blockDescriptorOffset);
                blockCounter++;
            } else if ((mergePostingList.getPostingListSize() - (blockCounter * numPostingInBlock)) < numPostingInBlock) {
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

        if (blockCounter != numBlocks)
            System.out.println("Error in saveBlockPosting: blockCounter != numBlocks");

        return numBlocks;

    }
}
