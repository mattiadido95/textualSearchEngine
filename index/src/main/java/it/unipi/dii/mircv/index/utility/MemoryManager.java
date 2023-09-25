package it.unipi.dii.mircv.index.utility;

import it.unipi.dii.mircv.index.structures.Document;
import it.unipi.dii.mircv.index.structures.Lexicon;
import it.unipi.dii.mircv.index.structures.PostingList;

import javax.print.Doc;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * This class manages memory and provides methods for saving and clearing data structures.
 */
public class MemoryManager {

    private long freeMemoryPercentage = 0;

    private Logs log = new Logs();

    private String filePath = "data/index/invertedIndex.txt";
    private static final String PARTIAL_LEXICON_PATH = "data/index/lexicon/lexicon_";
    private static final String PARTIAL_DOCUMENTS_PATH = "data/index/documents/documents_";
    private static final String PARTIAL_INDEX_PATH = "data/index/index_";

    public MemoryManager() {
        setFreeMemoryPercentage();
    }

    public void printMemory(String timestamp) {
        long freeMemoryMB = bytesToMegabytes(getFreeMemory());
        long totalMemoryMB = bytesToMegabytes(getTotalMemory());

        System.out.println("["+timestamp+"] Memory status:");
        System.out.println(" -> Free memory: " + freeMemoryMB + " MB");
        System.out.println(" -> Total memory: " + totalMemoryMB + " MB");
        System.out.println(" -> Free memory percentage: " + this.freeMemoryPercentage + "%");
        System.out.println("**************************************");
    }

    private long bytesToMegabytes(long bytes) {
        return bytes / (1024 * 1024); // 1 megabyte = 1024 * 1024 bytes
    }


    private long getFreeMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.freeMemory();
    }

    private long getTotalMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory();
    }

    private void setFreeMemoryPercentage() {
        this.freeMemoryPercentage = (bytesToMegabytes(getFreeMemory()) * 100) / bytesToMegabytes(getTotalMemory());
    }

    public boolean checkFreeMemory() {
        return this.freeMemoryPercentage > 10 ? false : true;
    }

    /**
     * Saves the inverted index and lexicon to disk.
     *
     * @param lexicon      The lexicon to save.
     * @param invertedIndex The inverted index to save.
     * @param indexCounter The index counter used to distinguish different index segments.
     */
    public void saveInvertedIndexToDisk(Lexicon lexicon, HashMap<String, PostingList> invertedIndex, int indexCounter){

        for (String term : lexicon.getLexicon().keySet()) {
            // for each term in lexicon
//            int df = lexicon.getLexicon().get(term).getDf(); // get df of term
            PostingList postingList = invertedIndex.get(term); // get corresponding posting list from inverted index
            long offset = postingList.savePostingListToDisk(indexCounter,PARTIAL_INDEX_PATH); // save posting list to disk and get offset of file
            lexicon.getLexicon().get(term).setOffset(offset); // set offset of term in the lexicon

//            PostingList readedPostingList = new PostingList();
//            readedPostingList.readPostingList(indexCounter, df, offset);
//
//            System.out.println("**********CHECKING POSTING LIST*********");
//            System.out.println("Posting list readed from disk: " + readedPostingList.toString());
//            System.out.println("Posting list saved to disk: " + postingList.toString());
//            System.out.println("**************************************");
        }
//        log.getLog("End index saving to disk");

        lexicon.saveLexiconToDisk(indexCounter,PARTIAL_LEXICON_PATH); // save lexicon to disk
        lexicon.getLexicon().clear(); // clear lexicon
//        lexicon.readLexiconFromDisk(indexCounter); // read lexicon from disk

//        log.getLog("End lexicon saving to disk");

//        System.out.println("**********CHECKING LEXICON*********");
//        System.out.println("Lexicon saved to disk: " + lexicon.toString());
//        System.out.println("**************************************");



        // TODO implement reading from file
/*
            // read object from file
            try (FileInputStream fileIn = new FileInputStream(filePath);
             ObjectInputStream objectIn = new ObjectInputStream(fileIn)) {

                while (true) {
                    try {
                        HashMap<String, PostingList> invertedIndexBlock = (HashMap<String, PostingList>) objectIn.readObject();
                        if (invertedIndexBlock == null) {
                            break; // Fine del file
                        }

                        // Processa il blocco di dati letto
                        processInvertedIndexBlock(invertedIndexBlock);
                    } catch (EOFException | ClassNotFoundException e) {
                        // Fine del file o errore di deserializzazione
                        break;
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
*/
    }

     /**
     * Clears memory by clearing the inverted index, documents, and lexicon.
     *
     * @param lexicon      The lexicon to clear.
     * @param invertedIndex The inverted index to clear.
     * @param docs         The list of documents to clear.
     */
    public void clearMemory(Lexicon lexicon, HashMap<String, PostingList> invertedIndex, ArrayList<Document> docs){
        invertedIndex.clear();  // clear index
        docs.clear(); // clear docs
        lexicon.getLexicon().clear(); // clear lexicon
    }
}
