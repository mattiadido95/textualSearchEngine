package it.unipi.dii.mircv.index.utility;

import it.unipi.dii.mircv.index.structures.Lexicon;
import it.unipi.dii.mircv.index.structures.PostingList;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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
        System.out.println("[" + timestamp + "]");
        System.out.println(postingList.toString());
        System.out.println("**************************************");
    }

    private void logHashMap(HashMap invertedIndex, String timestamp) {
        System.out.println("[" + timestamp + "] InvertedIndex status: ");
        System.out.println(" -> Size: " + invertedIndex.size());
        System.out.println("**************************************");
    }

    private void logMemoryManager(MemoryManager memoryManager, String timestamp) {
        memoryManager.printMemory(timestamp);
    }

    private void logString(String string, String timestamp) {
        System.out.println("[" + timestamp + "]");
        System.out.println(" -> " + string);
        System.out.println("**************************************");
    }

    public void addLog(String logType, long startTime, long endTime) {
        // Crea l'oggetto JSON per il log
        JSONObject logObject = new JSONObject();
        logObject.put("type", logType);
        logObject.put("start_time", startTime);
        logObject.put("end_time", endTime);
        logObject.put("duration", endTime-startTime);

        // Controlla se il file JSON esiste
        String fileName = "data/logs/logs.json";
        File file = new File(fileName);

        if (file.exists()) {
            // Carica il contenuto esistente
            try {
                String fileContent = new String(Files.readAllBytes(Paths.get(fileName)));
                JSONArray existingLogs = new JSONArray(fileContent);
                existingLogs.put(logObject);

                // Scrivi il nuovo array nel file JSON
                try (FileWriter writer = new FileWriter(fileName)) {
                    writer.write(existingLogs.toString(4));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // Se il file non esiste, crea un nuovo array di logs
            JSONArray logs = new JSONArray();
            logs.put(logObject);

            // Scrivi il nuovo array nel file JSON
            try (FileWriter writer = new FileWriter(fileName)) {
                writer.write(logs.toString(4));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
