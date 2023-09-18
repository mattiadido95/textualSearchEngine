package it.unipi.dii.mircv.index.structures;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LexiconTest {
    Lexicon lexicon;

    private static final String POSTING_LIST_PATH = "src/test/data/postingListTest.bin";

    @BeforeEach
    public void setUp() {
        // Inizializza l'oggetto BlockDescriptor qui, se necessario
        lexicon = new Lexicon();
        addLexiconElem("cane");
        addLexiconElem("gatto");
        addLexiconElem("topo");
    }

    private void addLexiconElem(String term) {
        PostingList pl = createPostingList();
        long offset = pl.savePostingListToDisk(-1,POSTING_LIST_PATH);
        LexiconElem elem = new LexiconElem(pl.getPostingListSize(), 0, offset, 0, 0, 0);
        lexicon.getLexicon().put(term, elem);
    }

    private PostingList createPostingList() {
        PostingList pl = new PostingList();
        int num = (int) (Math.random() * 10);
        for (int i = 0; i < 50 - num; i++) {
            Posting p = new Posting(i, i % 6);
            pl.getPostings().add(p);
        }
        return pl;
    }

    @Test
    public void testAddLexiconElem() {
        // Test per verificare che l'aggiunta di elementi al lexicon funzioni correttamente
        // Assicurati di implementare questa logica nel tuo Lexicon
        // Verifica che gli elementi siano stati aggiunti correttamente al lexicon
        assertTrue(lexicon.getLexicon().containsKey("cane"));
        assertTrue(lexicon.getLexicon().containsKey("gatto"));
        assertTrue(lexicon.getLexicon().containsKey("topo"));
    }
}
