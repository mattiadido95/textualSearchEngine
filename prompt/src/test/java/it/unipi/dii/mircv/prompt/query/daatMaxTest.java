package it.unipi.dii.mircv.prompt.query;

import it.unipi.dii.mircv.index.structures.Document;
import it.unipi.dii.mircv.index.structures.Lexicon;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;

public class daatMaxTest {
    private static final String DOCUMENTS_PATH = "data/index/documents.bin";
    private static final String LEXICON_PATH = "data/index/lexicon.bin";
    private Lexicon lexicon;
    private ArrayList<Document> documents;
    private Searcher searcherdaat;
    private Searcher searchermax;


    @BeforeAll
    public void setUp() {
        // Inizializza l'oggetto BlockDescriptor qui, se necessario
        lexicon.readLexiconFromDisk(-1, LEXICON_PATH);
        documents = Document.readDocumentsFromDisk(-1, DOCUMENTS_PATH);
        Searcher searcherdaat = new Searcher(lexicon, documents);
        Searcher searchermax = new Searcher(lexicon, documents);
    }

    @Test
    private void addLexiconElem() {

        ArrayList<ArrayList<String>> querys = new ArrayList<>();

        ArrayList<String> query1 = new ArrayList<>(Arrays.asList("what", "is", "the", "difference", "between", "a", "2d", "and", "3d", "shape"));
        ArrayList<String> query2 = new ArrayList<>(Arrays.asList("how", "long", "is", "a", "cubit?"));
        ArrayList<String> query3 = new ArrayList<>(Arrays.asList("what", "is", "paranoid", "sc"));
        ArrayList<String> query4 = new ArrayList<>(Arrays.asList("what", "is", "parallel", "structure?", "why", "is", "it", "so", "important", "in", "the", "sermon", "on", "the", "mount?"));
        ArrayList<String> query5 = new ArrayList<>(Arrays.asList("what", "is", "paper", "with", "a", "watermark", "called"));
        ArrayList<String> query6 = new ArrayList<>(Arrays.asList("what", "is", "pressure", "vessel", "testing", "and", "repair"));
        ArrayList<String> query7 = new ArrayList<>(Arrays.asList("what", "is", "the", "gram", "molecular", "weight", "of", "maltose"));
        ArrayList<String> query8 = new ArrayList<>(Arrays.asList("treasure", "island", "game", "for", "ps3", "worth"));
        ArrayList<String> query9 = new ArrayList<>(Arrays.asList("where", "are", "protists", "most", "abundant", "in", "humans"));
        ArrayList<String> query10 = new ArrayList<>(Arrays.asList("what", "is", "the", "difference", "between", "a", "2d", "and", "3d", "shape"));

        querys.add(query1);
        querys.add(query2);
        querys.add(query3);
        querys.add(query4);
        querys.add(query5);
        querys.add(query6);
        querys.add(query7);
        querys.add(query8);
        querys.add(query9);
        querys.add(query10);

        for (ArrayList<String> query : querys) {
            searcherdaat.DAAT(query, 10, "disjunctive", "BM25");
            searchermax.maxScore(query, 10, "disjunctive", "BM25");
            for (int i = 0; i < 10; i++) {
                assertEquals(searcherdaat.getQueryResults().get(i).getDocNo(), searchermax.getQueryResults().get(i).getDocNo());
                assertEquals(searcherdaat.getQueryResults().get(i).getScoring(), searchermax.getQueryResults().get(i).getScoring());
            }
        }

    }

}
