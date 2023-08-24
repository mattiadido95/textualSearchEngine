package it.unipi.dii.mircv.index;

import it.unipi.dii.mircv.index.algorithms.Merger;
import it.unipi.dii.mircv.index.algorithms.Spimi;
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

        Spimi spimi = new Spimi(COLLECTION_PATH);
        spimi.execute();
        Merger merger = new Merger(COLLECTION_PATH, spimi.getIndexCounter());
        merger.execute();

    }
}














