package it.unipi.dii.mircv.prompt.dynamicPruning;

import it.unipi.dii.mircv.index.structures.Document;
import it.unipi.dii.mircv.index.structures.Lexicon;
import it.unipi.dii.mircv.prompt.query.Searcher;

import java.io.File;
import java.util.ArrayList;

public class DynamicPruning {
    private Lexicon lexicon;
    private ArrayList<Document> documents;
    private Searcher searcher;
    private ArrayList<String> queryTerms;
    private String LEXICON_PATH = "data/index/lexicon.bin";

    public DynamicPruning(Lexicon lexicon, ArrayList<Document> documents) {
        this.documents = documents;
        this.lexicon = lexicon;
        this.searcher = new Searcher(lexicon, documents);
        this.queryTerms = new ArrayList<>(this.lexicon.getLexiconKeys());
    }

    public void TUB_processing(String scoreFunction) {
        // TODO da testare

//        int counter = 0;
        for (String term : this.queryTerms) {
            ArrayList<String> faketerm = new ArrayList<>();
            faketerm.add(term); // used to call the search function, made for compatibility
            System.out.println(term);
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
            if (term.equals("war")){
                System.out.println(this.lexicon.getLexiconElem(term).getTUB_bm25());
                System.out.println(this.lexicon.getLexiconElem(term).getTUB_tfidf());
            }
//            if (counter == 10) {
//                System.out.println("TUB processing completed");
//                System.out.println("TUB processing results: ");
//                for (QueryResult queryResult : queryResults) {
//                    System.out.println(queryResult);
//                }
//                break;
//            }
//            counter++;
        }
        // delete lexicon.bin file
        File file = new File("data/index/lexicon.bin");
        file.delete();
        this.lexicon.saveLexiconToDisk(-1,LEXICON_PATH);
    }

    public void DUB_processing(String scoreFunctino) {

    }
}
