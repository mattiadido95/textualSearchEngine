package it.unipi.dii.mircv.index.algorithms;

import it.unipi.dii.mircv.index.preprocessing.Preprocessing;
import it.unipi.dii.mircv.index.structures.*;
import it.unipi.dii.mircv.index.utility.Logs;
import it.unipi.dii.mircv.index.utility.MemoryManager;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import javax.management.loading.PrivateClassLoader;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Spimi {
    private String COLLECTION_PATH;

    private Logs log;
    private int indexCounter;
    private int documentCounter;
    private long totDocLength;
    private static final String LEXICON_PATH = "data/index/lexicon/";
    private static final String DOCUMENTS_PATH = "data/index/documents/";
    private static final String PARTIAL_DOCUMENTS_PATH = "data/index/documents/documents_";
    private static final String INDEX_PATH = "data/index/";

    private static final int MAX_DOC_PER_FILE = 250000;


    public Spimi(String collection) {
        this.COLLECTION_PATH = collection;
        this.log = new Logs();// create a log object to print log messages
        this.indexCounter = 0;
    }

    public String getCOLLECTION_PATH() {
        return COLLECTION_PATH;
    }

    public Logs getLog() {
        return log;
    }

    public int getIndexCounter() {
        return indexCounter;
    }

    public void execute() throws IOException {

        log.getLog("Start indexing ...");

        deleteFiles("data/index/", "bin");
        deleteFiles("data/index/lexicon/", "bin");
        deleteFiles("data/index/documents/", "bin");

        log.getLog("Deleted old index files ...");


        TarArchiveInputStream tarArchiveInputStream = new TarArchiveInputStream(new GzipCompressorInputStream(new FileInputStream(COLLECTION_PATH)));
        tarArchiveInputStream.getNextEntry();


        // open buffer to read documents
        try (BufferedReader br = new BufferedReader(new InputStreamReader(tarArchiveInputStream, "UTF-8"))) {
            Lexicon lexicon = new Lexicon(); // create a lexicon
            HashMap<String, PostingList> invertedIndex = new HashMap<>(); // create an invertedIndex with an hashmap linking each token to its posting list
            ArrayList<Document> documents = new ArrayList<>(); // create an array of documents
            MemoryManager manageMemory = new MemoryManager();

            String line; // start reading document by document

            totDocLength = 0;

            while ((line = br.readLine()) != null) {

                //if (manageMemory.checkFreeMemory()) {

                Preprocessing preprocessing = new Preprocessing(line, documentCounter);
                Document document = preprocessing.getDoc(); // for each document, start preprocessing
                List<String> tokens = preprocessing.tokens; // and return a list of tokens
                documents.add(document); // add document to the array of documents
                totDocLength += document.getLength();

                for (String token : tokens) {
                    lexicon.addLexiconElem(token); // add token to the lexicon
                    int newDf = addElementToInvertedIndex(invertedIndex, token, document); // add token to the inverted index
                    lexicon.setDf(token, newDf);
                }

                documentCounter++;

                if (documentCounter % MAX_DOC_PER_FILE == 0) {
                    log.getLog("Processed: " + documentCounter + " documents");
//                    log.getLog("Memory is full, suspend indexing, save invertedIndex to disk and clear memory ...");
                    //save Structures to disk
                    manageMemory.saveInvertedIndexToDisk(lexicon, invertedIndex, indexCounter); // save inverted index to disk
                    Document.saveDocumentsToDisk(documents, indexCounter, PARTIAL_DOCUMENTS_PATH); // save documents to disk
                    manageMemory.clearMemory(lexicon, invertedIndex, documents); // clear inverted index and document index from memory
                    //TODO serve davvero fare la new
//                    invertedIndex = new HashMap<>(); // create a new inverted index

//                    //read Structures from disk
//                    lexicon.readLexiconFromDisk(indexCounter);
//                    // per ogni chiave del lexicon, leggi il posting list dal file
//                    for (String key : lexicon.getLexicon().keySet()) {
//                        //get lexicon elem
//                        LexiconElem lexiconElem = lexicon.getLexiconElem(key);
//                        PostingList postingList = new PostingList();
//                        postingList.readPostingList(indexCounter, lexiconElem.getDf(), lexiconElem.getOffset());
//                        System.out.println(lexiconElem);
//                        System.out.println(postingList);
//                    }
                    // clear per sicurezza
//                    manageMemory.clearMemory(lexicon, invertedIndex, documents); // clear inverted index and document index from memory
//                    invertedIndex = new HashMap<>(); // create a new inverted index

//                    ArrayList<Document> documents1 = Document.readDocumentsFromDisk(indexCounter);
//                    System.out.println(documents1);
                    indexCounter += 1;
//                    if (documentCounter == 20000)
//                        break;
                }


            }
            if (!documents.isEmpty()) {
                log.getLog("Processed: " + documentCounter + " documents");

                manageMemory.saveInvertedIndexToDisk(lexicon, invertedIndex, indexCounter); // save inverted index to disk
                Document.saveDocumentsToDisk(documents, indexCounter, PARTIAL_DOCUMENTS_PATH); // save documents to disk
                manageMemory.clearMemory(lexicon, invertedIndex, documents); // clear inverted index and document index from memory

                indexCounter += 1;
            }
            //save into disk documentCounter
            FileOutputStream fileOut = new FileOutputStream("data/index/documentInfo.bin");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(documentCounter);
            out.writeObject(totDocLength);
            out.close();
            fileOut.close();
            tarArchiveInputStream.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        log.getLog("Indexing completed ...");
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


    private static void deleteFiles(String folderPath, String extension) {
        File folder = new File(folderPath);
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith("." + extension)) {
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
