package it.unipi.dii.mircv.index;

import it.unipi.dii.mircv.index.preprocessing.Preprocessing;
import it.unipi.dii.mircv.index.structures.Document;
import it.unipi.dii.mircv.index.structures.PostingList;
import it.unipi.dii.mircv.index.utility.Logs;
import it.unipi.dii.mircv.index.utility.MemoryManager;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class Main {
    private static final String COLLECTION_PATH = "data/collection/collection.tsv";

    private static Logs log = new Logs(); // create a log object to print log messages

    public static void main(String[] args) {

        // log.getLog("Start indexing ...");

        try {

            HashMap<String, PostingList> invertedIndex = new HashMap<>(); // create an invertedIndex with an hashmap linking each token to its posting list

            BufferedReader br = new BufferedReader(new FileReader(COLLECTION_PATH)); // open buffer to read documents
            String line; // start reading document by document

            while ((line = br.readLine()) != null) {

                MemoryManager manageMemory = new MemoryManager();
                // log.getLog(manageMemory);
                if (manageMemory.checkFreeMemory()) {
                    log.getLog("Memory is full, suspend indexing and save invertedIndex to disk");
                    // TODO save inverted index to disk, call to memoryManager method
                    break; // TODO remove this break and convert to continue
                }

                Preprocessing preprocessing = new Preprocessing(line);
                Document document = preprocessing.getDoc(); // for each document, start preprocessing
                List<String> tokens = preprocessing.tokens; // and return a list of tokens

                for (String token : tokens) {
                    addElementToInvertedIndex(invertedIndex, token, document); // add token to the inverted index
                }

                // log.getLog(invertedIndex);

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void addElementToInvertedIndex(HashMap invertedIndex, String token, Document document) {
        // check if the token is already in the inverted index and manage the update the posting list
        if (invertedIndex.containsKey(token)) {
            // update posting list for existing token
            PostingList postingList = (PostingList) invertedIndex.get(token); // get the posting list of the existing token
            postingList.updatePostingList(token, document); // update the posting list
            log.getLog(postingList);
        } else {
            // create new posting list for new token
            PostingList postingList = new PostingList(token, document); // create a new posting list for new token
            invertedIndex.put(token, postingList); // add the posting list to the inverted index
        }
    }
}














