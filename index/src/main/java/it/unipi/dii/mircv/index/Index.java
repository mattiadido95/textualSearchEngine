package it.unipi.dii.mircv.index;

import it.unipi.dii.mircv.index.preprocessing.Preprocessing;
import it.unipi.dii.mircv.index.structures.*;
import it.unipi.dii.mircv.index.utility.Logs;
import it.unipi.dii.mircv.index.utility.MemoryManager;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Index {
    private static final String COLLECTION_PATH = "data/collection/collection.tsv";
    private static Logs log = new Logs(); // create a log object to print log messages
    private static int indexCounter = 0;

    public static void main(String[] args) {

        log.getLog("Start indexing ...");

        deleteFiles("data/index/");
        deleteFiles("data/index/lexicon/");
        deleteFiles("data/index/documents/");

        log.getLog("Deleted old index files ...");

        try {
            Lexicon lexicon = new Lexicon(); // create a lexicon
            HashMap<String, PostingList> invertedIndex = new HashMap<>(); // create an invertedIndex with an hashmap linking each token to its posting list
            ArrayList<Document> documents = new ArrayList<>(); // create an array of documents

            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(COLLECTION_PATH), "UTF-8"));// open buffer to read documents
            String line; // start reading document by document

            int documentCounter = 0;

            while ((line = br.readLine()) != null) {

                MemoryManager manageMemory = new MemoryManager();
                //if (manageMemory.checkFreeMemory()) {

                Preprocessing preprocessing = new Preprocessing(line, documentCounter);
                Document document = preprocessing.getDoc(); // for each document, start preprocessing
                List<String> tokens = preprocessing.tokens; // and return a list of tokens
                documents.add(document); // add document to the array of documents

                for (String token : tokens) {
                    lexicon.addLexiconElem(token); // add token to the lexicon
                    int newDf = addElementToInvertedIndex(invertedIndex, token, document); // add token to the inverted index
                    lexicon.setDf(token, newDf);
                }

                documentCounter++;

                if(documentCounter % 250000 == 0){
                    log.getLog("Processed: " + documentCounter + " documents");
//                    log.getLog("Memory is full, suspend indexing, save invertedIndex to disk and clear memory ...");
                    manageMemory.saveInvertedIndexToDisk(lexicon, invertedIndex, indexCounter); // save inverted index to disk
                    Document.saveDocumentsToDisk(documents,indexCounter); // save documents to disk
                    manageMemory.clearMemory(lexicon, invertedIndex, documents); // clear inverted index and document index from memory
                    invertedIndex = new HashMap<>(); // create a new inverted index
                    indexCounter += 1;
                    //log.getLog(manageMemory); // print memory status after clearing memory

                    // TODO VA FATTA LA SORT DEI TERMINI prima di salvare su disco (forse non serve piu)
                }

                if (documentCounter % 10 == 0) {
                    // TODO TESTING dei primi 30 documenti -> 3 file diversi
                    log.getLog("Processed: " + documentCounter + " documents");
//                    log.getLog("Memory is full, suspend indexing, save invertedIndex to disk and clear memory ...");
                    //save Structures to disk
                    manageMemory.saveInvertedIndexToDisk(lexicon, invertedIndex, indexCounter); // save inverted index to disk
                    Document.saveDocumentsToDisk(documents,indexCounter); // save documents to disk
                    manageMemory.clearMemory(lexicon, invertedIndex, documents); // clear inverted index and document index from memory
                    //TODO serve davvero fare la new
                    invertedIndex = new HashMap<>(); // create a new inverted index

                    //read Structures from disk
                    lexicon.readLexiconFromDisk(indexCounter);
                    // per ogni chiave del lexicon, leggi il posting list dal file
                    for(String key : lexicon.getLexicon().keySet()){
                        //get lexicon elem
                        LexiconElem lexiconElem = lexicon.getLexiconElem(key);
                        PostingList postingList = new PostingList();
                        postingList.readPostingList(indexCounter, lexiconElem.getDf(), lexiconElem.getOffset());
                        System.out.println(lexiconElem);
                        System.out.println(postingList);
                    }
                    // clear per sicurezza
                    manageMemory.clearMemory(lexicon, invertedIndex, documents); // clear inverted index and document index from memory
                    invertedIndex = new HashMap<>(); // create a new inverted index

                    ArrayList<Document> documents1 = Document.readDocuments(indexCounter);
                    System.out.println(documents1);
                    indexCounter += 1;
                    if(documentCounter == 30)
                        break;
                }

//                if (documentCounter % 500000 == 0) {
//                    log.getLog(invertedIndex);
//                    log.getLog(lexicon);
//                    log.getLog("Processed: " + documentCounter + " documents");
//                }

                // TODO FARE MERGE INDICI E VOCABOLARIO
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static int addElementToInvertedIndex(HashMap invertedIndex, String token, Document document) {
        // check if the token is already in the inverted index and manage the update the posting list
        if (invertedIndex.containsKey(token)) {
            // update posting list for existing token
            PostingList postingList = (PostingList) invertedIndex.get(token); // get the posting list of the existing token
            postingList.updatePostingList(document); // update the posting list
            return postingList.getPostingListSize(); // return the size of the posting list
        } else {
            // create new posting list for new token
            PostingList postingList = new PostingList(document); // create a new posting list for new token
            invertedIndex.put(token, postingList); // add the posting list to the inverted index
            return postingList.getPostingListSize(); // return the size of the posting list
        }
    }


    private static void deleteFiles(String folderPath) {
        File folder = new File(folderPath);
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        file.delete();
                    }
                }
            }
        }
        //create folder if not exists
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

}














