package it.unipi.dii.mircv.index.algorithms;

import it.unipi.dii.mircv.index.preprocessing.Preprocessing;
import it.unipi.dii.mircv.index.structures.*;
import it.unipi.dii.mircv.index.utility.Logs;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

// Class-level variables and constructor...

/**
 * The Spimi class implements the SPIMI (Single-pass in-memory indexing) algorithm
 * for building an inverted index from a collection of documents.
 * It manages indexing, preprocessing, and data storage.
 */
public class Spimi {
    private String COLLECTION_PATH;
    private Logs log;
    private int indexCounter;
    private int documentCounter;
    private long totDocLength;
    private boolean compressed_reading;
    private boolean porterStemmer;
    private static final String PARTIAL_DOCUMENTS_PATH = "data/index/documents/documents_";
    private static final int MAX_DOC_PER_FILE = 250000;
    private static final String PARTIAL_INDEX_PATH = "data/index/index_";
    private static final String PARTIAL_LEXICON_PATH = "data/index/lexicon/lexicon_";

    /**
     * Constructs a new Spimi indexer.
     *
     * @param collection         The path to the collection of documents to be indexed.
     * @param porterStemmer      A boolean indicating whether Porter stemming should be applied during preprocessing.
     * @param compressed_reading A boolean indicating whether the collection is compressed in tar.gz format.
     */
    public Spimi(String collection, boolean porterStemmer, boolean compressed_reading) {
        this.COLLECTION_PATH = collection;
        this.log = new Logs();// create a log object to print log messages
        this.indexCounter = 0;
        this.compressed_reading = compressed_reading;
        this.porterStemmer = porterStemmer;
    }

    public int getIndexCounter() {
        return indexCounter;
    }

    /**
     * Executes the SPIMI (Single-pass in-memory indexing) algorithm to build an inverted index
     * from a collection of documents.
     *
     * @throws IOException If there are any issues with reading or writing files.
     */
    public void execute() throws IOException {

        log.getLog("Start indexing ...");
        deleteFiles("data/index/", "bin");
        deleteFiles("data/index/lexicon/", "bin");
        deleteFiles("data/index/documents/", "bin");
        log.getLog("Deleted old index files ...");

        TarArchiveInputStream tarArchiveInputStream = null;
        // open buffer to read documents
        try {
            BufferedReader br;

            if (compressed_reading) {
                tarArchiveInputStream = new TarArchiveInputStream(new GzipCompressorInputStream(new FileInputStream(COLLECTION_PATH)));
                tarArchiveInputStream.getNextEntry();
                br = new BufferedReader(new InputStreamReader(tarArchiveInputStream, "UTF-8"));
            } else {
                br = new BufferedReader(new InputStreamReader(new FileInputStream(COLLECTION_PATH), "UTF-8"));
            }

            Lexicon lexicon = new Lexicon(); // create a lexicon
            HashMap<String, PostingList> invertedIndex = new HashMap<>(); // create an invertedIndex with an hashmap linking each token to its posting list
            ArrayList<Document> documents = new ArrayList<>(); // create an array of documents

            String line; // start reading document by document
            totDocLength = 0;
            while ((line = br.readLine()) != null) {
                Preprocessing preprocessing = new Preprocessing(line, documentCounter, porterStemmer);
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
                    //save Structures to disk
                    saveInvertedIndexToDisk(lexicon, invertedIndex, indexCounter); // save inverted index to disk
                    Document.saveDocumentsToDisk(documents, indexCounter, PARTIAL_DOCUMENTS_PATH); // save documents to disk
                    clearMemory(lexicon, invertedIndex, documents); // clear inverted index and document index from memory
                    indexCounter += 1;
                }
//                if (documentCounter == 3 * MAX_DOC_PER_FILE)
//                        break;
            }
            if (!documents.isEmpty()) {
                log.getLog("Processed: " + documentCounter + " documents");
                saveInvertedIndexToDisk(lexicon, invertedIndex, indexCounter); // save inverted index to disk
                Document.saveDocumentsToDisk(documents, indexCounter, PARTIAL_DOCUMENTS_PATH); // save documents to disk
                clearMemory(lexicon, invertedIndex, documents); // clear inverted index and document index from memory
                indexCounter += 1;
            }

            //save into disk documentCounter
            FileOutputStream fileOut = new FileOutputStream("data/index/documentInfo.bin");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(documentCounter);
            out.writeObject(totDocLength);
            out.close();
            fileOut.close();
            if (compressed_reading)
                tarArchiveInputStream.close();
            br.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        log.getLog("Indexing completed ...");
    }

    /**
     * Adds a token to the inverted index, updating the corresponding posting list for the token.
     * If the token is not already in the inverted index, a new posting list is created.
     *
     * @param invertedIndex A HashMap representing the inverted index with tokens as keys and posting lists as values.
     * @param token         The token to be added to the inverted index.
     * @param document      The document associated with the token.
     * @return The size of the updated or newly created posting list.
     */
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

    /**
     * Deletes files with a specified extension from a given folder path. If the folder does not exist, it creates it.
     *
     * @param folderPath The path of the folder from which files should be deleted.
     * @param extension  The file extension to filter files for deletion.
     */
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

    /**
     * Saves the inverted index and lexicon to disk.
     *
     * @param lexicon       The lexicon to save.
     * @param invertedIndex The inverted index to save.
     * @param indexCounter  The index counter used to distinguish different index segments.
     */
    public void saveInvertedIndexToDisk(Lexicon lexicon, HashMap<String, PostingList> invertedIndex, int indexCounter) {

        for (String term : lexicon.getLexicon().keySet()) {
            // for each term in lexicon
            PostingList postingList = invertedIndex.get(term); // get corresponding posting list from inverted index
            long offset = postingList.savePostingListToDisk(indexCounter, PARTIAL_INDEX_PATH); // save posting list to disk and get offset of file
            lexicon.getLexicon().get(term).setOffset(offset); // set offset of term in the lexicon
        }
        lexicon.saveLexiconToDisk(indexCounter, PARTIAL_LEXICON_PATH); // save lexicon to disk
        lexicon.getLexicon().clear(); // clear lexicon
    }

    /**
     * Clears memory by clearing the inverted index, documents, and lexicon.
     *
     * @param lexicon       The lexicon to clear.
     * @param invertedIndex The inverted index to clear.
     * @param docs          The list of documents to clear.
     */
    public void clearMemory(Lexicon lexicon, HashMap<String, PostingList> invertedIndex, ArrayList<Document> docs) {
        invertedIndex.clear();  // clear index
        docs.clear(); // clear docs
        lexicon.getLexicon().clear(); // clear lexicon
    }
}
