package it.unipi.dii.mircv.index.structures;

import it.unipi.dii.mircv.index.algorithms.Merger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;

import java.util.TreeMap;

public class LexiconTest {
    Lexicon lexicon = new Lexicon();


    @BeforeEach
    public void setUp() {
        // Inizializza l'oggetto BlockDescriptor qui, se necessario
        addLexiconElem("cane"); // doc ID [0, 76]
        addLexiconElem("gatto"); // doc ID [21, 76]
        addLexiconElem("topo"); // doc ID [33, 76]
    }

    private void addLexiconElem(String term) {
        PostingList pl = createPostinList(term);
        LexiconElem elem = new LexiconElem(pl.getPostingListSize(), 0, 0, 0, 0, 0);
        int blocks = Merger.saveBlockPosting(pl, elem, true);
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
        return pl;
    }

    @Test
    public void testAddLexiconElem() {
        lexicon.saveLexiconToDisk(-2);
        lexicon = new Lexicon();
        lexicon.readLexiconFromDisk(-2);
        // Test per verificare che l'aggiunta di elementi al lexicon funzioni correttamente
        // Assicurati di implementare questa logica nel tuo Lexicon
        // Verifica che gli elementi siano stati aggiunti correttamente al lexicon
        assertTrue(lexicon.getLexicon().containsKey("cane"));
        assertTrue(lexicon.getLexicon().containsKey("gatto"));
        assertTrue(lexicon.getLexicon().containsKey("topo"));
    }
}
