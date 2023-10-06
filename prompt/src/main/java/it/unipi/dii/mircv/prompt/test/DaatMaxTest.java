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
        queries.add(new Query("what is the difference between a 2d and 3d shape", true));
        queries.add(new Query("how long is a cubit?", true));
        queries.add(new Query("what is paranoid sc", true));
        queries.add(new Query("what is parallel structure? why is it so important in the sermon on the mount?", true));
        queries.add(new Query("what is paper with a watermark called", true));
        queries.add(new Query("what is parapsychology?", true));
        queries.add(new Query("what is pressure vessel testing and repair", true));
        queries.add(new Query("what is the gram molecular weight of maltose", true));
        queries.add(new Query("treasure island game for ps3 worth", true));
        queries.add(new Query("where are protists most abundant in humans", true));
        queries.add(new Query("most frequent number in powerball", true));
        queries.add(new Query("tv advertising, what is vcr, stats", true));
        queries.add(new Query("what is pseudoarthrosis thoracic spine", true));
        queries.add(new Query("what is pseudocode", true));
        queries.add(new Query("who sang louie louie louie louie", true));
        queries.add(new Query("how much does it cost for a breast augmentation and brazilian butt lift", true));
        queries.add(new Query("tv schedule archive disney channel", true));
        queries.add(new Query("tv series benson cast", true));
        queries.add(new Query("tv series coach cast", true));
        queries.add(new Query("cost of an average wedding cake", true));
        queries.add(new Query("what is natural history", true));
        queries.add(new Query("what is natural gas solutions", true));
        queries.add(new Query("cost of an average patio slab per sq ft", true));
        queries.add(new Query("what is natural gas solutions", true));
        queries.add(new Query("what training should be done annually for employees", true));
        queries.add(new Query("what is archway publishing", true));
        queries.add(new Query("what trait does collagen control", true));
        queries.add(new Query("define incidence matrix", true));
        queries.add(new Query("define incision and drainage", true));
        queries.add(new Query("define inclusive environment", true));
        queries.add(new Query("how to use a king size headboard on a california king", true));
        queries.add(new Query("when did congress pass the wherry bill", true));
        queries.add(new Query("what does rescind mean on insurance policy", true));
        queries.add(new Query("what county is gatlinburg in", true));
        queries.add(new Query("what does research say about a child that is constantly disappointed by the parent", true));
        queries.add(new Query("what transponder for direct tv", true));
        queries.add(new Query("how to use a posey pillow backdrop stand", true));
        queries.add(new Query("what transport molecule transports oxygen", true));
        queries.add(new Query("travel guide to puerto rico visa requirements", true));
        queries.add(new Query("how long is a cats gestational period", true));
        queries.add(new Query("what is prenuptial", true));
        queries.add(new Query("what is preoperative clearance", true));
        queries.add(new Query("travel trailer fall off block damage", true));
        queries.add(new Query("what is pehlwani", true));
        queries.add(new Query("what is pegging in trade", true));
        queries.add(new Query("what are the causes of the pinky and ring finger to lock in a curl", true));
        queries.add(new Query("what is the origin of the flexor carpi radialis", true));
        queries.add(new Query("what is prepared mustard vs ground mustard", true));
        queries.add(new Query("what is edta lab test", true));
        queries.add(new Query("who plays sam in lmn", true));
        queries.add(new Query("what is peg ratio example", true));
        queries.add(new Query("liter chemistry definition", true));
        queries.add(new Query("what is preppy k", true));
        queries.add(new Query("what is peer review testing", true));
        queries.add(new Query("what is peekaboo", true));
        queries.add(new Query("price of copper by ounce, pound", true));
        queries.add(new Query("trazodone for dogs side effects", true));
        queries.add(new Query("treadmill incline meaning", true));


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
