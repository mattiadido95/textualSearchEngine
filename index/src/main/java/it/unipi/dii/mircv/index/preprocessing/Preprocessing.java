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

    public Preprocessing(String query, boolean porterStemmerOption) {
        List<String> words = tokenization(query);
        words = removeNumbers(words); // Remove words that contain more than 4 digits
        words = removeWordstop(words); // Remove stopwords
        if (porterStemmerOption) {
            PorterStemmer porterStemmer = new PorterStemmer(); // Stemming
            List<String> stemWords = new ArrayList<>();
            for (String word : words) {
                String stem = porterStemmer.stemWord(word);
                stemWords.add(stem);
            }
            this.tokens = stemWords;
        } else {
            this.tokens = words;
        }
    }

    public Preprocessing(String document, int docCounter, boolean porterStemmerOption) {
        // create new document
        this.doc = new Document(document, docCounter);
        List<String> words = tokenization(doc.getBody());
        words = removeNumbers(words); // Remove words that contain numbers
        words = removeWordstop(words); // Remove stopwords
        if (porterStemmerOption) {
            PorterStemmer porterStemmer = new PorterStemmer(); // Stemming
            List<String> stemWords = new ArrayList<>();
            for (String word : words) {
                String stem = porterStemmer.stemWord(word);
                stemWords.add(stem);
            }
            this.doc.setLength(stemWords.size());
            this.tokens = stemWords;
        } else {
            this.doc.setLength(words.size());
            this.tokens = words;
        }
    }

    // function to remove all words that contain more than 4 digits
    public List<String> removeNumbers(List<String> words) {
        List<Integer> indexList = new ArrayList<>();
        for (int i = 0; i < words.size(); i++) {
            String word = words.get(i);
            if (word.matches(".*\\d.*")) {
                indexList.add(i);
            }
        }
        Collections.reverse(indexList);
        for (int index : indexList) {
            words.remove(index);
        }
        return words;
    }

    public List<String> tokenization(String doc) {
        List<String> words = new ArrayList<>();
        doc = doc.toLowerCase();
        String regex = "\\s+|\\!|\"|\\#|\\$|\\%|\\&|\\'|\\(|\\)|\\*|\\+|"
                + "\\,|\\-|\\.|\\/|\\:|\\;|\\<|\\=|\\>|\\|\\?|\\@|\\[|"
                + "\\]|\\^|\\`|\\{|\\||\\}|\\~";
        String regex2 = "[\\s!\"#$%&'()*+,\\-./:;<=>?@\\[\\]^`{|}~]+";
        Pattern pattern = Pattern.compile(regex);
        Pattern pattern2 = Pattern.compile(regex2);

        String[] tokens = pattern.split(doc);
        for (String token : tokens) {
            String[] subTokens = pattern2.split(token);
            for (String subToken : subTokens) {
                if (!subToken.isEmpty()) {
                    words.add(subToken);
                }
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

    public Document getDoc() {
        return doc;
    }
}
