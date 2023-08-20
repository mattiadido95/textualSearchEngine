package it.unipi.dii.mircv.index.preprocessing;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import ca.rmen.porterstemmer.PorterStemmer;
import it.unipi.dii.mircv.index.structures.Document;


public class Preprocessing {

    private static final String STOPWORDS_PATH = "data/stop_words_english.txt";

    private Map<String, Map<String, Integer>> index;

    private Document doc;

    public List<String> tokens = new ArrayList<>();

    public Preprocessing(String document,int docCounter){
        // create new document
        this.doc = new Document(document,docCounter);
        List<String> words = tokenization(doc.getBody());
        words = removeWordstop(words); // Remove stopwords
        PorterStemmer porterStemmer = new PorterStemmer(); // Stemming
        List<String> stemWords = new ArrayList<>();
        for (String word : words) {
            String stem = porterStemmer.stemWord(word);
            stemWords.add(stem);
        }
        this.doc.setLength(words.size());
        this.tokens = stemWords;

    }

//    public Preprocessing() {
//        index = new HashMap<>();
//        try {
//            BufferedReader reader = new BufferedReader(new FileReader(""));
//            String line;
//            int c = 0;
//            while ((line = reader.readLine()) != null) {
//                List<String> words = tokenization(line); // Tokenization
//                String id = words.get(0);
//                words.remove(0);
//                words = removeWordstop(words); // Remove stopwords
//                PorterStemmer porterStemmer = new PorterStemmer(); // Stemming
//                List<String> stemWords = new ArrayList<>();
//                for (String word : words) {
//                    String stem = porterStemmer.stemWord(word);
//                    stemWords.add(stem);
//                }
//                buildIndex(id, stemWords); // Build index
//
////                c++;
////                if (c == 200000) {
//                for (Map.Entry<String, Map<String, Integer>> entry : index.entrySet()) {
//                    System.out.print(entry.getKey() + ": ");
//                    Map<String, Integer> inMap = entry.getValue();
//                    for (Map.Entry<String, Integer> innerEntry : inMap.entrySet()) {
//                        System.out.print("(" + innerEntry.getKey() + "," + innerEntry.getValue() + "),");
//                    }
//                    System.out.println();
//                }
////                    break;
////                }
//            }
//            reader.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    public List<String> tokenization(String doc) {
        List<String> words = new ArrayList<>();
        doc = doc.toLowerCase();
        String regex = "\\s+|\\!|\"|\\#|\\$|\\%|\\&|\\'|\\(|\\)|\\*|\\+|"
                + "\\,|\\-|\\.|\\/|\\:|\\;|\\<|\\=|\\>|\\|\\?|\\@|\\[|"
                + "\\]|\\^|\\`|\\{|\\||\\}|\\~";
        Pattern pattern = Pattern.compile(regex);
        String[] tokens = pattern.split(doc);
        for (String token : tokens) {
            if (!token.isEmpty()) {
                words.add(token);
            }
        }
        return words;
    }

    private List<String> getStopwords() {
        List<String> stopwords = new ArrayList<>();
        try {
            BufferedReader file = new BufferedReader(new FileReader(STOPWORDS_PATH));
            String line;
            while ((line = file.readLine()) != null) {
                stopwords.add(line);
            }
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stopwords;
    }

    private List<String> removeWordstop(List<String> words) {
        List<String> stopwords = getStopwords();
        List<Integer> indexList = new ArrayList<>();
        Collections.sort(words);
        // remove stopwords from words list
        words.removeAll(stopwords);
        return words;
    }

    private void buildIndex(String id, List<String> words) {
        Map<String, Integer> postingList;
        int frequency;
        for (String word : words) {
            if (index.containsKey(word)) {
                postingList = index.get(word);
                if (postingList.containsKey(id)) {
                    frequency = postingList.get(id) + 1;
                } else {
                    frequency = 1;
                }
            } else {
                postingList = new HashMap<>();
                frequency = 1;
            }
            postingList.put(id, frequency);
            index.put(word, postingList);
        }
    }


    public Document getDoc() {
        return doc;
    }
}
