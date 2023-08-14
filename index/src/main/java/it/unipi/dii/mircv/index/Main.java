package it.unipi.dii.mircv.index;

import ca.rmen.porterstemmer.PorterStemmer;
import it.unipi.dii.mircv.index.preprocessing.Preprocessing;
import it.unipi.dii.mircv.index.structures.Document;
import it.unipi.dii.mircv.index.structures.Posting;
import it.unipi.dii.mircv.index.structures.PostingList;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Main {
    private static final String COLLECTION_PATH = "data/collection/collection.tsv";

    // TODO manage memory space using an hashmap



    public static void main(String[] args) {

        System.out.println("Start indexing ...");

        try {
            // open buffer to read from collection.tsv
            BufferedReader br = new BufferedReader(new FileReader(COLLECTION_PATH));
            String line;

            // create an inverted index with an hashmap linking each token to its posting list
            HashMap<String, PostingList> invertedIndex = new HashMap<>();

            // start reading document by document
            while ((line = br.readLine()) != null) {

                // TODO manage memory occupation of the inverted index with Runtime library

                // for each document, start preprocessing and return a list of tokens
                Preprocessing preprocessing = new Preprocessing(line);
                Document document = preprocessing.getDoc();
                List<String> tokens = preprocessing.tokens;

                // for each token, create a Posting List
                for (String token : tokens) {
                    PostingList postingList = new PostingList(token, document);
                    // add the posting list to the inverted index
                    invertedIndex.put(token, postingList);
                    // TODO check if the token is already in the inverted index and manage the update the posting list
                }


            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
}