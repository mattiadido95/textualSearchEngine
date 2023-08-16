package it.unipi.dii.mircv.index.utility;

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

    public void saveInvertedIndexToDisk(HashMap<String, PostingList> invertedIndex) {
        // TODO implement method to save inverted index to disk

        // write object to file
        try (FileOutputStream fileOut = new FileOutputStream(filePath, true);
             ObjectOutputStream objectOut = new ObjectOutputStream(fileOut)) {

            objectOut.writeObject(invertedIndex);
            objectOut.writeObject(null); // stopping condition for reading an hashmap object

            log.getLog("The Object  was successfully written into invertedIndex.txt");

        } catch (IOException e) {
            e.printStackTrace();
        }
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
