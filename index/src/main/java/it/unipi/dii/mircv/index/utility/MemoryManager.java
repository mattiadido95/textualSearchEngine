package it.unipi.dii.mircv.index.utility;

import it.unipi.dii.mircv.index.structures.Document;
import it.unipi.dii.mircv.index.structures.Lexicon;
import it.unipi.dii.mircv.index.structures.PostingList;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;



public class MemoryManager {

    private long freeMemoryPercentage = 0;

    private Logs log = new Logs();

    private String filePath = "data/index/invertedIndex.txt";

    public MemoryManager() {
        setFreeMemoryPercentage();
    }

    public void printMemory() {
        long freeMemoryMB = bytesToMegabytes(getFreeMemory());
        long totalMemoryMB = bytesToMegabytes(getTotalMemory());

        System.out.println("Memory status:");
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

    public void saveInvertedIndexToDisk(Lexicon lexicon, HashMap<String, PostingList> invertedIndex, int indexCounter){

        for (String term : lexicon.getLexicon().keySet()) {
            // for each term in lexicon
            int df = lexicon.getLexicon().get(term).getDf(); // get df of term
            PostingList postingList = invertedIndex.get(term); // get corresponding posting list from inverted index
            long offset = postingList.savePostingListToDisk(indexCounter); // save posting list to disk and get offset of file
            lexicon.getLexicon().get(term).setOffset(offset); // set offset of term in the lexicon


            PostingList readedPostingList = new PostingList();
            readedPostingList.readPostingList(indexCounter, df, offset);

//            System.out.println("**********CHECKING POSTING LIST*********");
//            System.out.println("Posting list readed from disk: " + readedPostingList.toString());
//            System.out.println("Posting list saved to disk: " + postingList.toString());
//            System.out.println("**************************************");
        }

        lexicon.saveLexiconToDisk(indexCounter); // save lexicon to disk
        lexicon.getLexicon().clear(); // clear lexicon
        lexicon.readLexiconFromDisk(indexCounter); // read lexicon from disk

        System.out.println("**********CHECKING LEXICON*********");
        System.out.println("Lexicon saved to disk: " + lexicon.toString());
        System.out.println("**************************************");



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

    public void clearMemory(HashMap<String, PostingList> invertedIndex) {
        invertedIndex.clear();  // clear index
        invertedIndex = null; // set inverted index to null to free memory
    }
}
