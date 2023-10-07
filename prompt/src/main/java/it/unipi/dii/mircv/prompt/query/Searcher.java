package it.unipi.dii.mircv.prompt.query;

import it.unipi.dii.mircv.index.structures.*;
import it.unipi.dii.mircv.prompt.structure.QueryResult;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.*;


/**
 * This class is used to perform the search of the documents in the collection.
 * It uses the lexicon and the posting lists to perform the search.
 * It implements two different search algorithms:
 * - DAAT (Document At A Time)
 * - MaxScore
 */
public class Searcher {
    private Lexicon lexicon;
    private ArrayList<Document> documents;
    private ArrayList<QueryResult> queryResults; // list of documents that match the query
    private ArrayList<String> previousQueryTerms; // list of query terms of the previous query, used to avoid reprocessing the same query
    private String previousMode; // mode of the previous query, used to avoid reprocessing the same query
    private String previousScoringFunction; // scoring function of the previous query, used to avoid reprocessing the same query
    private ArrayList<BlockDescriptorList> blockDescriptorList; // list of block descriptors for each query term
    private ArrayList<PostingList> postingLists; // list of posting lists for each query term
    private double AVG_DOC_LENGTH;
    private static int N_docs = 0; // number of documents in the collection
    private static final String BLOCK_DESCRIPTOR_PATH = "data/index/blockDescriptor.bin";
    private static final String INDEX_PATH = "data/index/index.bin";

    /**
     * Constructor of the class.
     *
     * @param lexicon   lexicon of the collection
     * @param documents list of documents in the collection
     */
    public Searcher(Lexicon lexicon, ArrayList<Document> documents) {
        this.queryResults = new ArrayList<>();
        this.postingLists = new ArrayList<>();
        this.previousQueryTerms = new ArrayList<>();
        this.blockDescriptorList = new ArrayList<>();
        this.previousMode = "";
        this.previousScoringFunction = "";
        this.lexicon = lexicon;
        this.documents = documents;
        //read number of docs from disk
        try (FileInputStream fileIn = new FileInputStream("data/index/documentInfo.bin");
             ObjectInputStream in = new ObjectInputStream(fileIn)) {
            this.N_docs = (int) in.readObject();
            long totDocLength = (long) in.readObject();
            AVG_DOC_LENGTH = totDocLength / N_docs;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method is used to perform the DAAT algorithm.
     *
     * @param queryTerms      list of query terms
     * @param K               number of results to be returned
     * @param mode            mode of the query
     * @param scoringFunction scoring function used
     */
    public void DAAT(ArrayList<String> queryTerms, int K, String mode, String scoringFunction) {
        if ((this.previousQueryTerms.equals(queryTerms) &&
                this.previousScoringFunction.equals(scoringFunction) &&
                (this.previousMode.equals(mode) || queryTerms.size() == 1))) // same query as before
            return;
        //process query and clear previous results
        this.previousQueryTerms = new ArrayList<>(queryTerms);
        this.previousMode = mode;
        this.previousScoringFunction = scoringFunction;
        this.queryResults.clear();

        int minDocId;
        ArrayList<Integer> indexes = new ArrayList<>();
        ArrayList<Double> scores = new ArrayList<>();
        ArrayList<Integer> blocksNumber = new ArrayList<>();
        ArrayList<String> queryTermsPresentInLexicon = new ArrayList<>();

        LinkedHashMap<String, LexiconElem> queryTermsMap = new LinkedHashMap<>();
        for (String term : queryTerms) {
            if (lexicon.getLexicon().containsKey(term)) {
                queryTermsMap.put(term, lexicon.getLexiconElem(term));
            } else if (!lexicon.getLexicon().containsKey(term) && mode.equals("conjunctive")) {
                return;
            }
        }

        if (queryTermsMap.size() == 0)
            return;

        //initialize posting list for query terms
        initializePostingListForQueryTerms(queryTermsMap, blocksNumber);
        queryTermsPresentInLexicon.addAll(queryTermsMap.keySet());

        if (postingLists.size() == 0)
            return; // if no terms in query are in lexicon means that there are no results

        do {
            scores.clear();
            indexes.clear();
            // get min docID from posting list iterators and indexes of posting list iterators with min docID
            minDocId = getNextDocIdDAAT(indexes);

            if (minDocId == Integer.MAX_VALUE)
                break;

            if (mode.equals("conjunctive") && indexes.size() != postingLists.size()) {
                for (Integer i : indexes) {
                    //update posting list
                    updatePosting(postingLists.get(i), i);
                }
                continue;
            }

            double document_score = 0;

            for (Integer i : indexes) {
                //calculate score for posting list with min docID
                if (postingLists.get(i).getPostingIterator() != null) {
                    if (scoringFunction.equals("TFIDF"))
                        scores.add(tfidf(postingLists.get(i).getFreq(), lexicon.getLexiconElem(queryTermsPresentInLexicon.get(i)).getDf()));
                    else if (scoringFunction.equals("BM25"))
                        scores.add(BM25(postingLists.get(i).getFreq(), lexicon.getLexiconElem(queryTermsPresentInLexicon.get(i)).getDf(), documents.get(minDocId).getLength(), AVG_DOC_LENGTH));
                    //update posting list
                    updatePosting(postingLists.get(i), i);
                }
            }

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
                document_score = Math.round(document_score * 10e5) / 10e5;
                queryResults.add(new QueryResult(pid, document_score));
            }
        } while (true);
        Collections.sort(queryResults);
        if (queryResults.size() > K) {
            queryResults = new ArrayList<>(queryResults.subList(0, K));
        }
        postingLists.clear();
    }

    /**
     * This method is used to perform the MaxScore algorithm.
     *
     * @param queryTerms      list of query terms
     * @param K               number of results to be returned
     * @param mode            mode of the query
     * @param scoringFunction scoring function used
     */
    public void maxScore(ArrayList<String> queryTerms, int K, String mode, String scoringFunction) {
        if ((this.previousQueryTerms.equals(queryTerms) && this.previousScoringFunction.equals(scoringFunction) && (this.previousMode.equals(mode) || queryTerms.size() == 1))) // same query as before
            return;
        //process query and clear previous results
        this.previousQueryTerms = new ArrayList<>(queryTerms);
        this.previousMode = mode;
        this.previousScoringFunction = scoringFunction;
        this.queryResults.clear();

        ArrayList<Integer> blocksNumber = new ArrayList<>();
        int essential_index = 0;
        double current_threshold = 0, partial_score, DUB;

        //sort query term by tub
        LinkedHashMap<String, LexiconElem> queryTermsMap = new LinkedHashMap<>();
        for (String term : queryTerms) {
            if (lexicon.getLexicon().containsKey(term)) {
                queryTermsMap.put(term, lexicon.getLexiconElem(term));
            } else if (!lexicon.getLexicon().containsKey(term) && mode.equals("conjunctive")) {
                return;
            }
        }
        if (queryTermsMap.size() == 0)
            return;
        queryTermsMap = Lexicon.sortLexicon(queryTermsMap, scoringFunction);

        //initialize posting list for query terms
        initializePostingListForQueryTerms(queryTermsMap, blocksNumber);

        // until there are posting lists to be processed
        do {
            // get next docid to be processed
            int new_essential_index = -2;
            int docid = getNextDocIdMAXSCORE(essential_index);

            if (docid == Integer.MAX_VALUE)
                break;

            if (scoringFunction.equals("TFIDF"))
                DUB = documents.get(docid).getDUB_tfidf();
            else
                DUB = documents.get(docid).getDUB_bm25();

            //process next docid
            if (DUB < current_threshold) {
                //update posting lists
                for (int j = essential_index; j < postingLists.size(); j++) {
                    PostingList pl = postingLists.get(j);
                    if (pl.getPostingIterator() != null && pl.getDocId() == docid) {
                        updatePosting(pl, j);
                    }
                }
                continue;
            }

            partial_score = computeEssentialPS(essential_index, scoringFunction, docid, queryTermsMap, mode); // compute partial score for docID into essential posting list

            if (current_threshold != 0)
                DUB = sumNonEssentialTUBs(essential_index, partial_score, scoringFunction, queryTermsMap);
            else
                DUB = partial_score;

            // if DUB > current_threshold compute DUB
            if (DUB > current_threshold) {
                partial_score = computeDUB(essential_index, docid, scoringFunction, partial_score, DUB, current_threshold, blocksNumber, queryTermsMap, mode);
            }
            if (partial_score > current_threshold) {
                // Get document
                Document document = documents.get(docid);
                // Get document pid
                String pid = document.getDocNo();
                // Add pid to results
                partial_score = Math.round(partial_score * 10e5) / 10e5;
                queryResults.add(new QueryResult(pid, partial_score));
                // if queryresult.size > k sort by score and remove last element
                if (queryResults.size() >= K) {
                    Collections.sort(queryResults);
                    if (queryResults.size() > K)
                        queryResults.remove(queryResults.size() - 1);
                    // update current_threshold
                    current_threshold = queryResults.get(queryResults.size() - 1).getScoring();
                    new_essential_index = compute_essential_index(queryTermsMap, scoringFunction, current_threshold);
                }
            }
            if (new_essential_index != -2)
                essential_index = new_essential_index;
        }
        while (essential_index != -1);
        Collections.sort(queryResults);
    }

    /**
     * This method is used to compute the partial score of a document. It computes the score of the document indicated by docid
     * counting the score of the non-essential posting lists that contain the document.
     *
     * @param essential_index   index of the first posting list that contains essential postings
     * @param docid             docID of the document
     * @param scoringFunction   scoring function used
     * @param partial_score     partial score of the document
     * @param DUB               DUB of the document
     * @param current_threshold current threshold
     * @param blocksNumber      list of number of blocks for each query term
     * @param queryTermsMap     map of query terms and their lexicon elements
     * @param mode              mode of the query
     * @return partial score of the document
     */
    private double computeDUB(int essential_index, int docid, String scoringFunction, double partial_score, double DUB, double current_threshold, ArrayList<Integer> blocksNumber, HashMap<String, LexiconElem> queryTermsMap, String mode) {
        ArrayList<String> termList = new ArrayList<>(queryTermsMap.keySet());
        /*
        for (int j = essential_index - 1; j >= 0; j--) {
            String term = termList.get(j);
            Posting p = postingLists.get(j).nextGEQ(docid, blockDescriptorList.get(j), blocksNumber.get(j), INDEX_PATH);

            if (p == null && mode.equals("conjunctive"))
                return 0;
            else if (p == null)
                continue;

            double termScore;
            if (scoringFunction.equals("TFIDF")) {
                termScore = tfidf(p.getFreq(), lexicon.getLexiconElem(term).getDf());
            } else if (scoringFunction.equals("BM25")) {
                termScore = BM25(p.getFreq(), lexicon.getLexiconElem(term).getDf(), documents.get(docid).getLength(), AVG_DOC_LENGTH);
            } else {
                termScore = 0; // Gestione del caso in cui la scoringFunction non è definita
            }

            DUB -= termScore;

            if (p.getDocID() != docid && mode.equals("conjunctive"))
                return 0;
            else if (p.getDocID() != docid)
                continue;

            partial_score += termScore;
            DUB += termScore;

            if (DUB < current_threshold)
                break;
        } FOR OTTIMIZZATO TODO
    */
        for (int j = essential_index - 1; j >= 0; j--) {
            Posting p = postingLists.get(j).nextGEQ(docid, blockDescriptorList.get(j), blocksNumber.get(j), INDEX_PATH);

            // nextGEQ return null if docid not found in posting list
            if (p == null && mode.equals("conjunctive"))
                return 0;
            else if (p == null)
                continue;

            if (scoringFunction.equals("TFIDF"))
                DUB -= queryTermsMap.get(termList.get(j)).getTUB_tfidf();
            else if (scoringFunction.equals("BM25"))
                DUB -= queryTermsMap.get(termList.get(j)).getTUB_bm25();

            // nextGEQ return a posting with docid != docid
            if (p.getDocID() != docid && mode.equals("conjunctive"))
                return 0;
            else if (p.getDocID() != docid)
                continue;

            // docid found in posting list
            double result = 0;
            if (scoringFunction.equals("TFIDF")) {
                result = tfidf(p.getFreq(), lexicon.getLexiconElem(termList.get(j)).getDf());
                partial_score += result;
                DUB += result;
            } else if (scoringFunction.equals("BM25")) {
                result = BM25(p.getFreq(), lexicon.getLexiconElem(termList.get(j)).getDf(), documents.get(docid).getLength(), AVG_DOC_LENGTH);
                partial_score += result;
                DUB += result;
            }
            if (DUB < current_threshold)
                break;

        }
        return partial_score;
    }

    /**
     * This method is used to compute the sum of the TUBs of the non-essential posting lists.
     * We sum to the partial score the TUBs of the non-essential posting lists.
     *
     * @param essential_index index of the first posting list that contains essential postings
     * @param partial_score   partial score of the document
     * @param scoringFunction scoring function used
     * @param queryTermsMap   map of query terms and their lexicon elements
     * @return sum of the TUBs of the non-essential posting lists
     */
    private double sumNonEssentialTUBs(int essential_index, double partial_score, String scoringFunction, HashMap<String, LexiconElem> queryTermsMap) {
        // calcolo DUB
        double DUB = partial_score;
        ArrayList<String> termList = new ArrayList<>(queryTermsMap.keySet());

        for (int j = 0; j < essential_index; j++) {
            if (scoringFunction.equals("TFIDF"))
                DUB += queryTermsMap.get(termList.get(j)).getTUB_tfidf();
            else if (scoringFunction.equals("BM25"))
                DUB += queryTermsMap.get(termList.get(j)).getTUB_bm25();
        }
        /*
         if (scoringFunction.equals("TFIDF")) {
            for (int j = 0; j < essential_index; j++) {
                DUB += queryTermsMap.get(termList.get(j)).getTUB_tfidf();
            }
        } else if (scoringFunction.equals("BM25")) {
            for (int j = 0; j < essential_index; j++) {
                DUB += queryTermsMap.get(termList.get(j)).getTUB_bm25();
            }
        }
    */
        return DUB;
    }

    /**
     * This method is used to compute the partial score of a document. It computes the score of the document indicated by docid
     * counting the score of the essential posting lists that contain the document.
     *
     * @param essential_index index of the first posting list that contains essential postings
     * @param scoringFunction scoring function used
     * @param docid           docID of the document
     * @param queryTermsMap   map of query terms and their lexicon elements
     * @param mode            mode of the query
     * @return partial score of the document
     */
    private double computeEssentialPS(int essential_index, String scoringFunction, int docid, HashMap<String, LexiconElem> queryTermsMap, String mode) {
        double partial_score = 0;
        ArrayList<String> termList = new ArrayList<>(queryTermsMap.keySet());
        boolean notFound = false;
        for (int j = essential_index; j < postingLists.size(); j++) {
            PostingList pl = postingLists.get(j);
            if (pl.getPostingIterator() == null || pl.getDocId() != docid) {
                if (mode.equals("conjunctive"))
                    notFound = true;
                continue;
            }
            if (scoringFunction.equals("TFIDF"))
                partial_score += tfidf(pl.getFreq(), lexicon.getLexiconElem(termList.get(j)).getDf());
            else if (scoringFunction.equals("BM25"))
                partial_score += BM25(pl.getFreq(), lexicon.getLexiconElem(termList.get(j)).getDf(), documents.get(docid).getLength(), AVG_DOC_LENGTH);
            //update posting list
            updatePosting(pl, j);
        }
        if (notFound)
            return 0;
        return partial_score;
        /*
        for (int j = essential_index; j < postingLists.size(); j++) {
            PostingList pl = postingLists.get(j);
            if (pl.getPostingIterator() != null && pl.getDocId() == docid) {
                if (scoringFunction.equals("TFIDF")) {
                    partial_score += tfidf(pl.getFreq(), lexicon.getLexiconElem(termList.get(j)).getDf());
                } else if (scoringFunction.equals("BM25")) {
                    partial_score += BM25(pl.getFreq(), lexicon.getLexiconElem(termList.get(j)).getDf(), documents.get(docid).getLength(), AVG_DOC_LENGTH);
                }
                //update posting list
                updatePosting(pl, j);
            } else if (mode.equals("conjunctive")) {
                return 0; // Se il documento non è trovato e la modalità è "conjunctive", ritorna 0.
            }
        }

        return partial_score;
         */
    }

    /**
     * This method is used to update the posting list iterator.
     * If the posting list iterator has no more postings, it loads the next block.
     * If the posting list iterator has no more blocks, it closes the posting list.
     *
     * @param pl posting list iterator to be updated
     * @param j  index of the posting list iterator
     */
    private void updatePosting(PostingList pl, int j) {
        if (pl.hasNext()) // we have more postings in the current block
            pl.next();
        else if (!pl.hasNext() && blockDescriptorList.get(j).hasNext()) { // need to load next block
            blockDescriptorList.get(j).next();
            pl.readPostingList(-1, blockDescriptorList.get(j).getNumPosting(), blockDescriptorList.get(j).getPostingListOffset(), INDEX_PATH);
            pl.openList();
            pl.next();
        } else {
            pl.closeList();
        }
    }

    /**
     * This method is used to initialize the posting lists for the query terms.
     *
     * @param queryTermsMap map of query terms and their lexicon elements
     * @param blocksNumber  list of number of blocks for each query term
     */
    private void initializePostingListForQueryTerms(HashMap<String, LexiconElem> queryTermsMap, ArrayList<Integer> blocksNumber) {
        blockDescriptorList.clear();
        postingLists.clear();

        /*
        for (String term : queryTermsMap.keySet()) {
            LexiconElem lexiconElem = lexicon.getLexiconElem(term);
            long firstBlockOffset = lexiconElem.getOffset();
            int numBlocks = lexiconElem.getBlocksNumber();

            BlockDescriptorList blockDescriptor = new BlockDescriptorList(firstBlockOffset, numBlocks, BLOCK_DESCRIPTOR_PATH);
            blockDescriptor.openBlock();
            blockDescriptor.next();
            blockDescriptorList.add(blockDescriptor);

            PostingList postingList = new PostingList();
            postingList.readPostingList(-1, blockDescriptor.getNumPosting(), blockDescriptor.getPostingListOffset(), INDEX_PATH);
            postingList.openList();
            postingList.next();
            postingLists.add(postingList);

            blocksNumber.add(numBlocks);
        }
        */

        int i = 0;
        long firstBlockOffset;
        for (String term : queryTermsMap.keySet()) {
            firstBlockOffset = lexicon.getLexiconElem(term).getOffset();
            blocksNumber.add(lexicon.getLexiconElem(term).getBlocksNumber());
            //read all blocks
            blockDescriptorList.add(new BlockDescriptorList(firstBlockOffset, blocksNumber.get(i), BLOCK_DESCRIPTOR_PATH));
            blockDescriptorList.get(i).openBlock();
            blockDescriptorList.get(i).next();
            //load first posting list for the term
            PostingList postingList = new PostingList();
            postingList.readPostingList(-1, blockDescriptorList.get(i).getNumPosting(), blockDescriptorList.get(i).getPostingListOffset(), INDEX_PATH);
            postingList.openList();
            postingLists.add(postingList); // add postinglist of the term to postingListIterators
            postingLists.get(i).next();
            i++;
        }
    }

    /**
     * This method is used to compute the index of the first posting list that contains essential postings.
     *
     * @param queryTermsMap     map of query terms and their lexicon elements
     * @param scoringFunction   scoring function used
     * @param current_threshold current threshold
     * @return index of the first posting list that contains essential postings
     */
    private int compute_essential_index(HashMap<String, LexiconElem> queryTermsMap, String scoringFunction, double current_threshold) {
        if (current_threshold == 0)
            return 0;
        int essential_index = 0;
        double TUBsum = 0;
        boolean essential_postings_found = false;
        for (String term : queryTermsMap.keySet()) {
            if (scoringFunction.equals("BM25"))
                TUBsum += queryTermsMap.get(term).getTUB_bm25();
            else if (scoringFunction.equals("TFIDF"))
                TUBsum += queryTermsMap.get(term).getTUB_tfidf();
            if (TUBsum < current_threshold) {
                essential_index++;
            } else {
                //essential postings found
                essential_postings_found = true;
                break;
            }
        }
        if (essential_postings_found)
            return essential_index;
        else
            return -1;

        /*
            if (current_threshold == 0) {
                return 0;
            }

            int essential_index = 0;
            double TUBsum = 0;

            for (String term : queryTermsMap.keySet()) {
                LexiconElem lexiconElem = queryTermsMap.get(term);

                if (scoringFunction.equals("BM25")) {
                    TUBsum += lexiconElem.getTUB_bm25();
                } else if (scoringFunction.equals("TFIDF")) {
                    TUBsum += lexiconElem.getTUB_tfidf();
                }

                if (TUBsum >= current_threshold) {
                    return essential_index;
                }

                essential_index++;
            }

            return -1; // No essential postings found
        */
    }

    /**
     * This method is used to compute the TFIDF score of a document.
     *
     * @param tf term frequency in the document
     * @param df document frequency of the term
     * @return TFIDF score of the document
     */
    private double tfidf(int tf, int df) {
        double score = 0;
        if (tf > 0)
            score = (1 + Math.log(tf)) * Math.log(N_docs / df);
        return score;
    }

    /**
     * This method is used to compute the BM25 score of a document.
     *
     * @param tf           term frequency in the document
     * @param df           document frequency of the term
     * @param docLength    length of the document
     * @param avgDocLength average length of the documents in the collection
     * @return BM25 score of the document
     */
    private double BM25(int tf, int df, int docLength, double avgDocLength) {
        double score;
        double k1 = 1.2;
        double b = 0.75;
        double B = ((1 - b) + b * (docLength / avgDocLength));
        double idf = Math.log((N_docs) / (df));
        score = (tf / (k1 * B + tf)) * idf;
        return score;
    }

    /**
     * This method is used to get the next docID to be processed in the DAAT algorithm.
     * It returns the minimum docID among the posting lists iterators.
     *
     * @param indexes list of indexes of posting lists iterators with min docID
     * @return the minimum docID among the posting lists iterators
     */
    private int getNextDocIdDAAT(ArrayList<Integer> indexes) {
        int min = Integer.MAX_VALUE;
        for (int i = 0; i < postingLists.size(); i++) {
            PostingList postList = postingLists.get(i);
            if (postList.getPostingIterator() == null)
                continue; // posting list is empty or closed
            if (postList.getDocId() < min) {
                indexes.clear(); // reset indexes
                min = postList.getDocId(); // update min
                indexes.add(i); // add index of posting list iterator with min docID
            } else if (postList.getDocId() == min) {
                indexes.add(i); // add index of posting list iterator with min docID
            }
        }
        return min;
    }

    /**
     * This method is used to get the next docID to be processed in the MaxScore algorithm.
     * It returns the minimum docID among the posting lists iterators.
     *
     * @param essential_index index of the first posting list that contains essential postings
     * @return the minimum docID among the posting lists iterators
     */
    private int getNextDocIdMAXSCORE(int essential_index) {
        int min = Integer.MAX_VALUE;
        for (int i = essential_index; i < postingLists.size(); i++) {
            PostingList postList = postingLists.get(i);
            if (postList.getPostingIterator() == null)
                continue; // posting list is empty or closed
            if (postList.getDocId() < min) // update min
                min = postList.getDocId();
        }
        return min;
    }

    public void printResults(long time) {
        if (queryResults == null || queryResults.size() == 0) {
            System.out.println("\nUnfortunately, no documents were found for your query.");
            return;
        }
        System.out.println("\nThese " + queryResults.size() + " documents may are of your interest");
        for (QueryResult qr : queryResults) {
//            System.out.println(documents.get(Integer.parseInt(qr.getDocNo())).getDUB_tfidf());
//            System.out.println(documents.get(Integer.parseInt(qr.getDocNo())).getDUB_bm25());
            System.out.println(qr);
        }
        System.out.println("\nSearch time: " + time + " ms");
    }

    public ArrayList<QueryResult> getQueryResults() {
        return this.queryResults;
    }

}