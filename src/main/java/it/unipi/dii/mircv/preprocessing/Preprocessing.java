package it.unipi.dii.mircv.preprocessing;

import java.io.*;
import java.util.*;
import java.util.regex.*;

public class Preprocessing {
    private Map<String, Map<String, Integer>> index;

    public Preprocessing(String path) {
        index = new HashMap<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(path));
            String line;
            int c = 0;
            while ((line = reader.readLine()) != null) {
                List<String> words = tokenization(line);

                String id = words.get(0);
                words.remove(0);
                // Stopword
                words = removeWordstop(words);
                // Stemming (not provided in your code)
                // Index
                buildIndex(id, words);

                c++;
                if (c == 200) {
                    for (Map.Entry<String, Map<String, Integer>> entry : index.entrySet()) {
                        System.out.print(entry.getKey() + ": ");
                        Map<String, Integer> inMap = entry.getValue();
                        for (Map.Entry<String, Integer> innerEntry : inMap.entrySet()) {
                            System.out.print("(" + innerEntry.getKey() + "," + innerEntry.getValue() + "),");
                        }
                        System.out.println();
                    }
                    break;
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<String> tokenization(String doc) {
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
            BufferedReader file = new BufferedReader(new FileReader("../../data/stop_words_english.txt"));
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
        for (String stopword : stopwords) {
            int res = Collections.binarySearch(words, stopword);
            if (res >= 0) {
                indexList.add(res);
            }
        }
        Collections.reverse(indexList);
        for (int index : indexList) {
            words.remove(index);
        }
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

    public static void main(String[] args) {
        Preprocessing preprocessing = new Preprocessing("path/to/your/input/file.txt");
    }
}
