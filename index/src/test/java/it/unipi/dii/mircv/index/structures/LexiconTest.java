package it.unipi.dii.mircv.index.structures;

import org.junit.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;

import java.util.TreeMap;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LexiconTest {
    private TreeMap<String, LexiconElem> lexicon;

    @BeforeAll
    public void setUp() {
        // Inizializza l'oggetto BlockDescriptor qui, se necessario
        this.lexicon = new TreeMap<>();
    }

    @Test
    public void testLexicon() {
//        new LexiconTest();
        addLexiconElem("cane");
        addLexiconElem("gatto");
        addLexiconElem("topo");
    }

    public LexiconTest() {
    }

    private void addLexiconElem(String term) {
        PostingList pl = createPostinList();
        long offset = pl.savePostingListToDisk(-2);
        LexiconElem elem = new LexiconElem(pl.getPostingListSize(), 0, offset, 0, 0, 0);
        this.lexicon.put(term, elem);
    }

    private PostingList createPostinList() {
        PostingList pl = new PostingList();
        Random random = new Random();
        // Genera un numero casuale tra 0 (incluso) e 11 (escluso)
        int num = random.nextInt(6);
        int previous = 0;
        for (int i = 0; i < 50 - num; i++) {
            Posting p = new Posting(previous + random.nextInt(8) + 1, i % 6);
            pl.getPostings().add(p);
            previous = p.getDocID();
        }
        return pl;
    }


}
