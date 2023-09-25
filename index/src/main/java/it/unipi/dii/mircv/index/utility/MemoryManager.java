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

        System.out.println("[" + timestamp + "] Memory status:");
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


}
