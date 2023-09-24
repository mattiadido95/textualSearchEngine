package it.unipi.dii.mircv.prompt.test;

import it.unipi.dii.mircv.index.structures.Document;
import it.unipi.dii.mircv.index.structures.Lexicon;
import it.unipi.dii.mircv.prompt.query.Query;
import it.unipi.dii.mircv.prompt.query.Searcher;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;


public class DaatMaxTest {
    private static final String DOCUMENTS_PATH = "data/index/documents.bin";
    private static final String LEXICON_PATH = "data/index/lexicon.bin";
    private static Lexicon lexicon;
    private static ArrayList<Document> documents;
    private static Searcher searcherdaat;
    private static Searcher searchermax;

    public static void main(String[] args) {
        // Inizializza l'oggetto BlockDescriptor qui, se necessario
        lexicon = new Lexicon();
        lexicon.readLexiconFromDisk(-1, LEXICON_PATH);
        documents = Document.readDocumentsFromDisk(-1, DOCUMENTS_PATH);
        searcherdaat = new Searcher(lexicon, documents);
        searchermax = new Searcher(lexicon, documents);
        queryTest();
    }

    public static void queryTest() {

        ArrayList<Query> queries = new ArrayList<>();
        queries.add(new Query("what is the difference between a 2d and 3d shape",true));
        queries.add(new Query("how long is a cubit?",true));
        queries.add(new Query("what is paranoid sc",true));
        queries.add(new Query("what is parallel structure? why is it so important in the sermon on the mount?",true));
        queries.add(new Query("what is paper with a watermark called",true));
        queries.add(new Query("what is parapsychology?",true));
        queries.add(new Query("what is pressure vessel testing and repair",true));
        queries.add(new Query("what is the gram molecular weight of maltose",true));
        queries.add(new Query("treasure island game for ps3 worth",true));
        queries.add(new Query("where are protists most abundant in humans",true));

        for (Query query : queries) {
            System.out.println(query.getQueryTerms());
            searcherdaat.DAAT(query.getQueryTerms(), 10, "disjunctive", "TFIDF");
            searchermax.maxScore(query.getQueryTerms(), 10, "disjunctive", "TFIDF");
            for (int i = 0; i < searcherdaat.getQueryResults().size(); i++) {
                assertEquals(searcherdaat.getQueryResults().get(i).getDocNo(), searchermax.getQueryResults().get(i).getDocNo());
                assertEquals(searcherdaat.getQueryResults().get(i).getScoring(), searchermax.getQueryResults().get(i).getScoring());
            }
            searcherdaat.DAAT(query.getQueryTerms(), 10, "disjunctive", "BM25");
            searchermax.maxScore(query.getQueryTerms(), 10, "disjunctive", "BM25");
            for (int i = 0; i < searcherdaat.getQueryResults().size(); i++) {
                assertEquals(searcherdaat.getQueryResults().get(i).getDocNo(), searchermax.getQueryResults().get(i).getDocNo());
                assertEquals(searcherdaat.getQueryResults().get(i).getScoring(), searchermax.getQueryResults().get(i).getScoring());
            }
            searcherdaat.DAAT(query.getQueryTerms(), 10, "conjunctive", "TFIDF");
            searchermax.maxScore(query.getQueryTerms(), 10, "conjunctive", "TFIDF");
            for (int i = 0; i < searcherdaat.getQueryResults().size(); i++) {
                assertEquals(searcherdaat.getQueryResults().get(i).getDocNo(), searchermax.getQueryResults().get(i).getDocNo());
                assertEquals(searcherdaat.getQueryResults().get(i).getScoring(), searchermax.getQueryResults().get(i).getScoring());
            }
            searcherdaat.DAAT(query.getQueryTerms(), 10, "conjunctive", "BM25");
            searchermax.maxScore(query.getQueryTerms(), 10, "conjunctive", "BM25");
            for (int i = 0; i < searcherdaat.getQueryResults().size(); i++) {
                assertEquals(searcherdaat.getQueryResults().get(i).getDocNo(), searchermax.getQueryResults().get(i).getDocNo());
                assertEquals(searcherdaat.getQueryResults().get(i).getScoring(), searchermax.getQueryResults().get(i).getScoring());
            }
        }
    }

}
