package it.unipi.dii.mircv.prompt.dynamicPruning;

import it.unipi.dii.mircv.index.structures.Document;
import it.unipi.dii.mircv.index.structures.Lexicon;
import it.unipi.dii.mircv.prompt.query.Searcher;
import java.io.File;
import java.util.ArrayList;


/**
 * This class is used to compute the TUB scores for each term in the lexicon.
 * The TUB score is computed using the DAAT algorithm with a single term query.
 * The score function used is the one specified in the input.
 * The lexicon is updated with the new TUB scores.
 */
public class DynamicPruning {
    private Lexicon lexicon;
    private Searcher searcher;
    private ArrayList<String> queryTerms;
    private String LEXICON_PATH = "data/index/lexicon.bin";

    /**
     * Constructor
     * @param lexicon is the lexicon to be updated
     * @param documents are the documents used to compute the TUB scores
     */
    public DynamicPruning(Lexicon lexicon, ArrayList<Document> documents) {
        this.lexicon = lexicon;
        this.searcher = new Searcher(lexicon, documents);
        this.queryTerms = new ArrayList<>(this.lexicon.getLexiconKeys());
    }

    /**
     * This method computes the TUB scores for each term in the lexicon.
     * The score function used is the one specified in the input.
     * The lexicon is updated with the new TUB scores.
     * @param scoreFunction is the score function used to compute the TUB scores
     */
    public void TUB_processing(String scoreFunction) {
        for (String term : this.queryTerms) {
            ArrayList<String> faketerm = new ArrayList<>();
            faketerm.add(term); // used to call the search function, made for compatibility
            if (scoreFunction.equals("BM25")) {
                this.searcher.DAAT(faketerm, 1, "disjunctive", "BM25");
                this.lexicon.getLexiconElem(term).setTUB_bm25(this.searcher.getQueryResults().get(0).getScoring());
            } else if (scoreFunction.equals("TFIDF")) {
                this.searcher.DAAT(faketerm, 1, "disjunctive", "TFIDF");
                this.lexicon.getLexiconElem(term).setTUB_tfidf(this.searcher.getQueryResults().get(0).getScoring());
            } else {
                System.out.println("Wrong score function");
                return;
            }
        }
        // delete lexicon.bin file
        File file = new File("data/index/lexicon.bin");
        file.delete();
        // save the updated lexicon to disk
        this.lexicon.saveLexiconToDisk(-1, LEXICON_PATH);
    }
}
