package it.unipi.dii.mircv.prompt.query;

import it.unipi.dii.mircv.index.structures.*;
import it.unipi.dii.mircv.prompt.structure.PostingListIterator;
import it.unipi.dii.mircv.prompt.structure.QueryResult;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import static it.unipi.dii.mircv.index.structures.BlockDescriptor.readBlockDescriptorList;
import static it.unipi.dii.mircv.index.structures.BlockDescriptor.readFirstBlock;

public class Searcher {

    //    private String term;
    private Lexicon lexicon;
    private ArrayList<Document> documents;
    private ArrayList<QueryResult> queryResults;
    private ArrayList<String> previousQueryTerms;
    private ArrayList<Iterator<BlockDescriptor>> blockDescriptorIterators = null;
    private ArrayList<PostingList> postingLists = null;
    private String previousMode;
    private double AVG_DOC_LENGTH;
    private static int N_docs = 0; // number of documents in the collection


    private static final int NUMBER_OF_POSTING = 10;
    private static final int BLOCK_POSTING_LIST_SIZE = (4 * 2) * NUMBER_OF_POSTING; // 4 byte per docID, 4 byte per freq and postings


    public Searcher() {
        this.queryResults = new ArrayList<>();
        this.blockDescriptorIterators = new ArrayList<>();
        this.postingLists = new ArrayList<>();
        this.previousQueryTerms = new ArrayList<>();
        this.previousMode = "";
        //read number of docs from disk
        try (FileInputStream fileIn = new FileInputStream("data/index/documentInfo.bin");
             ObjectInputStream in = new ObjectInputStream(fileIn)) {
            this.N_docs = (int) in.readObject();
            long totDocLength = (long) in.readObject();
            AVG_DOC_LENGTH = totDocLength / N_docs;
            System.out.println(N_docs);
            System.out.println(AVG_DOC_LENGTH);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // search min docID in the posting list iterator array
//    private int getMinDocId(ArrayList<Iterator<Posting>> postingListIterators) {
//        int min = Integer.MAX_VALUE;
//        for (Iterator<Posting> postingListIterator : postingListIterators) {
//            if (postingListIterator.hasNext()) {
//                int id = postingListIterator.next().getDocID();
//                if (id < min)
//                    min = id;
//            }
//        }
//        return min;
//    }

//    private ArrayList<BlockDescriptor> openBlocks(long firstBlockoffset, Integer blocksNumber) {
//
//        ArrayList<BlockDescriptor> blocks = new ArrayList<>();
//        blocks = readBlockDescriptorList(firstBlockoffset, blocksNumber);
//
//        return blocks;
//    }

    public void DAAT_block(ArrayList<String> queryTerms, Lexicon lexicon, ArrayList<Document> documents, int K, String mode, String scoringFunction) {
        //TODO INTRODOTTO IL PARAMETRO SCORE PER LA SCORING FUNCTION quindi cambiare condizioni dell'if
        if ((this.previousQueryTerms.equals(queryTerms) && this.previousMode.equals(mode))
                || (this.previousQueryTerms.equals(queryTerms) && queryTerms.size() == 1)) // same query as before
            return;
        //process query and clear previous results
        this.previousQueryTerms = queryTerms;
        this.previousMode = mode;
        this.queryResults.clear();

        long firstBlockOffset;
        int blocksNumber;
        int minDocId;
        ArrayList<Integer> indexes = new ArrayList<>();
        ArrayList<Double> scores = new ArrayList<>();

        // for each term in query get all block descriptors and add them to blockDescriptorIterators
        for (String term : queryTerms) {
            if (lexicon.getLexicon().containsKey(term)) {
                firstBlockOffset = lexicon.getLexiconElem(term).getOffset();
                BlockDescriptor firstBlockDescriptor = readFirstBlock(firstBlockOffset); // read first block descriptor, used because MaxScore is not implemented
//                blocksNumber = lexicon.getLexiconElem(term).getBlocksNumber();
//                blockDescriptorIterators.add(openBlocks(firstBlockOffset, blocksNumber).iterator());
                // load total posting list for the term, used because MaxScore is not implemented
                PostingList postingList = new PostingList();
                postingList.readPostingList(-1, lexicon.getLexiconElem(term).getDf(), firstBlockDescriptor.getPostingListOffset());
                postingList.openList();
                postingLists.add(postingList); // add postinglist of the term to postingListIterators

                // print posting list size
//                System.out.println("Posting list size: " + postingList.getPostingListSize());

            }else{
                // if term is not in lexicon add empty posting list
                postingLists.add(null);
            }
        }

        if (postingLists.size() == 0)
            return; // if no terms in query are in lexicon means that there are no results

        // get min docID from posting list iterators and indexes of posting list iterators with min docID
        minDocId = getNextDocId(indexes);

        do {
            double document_score = 0;
            int term_counter = 0;

            for (Integer i : indexes) {
                //calculate score for posting list with min docID

                if(scoringFunction.equals("TFIDF"))
                    scores.add(tfidf(postingLists.get(i).getFreq(), lexicon.getLexiconElem(queryTerms.get(i)).getDf()));
                else if(scoringFunction.equals("BM25"))
                    scores.add(BM25(postingLists.get(i).getFreq(), lexicon.getLexiconElem(queryTerms.get(i)).getDf(), documents.get(minDocId).getLength(), AVG_DOC_LENGTH));
                term_counter++;
                // get next posting from posting list with min docID
                postingLists.get(i).next();
            }

            if (mode.equals("conjunctive") && term_counter != queryTerms.size())
                scores.clear();

            // Sum all the values of scores
            for (double score : scores) {
                document_score += score;
            }
            if (document_score > 0) {
                // Get document
                Document document = documents.get(minDocId);
                // Get document pid
                String pid = document.getDocNo();
                // Add pid to results
                queryResults.add(new QueryResult(pid, document_score));
            }

            scores.clear();
            indexes.clear();

            // get min docID from posting list iterators and indexes of posting list iterators with min docID
            minDocId = getNextDocId(indexes);
        } while (minDocId != Integer.MAX_VALUE);

        Collections.sort(queryResults);
        if (queryResults.size() > K) {
            queryResults = new ArrayList<>(queryResults.subList(0, K));
        }

        for (PostingList pi : postingLists) {
            if(pi != null)
                pi.closeList();
        }
        postingLists.clear();

    }

    private double tfidf(int tf, int df) {
        double score = 0;
        if (tf > 0)
            score = (1 + Math.log(tf)) * Math.log(N_docs / df);
        return score;
    }

    private double BM25(int tf, int df, int docLength, double avgDocLength) {
        //TODO CONTROLLARE LA FORMULA
        double score = 0;
        double k1 = 1.2;
        double b = 0.75;
        double B =  ((1 - b) + b * (docLength / avgDocLength));
        double idf = Math.log((N_docs) / (df));
        score = (tf / (k1 * B + tf)) * idf;
        return score;
    }

    private int getNextDocId(ArrayList<Integer> indexes) {
        int min = Integer.MAX_VALUE;

        for (int i = 0; i < postingLists.size(); i++) {
            PostingList postList = postingLists.get(i);
            if(postList == null) // term not in lexicon
                continue;
            if (postList.getActualPosting() == null) { //first lecture
                if (postList.hasNext())
                    postList.next();
                else
                    continue;
            }

            if (postList.getDocId() < min) {
                indexes.clear();
                min = postList.getDocId();
                indexes.add(i);
            } else if (postList.getDocId() == min) {
                indexes.add(i);
            }
        }
        return min;
    }

    public void printResults(long time) {
        if (queryResults == null || queryResults.size() == 0) {
            System.out.println("Unfortunately, no documents were found for your query.");
            return;
        }

        System.out.println("These " + queryResults.size() + " documents may are of your interest");
        System.out.println(queryResults);
        System.out.println("Search time: " + time + " ms");
    }

    public ArrayList<QueryResult> getQueryResults() {
        return this.queryResults;
    }
}
