package it.unipi.dii.mircv.index.utility;

import it.unipi.dii.mircv.index.structures.Lexicon;
import it.unipi.dii.mircv.index.structures.PostingList;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class Logs {

    private SimpleDateFormat dateFormat;

    public Logs() {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    private String getFormattedTimestamp() {
        return dateFormat.format(new Date());
    }

    public <T> void getLog(T element) {
        String typeObj = element.getClass().getSimpleName();
        String timestamp = getFormattedTimestamp();

        switch (typeObj) {
            case "PostingList":
                logPostingList((PostingList) element, timestamp);
                break;
            case "Document":
                // TODO implement log function for Document
                break;
            case "Posting":
                // TODO implement log function for Posting
                break;
            case "HashMap":
                logHashMap((HashMap) element, timestamp);
                break;
            case "String":
                logString((String) element, timestamp);
                break;
            case "MemoryManager":
                logMemoryManager((MemoryManager) element, timestamp);
                break;
            case "Lexicon":
                logLexicon((Lexicon) element, timestamp);
                break;
            default:
                // TODO implement log function for default and show log error
                break;
        }
    }

    private void logLexicon(Lexicon lexicon, String timestamp) {
        lexicon.printLexicon(timestamp);
    }

    private void logPostingList(PostingList postingList, String timestamp) {
        postingList.toString();
    }

    private void logHashMap(HashMap invertedIndex, String timestamp) {
        System.out.println("["+timestamp+"] InvertedIndex status: ");
        System.out.println(" -> Size: " + invertedIndex.size());
        System.out.println("**************************************");
    }


    private void logMemoryManager(MemoryManager memoryManager, String timestamp) {
        memoryManager.printMemory(timestamp);
    }


    private void logString(String string, String timestamp) {
        System.out.println("["+timestamp+"]");
        System.out.println(" -> " + string);
        System.out.println("**************************************");
    }

}
