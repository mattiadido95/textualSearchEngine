package it.unipi.dii.mircv.prompt.test;

import it.unipi.dii.mircv.index.preprocessing.Preprocessing;
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
        Preprocessing preprocessing = new Preprocessing();
        ArrayList<Query> queries = new ArrayList<>();
        queries.add(new Query(true, preprocessing).setQuery("what is the difference between a 2d and 3d shape"));
        queries.add(new Query(true, preprocessing).setQuery("how long is a cubit?"));
        queries.add(new Query(true, preprocessing).setQuery("what is paranoid sc"));
        queries.add(new Query(true, preprocessing).setQuery("what is parallel structure? why is it so important in the sermon on the mount?"));
        queries.add(new Query(true, preprocessing).setQuery("what is paper with a watermark called"));
        queries.add(new Query(true, preprocessing).setQuery("what is parapsychology?"));
        queries.add(new Query(true, preprocessing).setQuery("what is pressure vessel testing and repair"));
        queries.add(new Query(true, preprocessing).setQuery("what is the gram molecular weight of maltose"));
        queries.add(new Query(true, preprocessing).setQuery("treasure island game for ps3 worth"));
        queries.add(new Query(true, preprocessing).setQuery("where are protists most abundant in humans"));
        queries.add(new Query(true, preprocessing).setQuery("most frequent number in powerball"));
        queries.add(new Query(true, preprocessing).setQuery("tv advertising, what is vcr, stats"));
        queries.add(new Query(true, preprocessing).setQuery("what is pseudoarthrosis thoracic spine"));
        queries.add(new Query(true, preprocessing).setQuery("what is pseudocode"));
        queries.add(new Query(true, preprocessing).setQuery("who sang louie louie louie louie"));
        queries.add(new Query(true, preprocessing).setQuery("how much does it cost for a breast augmentation and brazilian butt lift"));
        queries.add(new Query(true, preprocessing).setQuery("tv schedule archive disney channel"));
        queries.add(new Query(true, preprocessing).setQuery("tv series benson cast"));
        queries.add(new Query(true, preprocessing).setQuery("tv series coach cast"));
        queries.add(new Query(true, preprocessing).setQuery("cost of an average wedding cake"));
        queries.add(new Query(true, preprocessing).setQuery("what is natural history"));
        queries.add(new Query(true, preprocessing).setQuery("what is natural gas solutions"));
        queries.add(new Query(true, preprocessing).setQuery("cost of an average patio slab per sq ft"));
        queries.add(new Query(true, preprocessing).setQuery("what is natural gas solutions"));
        queries.add(new Query(true, preprocessing).setQuery("what training should be done annually for employees"));
        queries.add(new Query(true, preprocessing).setQuery("what is archway publishing"));
        queries.add(new Query(true, preprocessing).setQuery("what trait does collagen control"));
        queries.add(new Query(true, preprocessing).setQuery("define incidence matrix"));
        queries.add(new Query(true, preprocessing).setQuery("define incision and drainage"));
        queries.add(new Query(true, preprocessing).setQuery("define inclusive environment"));
        queries.add(new Query(true, preprocessing).setQuery("how to use a king size headboard on a california king"));
        queries.add(new Query(true, preprocessing).setQuery("when did congress pass the wherry bill"));
        queries.add(new Query(true, preprocessing).setQuery("what does rescind mean on insurance policy"));
        queries.add(new Query(true, preprocessing).setQuery("what county is gatlinburg in"));
        queries.add(new Query(true, preprocessing).setQuery("what does research say about a child that is constantly disappointed by the parent"));
        queries.add(new Query(true, preprocessing).setQuery("what transponder for direct tv"));
        queries.add(new Query(true, preprocessing).setQuery("how to use a posey pillow backdrop stand"));
        queries.add(new Query(true, preprocessing).setQuery("what transport molecule transports oxygen"));
        queries.add(new Query(true, preprocessing).setQuery("travel guide to puerto rico visa requirements"));
        queries.add(new Query(true, preprocessing).setQuery("how long is a cat's gestational period"));
        queries.add(new Query(true, preprocessing).setQuery("what is prenuptial"));
        queries.add(new Query(true, preprocessing).setQuery("what is preoperative clearance"));
        queries.add(new Query(true, preprocessing).setQuery("travel trailer fall off block damage"));
        queries.add(new Query(true, preprocessing).setQuery("what is pehlwani"));
        queries.add(new Query(true, preprocessing).setQuery("what is pegging in trade"));
        queries.add(new Query(true, preprocessing).setQuery("what are the causes of the pinky and ring finger to lock in a curl"));
        queries.add(new Query(true, preprocessing).setQuery("what is the origin of the flexor carpi radialis"));
        queries.add(new Query(true, preprocessing).setQuery("what is prepared mustard vs ground mustard"));
        queries.add(new Query(true, preprocessing).setQuery("what is edta lab test"));
        queries.add(new Query(true, preprocessing).setQuery("who plays sam in lmn"));
        queries.add(new Query(true, preprocessing).setQuery("what is peg ratio example"));
        queries.add(new Query(true, preprocessing).setQuery("liter chemistry definition"));
        queries.add(new Query(true, preprocessing).setQuery("what is preppy k"));
        queries.add(new Query(true, preprocessing).setQuery("what is peer review testing"));
        queries.add(new Query(true, preprocessing).setQuery("what is peekaboo"));
        queries.add(new Query(true, preprocessing).setQuery("price of copper by ounce, pound"));
        queries.add(new Query(true, preprocessing).setQuery("trazodone for dogs side effects"));
        queries.add(new Query(true, preprocessing).setQuery("treadmill incline meaning"));

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
