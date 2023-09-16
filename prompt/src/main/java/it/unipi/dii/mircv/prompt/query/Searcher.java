package it.unipi.dii.mircv.prompt.query;

import it.unipi.dii.mircv.index.structures.*;
import it.unipi.dii.mircv.prompt.structure.QueryResult;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.*;

public class Searcher {

    //    private String term;
    private Lexicon lexicon;
    private ArrayList<Document> documents;
    private ArrayList<QueryResult> queryResults;
    private ArrayList<String> previousQueryTerms;
    private ArrayList<BlockDescriptorList> blockDescriptorList;
    private ArrayList<PostingList> postingLists = null;
    private String previousMode;
    private String previousScoringFunction;
    private double AVG_DOC_LENGTH;
    private static int N_docs = 0; // number of documents in the collection


    private static final int NUMBER_OF_POSTING = 10;
    private static final int BLOCK_POSTING_LIST_SIZE = (4 * 2) * NUMBER_OF_POSTING; // 4 byte per docID, 4 byte per freq and postings
    private static final int POSTING_LIST_SIZE = (4 * 2); // 4 byte per docID, 4 byte per freq

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

    public void DAAT(ArrayList<String> queryTerms, int K, String mode, String scoringFunction) {
//        if ((this.previousQueryTerms.equals(queryTerms) && this.previousMode.equals(mode) && this.previousScoringFunction.equals(scoringFunction))
//                || (this.previousQueryTerms.equals(queryTerms) && queryTerms.size() == 1 && this.previousScoringFunction.equals(scoringFunction)))) // same query as before
        if ((this.previousQueryTerms.equals(queryTerms) &&
                this.previousScoringFunction.equals(scoringFunction) &&
                (this.previousMode.equals(mode) || queryTerms.size() == 1))) // same query as before
            return;
        //process query and clear previous results
        this.previousQueryTerms = new ArrayList<>(queryTerms);
        this.previousMode = mode;
        this.previousScoringFunction = scoringFunction;
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
                BlockDescriptor firstBlockDescriptor = BlockDescriptor.readFirstBlock(firstBlockOffset); // read first block descriptor, used because MaxScore is not implemented
//                blocksNumber = lexicon.getLexiconElem(term).getBlocksNumber();
//                blockDescriptorIterators.add(openBlocks(firstBlockOffset, blocksNumber).iterator());
                // load total posting list for the term, used because MaxScore is not implemented
                PostingList postingList = new PostingList();
                postingList.readPostingList(-1, lexicon.getLexiconElem(term).getDf(), firstBlockDescriptor.getPostingListOffset());
                postingList.openList();
                postingLists.add(postingList); // add postinglist of the term to postingListIterators
            } else {
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

                if (scoringFunction.equals("TFIDF"))
                    scores.add(tfidf(postingLists.get(i).getFreq(), lexicon.getLexiconElem(queryTerms.get(i)).getDf()));
                else if (scoringFunction.equals("BM25"))
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
            if (pi != null)
                pi.closeList();
        }
        postingLists.clear();

        if (postingLists.size() == 0)
            return; // if no terms in query are in lexicon means that there are no results

    }

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
        double current_threshold = 0, partial_score = 0, DUB = 0;
        ArrayList<Integer> alreadyVisited = new ArrayList<>();

        //fai un sort su query terms in base a TUB
        LinkedHashMap<String, LexiconElem> queryTermsMap = new LinkedHashMap<>();
        for (String term : queryTerms) {
            if (lexicon.getLexicon().containsKey(term)) {
                queryTermsMap.put(term, lexicon.getLexiconElem(term));
            }
        }

        queryTermsMap = Lexicon.sortLexicon(queryTermsMap, scoringFunction);
        //initialize posting list for query terms
        initializePostingListForQueryTerms(queryTermsMap, blocksNumber);

        // finche ho essential posting list
        do {
            // get next docid to be processed
            int docid = postingLists.get(essential_index).getDocId();
            if (alreadyVisited.contains(docid)) {
                // check if docid is already processed, if yes go to next docid in posting list
                postingLists.get(essential_index).next();
                continue;
            }
            alreadyVisited.add(docid);

            partial_score = computeEssentialPS(essential_index, scoringFunction, docid, blocksNumber, queryTermsMap); // compute partial score for docID into essential posting list

            DUB = sumNonEssentialTUBs(essential_index, partial_score, scoringFunction, queryTermsMap);

            //controllo se DUB > current_threshold
            if (DUB > current_threshold) {
                partial_score = computeDUB(essential_index, docid, scoringFunction, partial_score, DUB, current_threshold, blocksNumber, queryTermsMap);
            }

            int new_essential_index = -2;
            if (partial_score > current_threshold) {
                // Get document
                Document document = documents.get(docid);
                // Get document pid
                String pid = document.getDocNo();
                // Add pid to results
                queryResults.add(new QueryResult(pid, partial_score));
                // SE queryresult.size > k
                if (queryResults.size() >= K) {
                    Collections.sort(queryResults);
                    if (queryResults.size() > K)
                        // Rimuovi ultimo elemento
                        queryResults.remove(queryResults.size() - 1);
                    // Aggiorna current_threshold
                    current_threshold = queryResults.get(queryResults.size() - 1).getScoring();
                    new_essential_index = compute_essential_index(queryTermsMap, scoringFunction, current_threshold);
                }
            }

            updateProcessingPost(blockDescriptorList, postingLists, essential_index, new_essential_index, queryTermsMap, blocksNumber);
        } while (essential_index != -1);

    }


    private int updateProcessingPost(ArrayList<BlockDescriptorList> blockDescriptorList, ArrayList<PostingList> postingLists, int essential_index, int new_essential_index, LinkedHashMap<String, LexiconElem> queryTermsMap, ArrayList<Integer> blocksNumber) {
        if (new_essential_index == -2 || essential_index == new_essential_index) { // non sono cambiati gli essential posting list
            if (postingLists.get(essential_index).hasNext()) // ho ancora post nella posting list attuale
                postingLists.get(essential_index).next();
            if (!postingLists.get(essential_index).hasNext() && blockDescriptorList.get(essential_index).hasNext()) { //devo caricare un altro blocco
                blockDescriptorList.get(essential_index).next();
                postingLists.get(essential_index).readPostingList(-1, blockDescriptorList.get(essential_index).getNumPosting(), blockDescriptorList.get(essential_index).getPostingListOffset());
                postingLists.get(essential_index).openList();
                postingLists.get(essential_index).next();
            } else {
                // posting list finita si va alla prossima posting list essential se esiste
                if (essential_index < postingLists.size() - 1) {
                    essential_index++;
                    //TODO troppo lenta cosi perche fa la clear e la ricarica con letture da disco, una volta caricate andrebbero solo riordinate
                    postingLists.clear();
                    initializePostingListForQueryTerms(queryTermsMap, blocksNumber);
                } else
                    essential_index = -1;
            }
        } else { // sono cambiati gli essential posting list
            essential_index = new_essential_index;
            //TODO troppo lenta cosi perche fa la clear e la ricarica con letture da disco, una volta caricate andrebbero solo riordinate
            postingLists.clear();
            initializePostingListForQueryTerms(queryTermsMap, blocksNumber);
        }
        //probabilmente va fatto un reset di tutti i posting list iterator e anche dei blocchi
        return essential_index;
    }

    private double computeDUB(int essential_index, int docid, String scoringFunction, double partial_score, double DUB, double current_threshold, ArrayList<Integer> blocksNumber, HashMap<String, LexiconElem> queryTermsMap) {
        ArrayList<String> termList = new ArrayList<>(queryTermsMap.keySet());
        for (int j = essential_index - 1; j >= 0; j--) {
            postingLists.get(j).nextGEQ(docid, blockDescriptorList.get(j), blocksNumber.get(j));

            if (scoringFunction.equals("TFIDF"))
                DUB -= queryTermsMap.get(termList.get(j)).getTUB_tfidf();
            else if (scoringFunction.equals("BM25"))
                DUB -= queryTermsMap.get(termList.get(j)).getTUB_bm25();

            if (postingLists.get(j).getDocId() != docid) // if docid not found in posting list
                continue;

            // docid found in posting list
            double result = 0;
            if (scoringFunction.equals("TFIDF")) {
                result = tfidf(postingLists.get(essential_index).getFreq(), lexicon.getLexiconElem(termList.get(essential_index)).getDf());
                partial_score += result;
                DUB += result;
            } else if (scoringFunction.equals("BM25")) {
                result = BM25(postingLists.get(essential_index).getFreq(), lexicon.getLexiconElem(termList.get(essential_index)).getDf(), documents.get(docid).getLength(), AVG_DOC_LENGTH);
                partial_score += result;
                DUB += result;
            }
            if (DUB < current_threshold)
                break;

        }
        return partial_score;
    }

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

        return DUB;
    }

    private double computeEssentialPS(int essential_index, String scoringFunction, int docid, ArrayList<Integer> blocksNumber, HashMap<String, LexiconElem> queryTermsMap) {
        double partial_score = 0;
        ArrayList<String> termList = new ArrayList<>(queryTermsMap.keySet());
        if (scoringFunction.equals("TFIDF"))
            partial_score += tfidf(postingLists.get(essential_index).getFreq(), lexicon.getLexiconElem(termList.get(essential_index)).getDf());
        else if (scoringFunction.equals("BM25"))
            partial_score += BM25(postingLists.get(essential_index).getFreq(), lexicon.getLexiconElem(termList.get(essential_index)).getDf(), documents.get(docid).getLength(), AVG_DOC_LENGTH);

        int i = 1;
        for (int j = essential_index + 1; j < postingLists.size(); j++) {
            postingLists.get(j).nextGEQ(docid, blockDescriptorList.get(j), blocksNumber.get(j));
            if (postingLists.get(j).getDocId() != docid)
                continue;
            if (scoringFunction.equals("TFIDF"))
                partial_score += tfidf(postingLists.get(essential_index).getFreq(), lexicon.getLexiconElem(termList.get(essential_index)).getDf());
            else if (scoringFunction.equals("BM25"))
                partial_score += BM25(postingLists.get(essential_index).getFreq(), lexicon.getLexiconElem(termList.get(essential_index)).getDf(), documents.get(docid).getLength(), AVG_DOC_LENGTH);
        }

        return partial_score;
    }

    private void initializePostingListForQueryTerms(HashMap<String, LexiconElem> queryTermsMap, ArrayList<Integer> blocksNumber) {
        int i = 0;
        long firstBlockOffset;
        for (String term : queryTermsMap.keySet()) {
            firstBlockOffset = lexicon.getLexiconElem(term).getOffset();
            blocksNumber.add(lexicon.getLexiconElem(term).getBlocksNumber());
            //read all blocks
            blockDescriptorList.add(new BlockDescriptorList(firstBlockOffset, blocksNumber.get(i)));
            blockDescriptorList.get(i).openBlock();
            blockDescriptorList.get(i).next();
            //load first posting list for the term
            PostingList postingList = new PostingList();
            postingList.readPostingList(-1, blockDescriptorList.get(i).getNumPosting(), blockDescriptorList.get(i).getPostingListOffset());
            postingList.openList();
            postingLists.add(postingList); // add postinglist of the term to postingListIterators
            postingLists.get(i).next();
            i++;
        }
    }

    private int compute_essential_index(HashMap<String, LexiconElem> queryTermsMap, String scoringFunction, double current_threshold) {
        if (current_threshold == 0)
            return 0;

        int essential_index = -1;
        double TUBsum = 0;
        boolean essential_postings_found = false;
        for (String term : queryTermsMap.keySet()) {
            if (scoringFunction.equals("BM25"))
                TUBsum += queryTermsMap.get(term).getTUB_bm25();
            else if (scoringFunction.equals("TFIDF"))
                TUBsum += queryTermsMap.get(term).getTUB_tfidf();
            if (TUBsum < current_threshold) {
                essential_index++;
            } else {//essential postings found
                essential_postings_found = true;
                break;
            }
        }
        if (essential_postings_found)
            return essential_index;
        else
            return -1;
    }

    private double tfidf(int tf, int df) {
        double score = 0;
        if (tf > 0)
            score = (1 + Math.log(tf)) * Math.log(N_docs / df);
        return score;
    }

    private double BM25(int tf, int df, int docLength, double avgDocLength) {
        //TODO CONTROLLARE LA FORMULA
        double score;
        double k1 = 1.2;
        double b = 0.75;

        double B = ((1 - b) + b * (docLength / avgDocLength));
        double idf = Math.log((N_docs) / (df));
        score = (tf / (k1 * B + tf)) * idf;
        return score;
    }

    private int getNextDocId(ArrayList<Integer> indexes) {
        int min = Integer.MAX_VALUE;

        for (int i = 0; i < postingLists.size(); i++) {
            PostingList postList = postingLists.get(i);
            if (postList == null) // term not in lexicon
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
