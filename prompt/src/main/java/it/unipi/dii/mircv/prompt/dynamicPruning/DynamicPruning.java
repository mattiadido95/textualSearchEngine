package it.unipi.dii.mircv.prompt.dynamicPruning;

import it.unipi.dii.mircv.index.structures.BlockDescriptor;
import it.unipi.dii.mircv.index.structures.Document;
import it.unipi.dii.mircv.index.structures.Lexicon;
import it.unipi.dii.mircv.index.structures.PostingList;
import it.unipi.dii.mircv.prompt.query.Searcher;
import it.unipi.dii.mircv.prompt.structure.QueryResult;

import java.util.ArrayList;

import static it.unipi.dii.mircv.index.structures.BlockDescriptor.readFirstBlock;

public class DynamicPruning {
    private Lexicon lexicon;
    private ArrayList<Document> documents;
    private Searcher searcher;
    private ArrayList<String> queryTerms;


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
        this.lexicon.saveLexiconToDisk(-1);
    }

    public void DUB_processing(String scoreFunctino) {

    }
}
