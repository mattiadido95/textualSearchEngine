package it.unipi.dii.mircv.index.utility;

import it.unipi.dii.mircv.index.structures.Lexicon;
import it.unipi.dii.mircv.index.structures.PostingList;

import java.util.HashMap;

public class Logs {

    public Logs() {
    }

    public <T> void getLog(T element) {
        String typeObj = element.getClass().getSimpleName();

        switch (typeObj) {
            case "PostingList":
                logPostingList((PostingList) element);
                break;
            case "Document":
                // TODO implement log function for Document
                break;
            case "Posting":
                // TODO implement log function for Posting
                break;
            case "HashMap":
                logHashMap((HashMap) element);
                break;
            case "String":
                logString((String) element);
                break;
            case "MemoryManager":
                logMemoryManager((MemoryManager) element);
                break;
            case "Lexicon":
                logLexicon((Lexicon) element);
                break;
            default:
                // TODO implement log function for default and show log error
                break;
        }
    }

    private void logLexicon(Lexicon lexicon) {
        lexicon.printLexicon();
    }

    private void logPostingList(PostingList postingList) {
        postingList.toString();
    }

    private void logHashMap(HashMap invertedIndex) {
        System.out.println("InvertedIndex status: ");
        System.out.println(" -> Size: " + invertedIndex.size());
        System.out.println("**************************************");
    }


    private void logMemoryManager(MemoryManager memoryManager) {
        memoryManager.printMemory();
    }


    private void logString(String string) {
        System.out.println(" -> " + string);
        System.out.println("**************************************");
    }

}
