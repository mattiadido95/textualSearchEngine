package it.unipi.dii.mircv.index.structures;

import it.unipi.dii.mircv.index.algorithms.Merger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;

import java.util.TreeMap;

public class LexiconTest {
    Lexicon lexicon;
    private static final Integer NUMBER_OF_POSTING = 10;
    private static final Integer BLOCK_POSTING_LIST_SIZE = (4 * 2) * NUMBER_OF_POSTING; // 4 byte per docID, 4 byte per freq and postings
    private static final String POSTING_LIST_PATH = "src/test/data/postingListTest.bin";
    private static final String BLOCK_DESCRIPTOR_PATH = "src/test/data/blockDescriptorTest.bin";
    private static final String LEXICON_PATH = "src/test/data/lexicon.bin";

    @BeforeEach
    public void setUp() {
        // Inizializza l'oggetto BlockDescriptor qui, se necessario
        lexicon = new Lexicon();
        addLexiconElem("cane"); // doc ID [0, 76]
        addLexiconElem("gatto"); // doc ID [21, 76]
        addLexiconElem("topo"); // doc ID [33, 76]
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
                init = 21;
                break;
            case "topo":
                init = 33;
                break;
        }
        for (int i = init; i < 77; i++) {
            Posting p = new Posting(i, i);
            pl.getPostings().add(p);
        }
         pl.getPostings().add(new Posting(100,100));
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
