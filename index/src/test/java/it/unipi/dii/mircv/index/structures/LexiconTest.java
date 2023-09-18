package it.unipi.dii.mircv.index.structures;

import org.junit.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.*;

import java.util.TreeMap;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LexiconTest {
    TreeMap<String, LexiconElem> lexicon;

    @BeforeAll
    public void setUp() {
        // Inizializza l'oggetto BlockDescriptor qui, se necessario
        this.lexicon = new TreeMap<String, LexiconElem>();
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
        int num = (int) (Math.random() * 10);
        for (int i = 0; i < 50 - num; i++) {
            Posting p = new Posting(i, i % 6);
            pl.getPostings().add(p);
        }
        return pl;
    }

    @Test
    public void testLexicon() {
        new LexiconTest();
        addLexiconElem("cane");
        addLexiconElem("gatto");
        addLexiconElem("topo");
    }

}
