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
    private ArrayList<Iterator<Posting>> postingsIterators = null;
    private ArrayList<Iterator<BlockDescriptor>> blockDescriptorIterators = null;
    private static int N_docs = 0; // number of documents in the collection

    private static final int NUMBER_OF_POSTING = 10;
    private static final int BLOCK_POSTING_LIST_SIZE = (4 * 2) * NUMBER_OF_POSTING; // 4 byte per docID, 4 byte per freq and postings


    public Searcher() {
        queryResults = new ArrayList<>();
        postingsIterators = new ArrayList<>();
        blockDescriptorIterators = new ArrayList<>();
        //read number of docs from disk
        try (FileInputStream fileIn = new FileInputStream("data/index/numberOfDocs.bin");
             ObjectInputStream in = new ObjectInputStream(fileIn)) {
            N_docs = (int) in.readObject();
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

    public void DAAT_block(ArrayList<String> queryTerms, Lexicon lexicon, ArrayList<Document> documents, int K, String mode) {
        queryResults.clear();
        long firstBlockOffset;
        int blocksNumber;
        int minDocId = Integer.MAX_VALUE;

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
                postingsIterators.add(postingList.getPostings().iterator()); // add postinglist of the term to postingListIterators
                // check if the min docID of the posting list is the new min
                if (postingList.getMinDocId() < minDocId)
                    minDocId = postingList.getMinDocId();
            }
        }

        if (postingsIterators.size() == 0)
            return; // if no terms in query are in lexicon means that there are no results

        int next_docId;
        do {
            ArrayList<Double> scores = new ArrayList<>();
            // Get the next docId by finding the minimum docId among all iterators
            next_docId = Integer.MAX_VALUE;
            for (Iterator<Posting> postingIterator : postingsIterators) {
                if (postingIterator.hasNext()) {
                    int currentDocId = postingIterator.next().getDocID();
                    if (currentDocId < next_docId) {
                        next_docId = currentDocId;
                    }
                }
            }

            if (next_docId == Integer.MAX_VALUE)
                break;

            double document_score = 0;
            int term_counter = 0;

            for (int i = 0; i < postingsIterators.size(); i++) {
                Iterator<Posting> postingIterator = postingsIterators.get(i);
                if (postingIterator.hasNext()) {
                    Posting posting = postingIterator.next();
                    int docId = posting.getDocID();
                    if (docId == next_docId) {
                        int tf = posting.getFreq();
                        scores.add(tfidf(tf, lexicon.getLexiconElem(queryTerms.get(i)).getDf()));
                        term_counter++;
                    }
                }
            }

            if (mode.equals("conjunctive") && term_counter != queryTerms.size())
                scores.clear();

            // Sum all the values of scores
            for (double score : scores) {
                document_score += score;
            }
            if (document_score > 0) {
                // Get document
                Document document = documents.get(next_docId);
                // Get document pid
                String pid = document.getDocNo();
                // Add pid to results
                queryResults.add(new QueryResult(pid, document_score));
            }
        } while (next_docId != Integer.MAX_VALUE);

        Collections.sort(queryResults);
        if (queryResults.size() > K) {
            queryResults = new ArrayList<>(queryResults.subList(0, K));
        }


    }

    public void DAAT_disk(ArrayList<String> queryTerms, Lexicon lexicon, ArrayList<Document> documents, int K, String mode) {
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
            queryResults = new ArrayList<>(queryResults.subList(0, K));
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
        if (queryResults == null || queryResults.size() == 0) {
            System.out.println("Unfortunately, no documents were found for your query.");
            return;
        }

        System.out.println("These " + queryResults.size() + " documents may are of your interest");
        System.out.println(queryResults);
        System.out.println("Search time: " + time + " ms");
    }

}
