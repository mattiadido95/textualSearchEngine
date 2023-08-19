package it.unipi.dii.mircv.index;

import it.unipi.dii.mircv.index.preprocessing.Preprocessing;
import it.unipi.dii.mircv.index.structures.Document;
import it.unipi.dii.mircv.index.structures.Lexicon;
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
    private static int indexCounter = 0;

    public static void main(String[] args) {

        log.getLog("Start indexing ...");

        try {
            Lexicon lexicon = new Lexicon(); // create a lexicon
            HashMap<String, PostingList> invertedIndex = new HashMap<>(); // create an invertedIndex with an hashmap linking each token to its posting list

            BufferedReader br = new BufferedReader(new FileReader(COLLECTION_PATH)); // open buffer to read documents
            String line; // start reading document by document

            int documentCounter = 0;

            while ((line = br.readLine()) != null) {

                MemoryManager manageMemory = new MemoryManager();
                if (manageMemory.checkFreeMemory()) {
                    //log.getLog("Memory is full, suspend indexing, save invertedIndex to disk and clear memory ...");
                    // TODO VA FATTA LA SORT DEI TERMINI prima di salvare su disco
                    // TODO verificare se le classi posting e posting list vanno davvero fatte serializzabili
                    indexCounter += 1;
                    manageMemory.saveInvertedIndexToDisk(invertedIndex, indexCounter); // save inverted index to disk
                    manageMemory.clearMemory(invertedIndex); // clear inverted index from memory
                    invertedIndex = new HashMap<>(); // create a new inverted index
                    //log.getLog(manageMemory); // print memory status after clearing memory
                }

                Preprocessing preprocessing = new Preprocessing(line, documentCounter);
                Document document = preprocessing.getDoc(); // for each document, start preprocessing
                List<String> tokens = preprocessing.tokens; // and return a list of tokens

                for (String token : tokens) {
                    lexicon.addLexiconElem(token); // add token to the lexicon
                    addElementToInvertedIndex(invertedIndex, token, document); // add token to the inverted index
                }

                documentCounter++;
                if (documentCounter % 500000 == 0) {
                    log.getLog(invertedIndex);
                    log.getLog(lexicon);
                    log.getLog("Processed: " + documentCounter + " documents");
                }

                // TODO FARE MERGE INDICI E VOCABOLARIO
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
            postingList.updatePostingList(document); // update the posting list
        } else {
            // create new posting list for new token
            PostingList postingList = new PostingList(document); // create a new posting list for new token
            invertedIndex.put(token, postingList); // add the posting list to the inverted index
        }
    }
}














