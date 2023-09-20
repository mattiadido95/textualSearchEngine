package it.unipi.dii.mircv.prompt;

import it.unipi.dii.mircv.index.preprocessing.Preprocessing;
import it.unipi.dii.mircv.index.structures.Document;
import it.unipi.dii.mircv.index.structures.Lexicon;
import it.unipi.dii.mircv.prompt.query.Query;
import it.unipi.dii.mircv.prompt.query.Searcher;

import java.awt.datatransfer.StringSelection;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;


import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

public class daatMaxTest {
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

        ArrayList<String> queriess = new ArrayList<>(Arrays.asList(
                "what is the difference between a 2d and 3d shape",
                "how long is a cubit?",
                "what is paranoid sc",
                "what is parallel structure? why is it so important in the sermon on the mount?",
                "what is paper with a watermark called",
                "what is pressure vessel testing and repair",
                "what is the gram molecular weight of maltose",
                "treasure island game for ps3 worth",
                "where are protists most abundant in humans",
                "what is the difference between a 2d and 3d shape"
        ));

        for (String query : queriess) {
            Query q = new Query(query);
            ArrayList<String> terms = (ArrayList<String>) q.getQueryTerms();
            searcherdaat.DAAT(terms, 10, "disjunctive", "TFIDF");
            searchermax.maxScore(terms, 10, "disjunctive", "TFIDF");
            System.out.println("Query: " + terms);
            for (int i = 0; i < searcherdaat.getQueryResults().size(); i++) {
                assertEquals(searcherdaat.getQueryResults().get(i).getDocNo(), searchermax.getQueryResults().get(i).getDocNo());
                assertEquals(searcherdaat.getQueryResults().get(i).getScoring(), searchermax.getQueryResults().get(i).getScoring());
            }
        }

        ArrayList<Query> queries = new ArrayList<>();
        queries.add(new Query("what is the difference between a 2d and 3d shape"));
        queries.add(new Query("how long is a cubit?"));
        queries.add(new Query("what is paranoid sc"));
        queries.add(new Query("what is parallel structure? why is it so important in the sermon on the mount?"));
        queries.add(new Query("what is paper with a watermark called"));
        queries.add(new Query("what is parapsychology?"));
        queries.add(new Query("what is parapsychology?"));
        queries.add(new Query("what is pressure vessel testing and repair"));
        queries.add(new Query("what is the gram molecular weight of maltose"));
        queries.add(new Query("treasure island game for ps3 worth"));
        queries.add(new Query("where are protists most abundant in humans"));

        for (Query query : queries) {
            System.out.println(query.getQueryTerms());
            searcherdaat.DAAT(query.getQueryTerms(), 10, "disjunctive", "TFIDF");
            searchermax.maxScore(query.getQueryTerms(), 10, "disjunctive", "TFIDF");
            for (int i = 0; i < searcherdaat.getQueryResults().size(); i++) {
                assertEquals(searcherdaat.getQueryResults().get(i).getDocNo(), searchermax.getQueryResults().get(i).getDocNo());
                assertEquals(searcherdaat.getQueryResults().get(i).getScoring(), searchermax.getQueryResults().get(i).getScoring());
            }
        }

    }

}
