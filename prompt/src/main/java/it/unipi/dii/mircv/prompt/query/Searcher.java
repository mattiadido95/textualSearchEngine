package it.unipi.dii.mircv.prompt.query;

import it.unipi.dii.mircv.index.structures.*;
import it.unipi.dii.mircv.prompt.structure.PostingListIterator;
import it.unipi.dii.mircv.prompt.structure.QueryResult;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;

public class Searcher {

//    private String term;
    private Lexicon lexicon;
    private ArrayList<Document> documents;
    private ArrayList<QueryResult> queryResults;
    private static int N_docs = 0; // number of documents in the collection

//    public Searcher(Lexicon lexicon, ArrayList<Document> documents) {
//        // TODO cosi carico ma poi passo le strutture in copia al searcher e occupo il doppio !!!!
//        this.lexicon = lexicon;
//        this.documents = documents;
//    }

    public Searcher() {
        queryResults = new ArrayList<>();
        //read number of docs from disk
        try (FileInputStream fileIn = new FileInputStream("data/index/numberOfDocs.bin");
             ObjectInputStream in = new ObjectInputStream(fileIn)) {
            N_docs = (int) in.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<QueryResult> getQueryResults() {
        return queryResults;
    }

//    public ArrayList<String> search(ArrayList<String> queryTerms, Lexicon lexicon, ArrayList<Document> documents) {
//        ArrayList<String> pid_results = new ArrayList<>();
//        for (String term : queryTerms) {
//            pid_results.addAll(searchTerm(term, lexicon, documents));
//        }
//        return pid_results;
//    }
//
//    public ArrayList<String> searchTerm(String term, Lexicon lexicon, ArrayList<Document> documents) {
//        this.term = term;
//        ArrayList<String> term_pid_results = new ArrayList<>();
//        if (lexicon.getLexicon().containsKey(term)) {
//            System.out.println("Term " + term + " found at offset " + lexicon.getLexiconElem(term).getOffset());
//            // get lexicon element
//            LexiconElem lexiconElem = lexicon.getLexiconElem(term);
//            // use lexiconElem.offset and lexiconElem.df to read posting list from index.bin
//            // get posting list
//            PostingList postingList = new PostingList();
//            postingList.readPostingList(-1, lexiconElem.getDf(), lexiconElem.getOffset());
//            // for each posting in posting list get document pid
//            for (Posting posting : postingList.getPostings()) {
//                // get document
//                Document document = documents.get(posting.getDocID());
//                // get document pid
//                String pid = document.getDocNo();
//                // add pid to results
//                term_pid_results.add(pid);
//            }
//        } else {
//            System.out.println("Term " + term + " not found in lexicon");
//        }
//
//        return term_pid_results;
//    }

    public void DAAT(ArrayList<String> queryTerms, Lexicon lexicon, ArrayList<Document> documents, int K, String mode) {
        queryResults.clear();
        //create postingListIterator
        PostingListIterator postingListIterator = new PostingListIterator();
        ArrayList<Integer> counter = new ArrayList<>();

        // populate postingListIterator with offset and df for each term in query
        for (String term : queryTerms) {
            if (lexicon.getLexicon().containsKey(term)) {
                postingListIterator.addOffset(lexicon.getLexiconElem(term).getOffset());
                postingListIterator.addDf(lexicon.getLexiconElem(term).getDf());
                counter.add(lexicon.getLexiconElem(term).getDf());
            }
        }
        if (postingListIterator.getCursor().size() == 0)
            return; // if no terms in query are in lexicon means that there are no results
        postingListIterator.openList(); // there are results so open the postingList

        int next_docId;

        do {
            ArrayList<Double> scores = new ArrayList<>();
            //get next docId
            next_docId = getNextDocId(postingListIterator, counter);
            if (next_docId == Integer.MAX_VALUE)
                break;
            double document_score = 0;
            int term_counter = 0;

            for (int i = 0; i < postingListIterator.getCursor().size(); i++) {
                int docId = postingListIterator.getDocId(i);
                if (docId == next_docId) {
                    int tf = postingListIterator.getFreq(i);
                    postingListIterator.next(i);
                    counter.set(i, counter.get(i) - 1);
                    scores.add(tfidf(tf, postingListIterator.getDf().get(i)));
                    term_counter++;
                }
            }

            if (mode.equals("conjunctive") && term_counter != queryTerms.size())
                scores.clear();

            //sum all the value of scores
            for (double score : scores) {
                document_score += score;
            }
            if (document_score > 0) {
                // get document
                Document document = documents.get(next_docId);
                // get document pid
                String pid = document.getDocNo();
                // add pid to results
                queryResults.add(new QueryResult(pid, document_score));
            }
        } while (next_docId != Integer.MAX_VALUE);

        postingListIterator.closeList();
        Collections.sort(queryResults);
        if (queryResults.size() > K) {
            queryResults = new ArrayList<>(queryResults.subList(0, K));;
        }
    }

    private double tfidf(int tf, int df) {
        double score = 0;
        if (tf > 0)
            score = (1 + Math.log(tf)) * Math.log(N_docs / df);
        return score;
    }

    private int getNextDocId(PostingListIterator pli, ArrayList<Integer> counter) {
        int min = Integer.MAX_VALUE;
        for (int i = 0; i < pli.getCursor().size(); i++) {
            if (counter.get(i) == 0) //posting list finished
                continue;
            int id = pli.getDocId(i);
            if (id == -1)
                continue;
            if (id < min)
                min = id;
        }
        return min;
    }

    public void printResults(long time) {
            if(queryResults == null || queryResults.size() == 0){
                System.out.println("Unfortunately, no documents were found for your query.");
                return;
            }

            System.out.println("These " + queryResults.size() + " documents may are of your interest");
            System.out.println(queryResults);
            System.out.println("Search time: " + time + " ms");
        }

}
