package it.unipi.dii.mircv.index.algorithms;

import it.unipi.dii.mircv.index.structures.LexiconElem;
import it.unipi.dii.mircv.index.utility.Logs;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.TreeMap;

public class Merger {

    private String INDEX_PATH;
    private int numberOfFiles;
    private Logs log;

    public Merger(String INDEX_PATH, int numberOfFiles) {
        this.INDEX_PATH = INDEX_PATH;
        this.log = new Logs();
        this.numberOfFiles = numberOfFiles;
    }

    public void execute() {
        // TODO implement merge algorithm
        log.getLog("Start merging ...");
        //load in memory first file index_0

//        // get list of files in data/index/ and put them in an array
//        ArrayList<String> fileList = new ArrayList<>();
//        File directory = new File("data/index/");
//        if (directory.exists() && directory.isDirectory()) {
//            File[] files = directory.listFiles();
//            if (files != null) {
//                for (File file : files) {
//                    if (file.isFile()) {
//                        fileList.add(file.getAbsolutePath());
//                    }
//                }
//            }
//        }
//
//        // open each file and read it importing posting lists in memory and search in other files for the same token
//        for (String file : fileList) {
//           try{
//               FileInputStream fileInputStream = new FileInputStream(file);
//               FileChannel fileChannel = fileInputStream.getChannel();
//               ByteBuffer buffer = ByteBuffer.allocate(1024); // Adjust buffer size as needed
//
//               while (fileChannel.read(buffer) != -1) {
//                   buffer.flip();
//                   int docID = buffer.getInt();
//                   int frequency = buffer.getInt();
//                   Posting posting = new Posting(docID, frequency);
//                   postingsList.add(posting);
//
//                   buffer.clear();
//               }
//
//               fileChannel.close();
//               fileInputStream.close();
//
//
//           } catch (IOException e) {
//               throw new RuntimeException(e);
//           }
//        }




//        for (int index_counter = 1; index_counter < numberOfFiles; index_counter++) {
//            //load in memory index_i
//            //merge index_0 with index_i
//        }

//        ArrayList<String> inputFiles = new ArrayList<>(); // Lista dei file da unire
//        for (int i = 0; i < numberOfFiles; i++) {
//            inputFiles.add(INDEX_PATH + "/index_" + i + ".bin");
//        }
//        String outputFile = INDEX_PATH + "/index.bin"; // File di output
//
//        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
//            ArrayList<BufferedReader> readers = new ArrayList<>();
//
//            // Apri tutti i file per la lettura
//            for (String inputFile : inputFiles) {
//                readers.add(new BufferedReader(new FileReader(inputFile)));
//            }
//
//            TreeMap<Integer> termQueue = new PriorityQueue<>(); // Coda prioritaria per tenere traccia dei termid
//
//            // Inizializza la coda con i primi valori dei termid da ciascun file
//            for (int i = 0; i < numberOfFiles; i++) {
//                LexiconElem line = readers.get(i).readEntry();
//                if (line != null) {
//                    termQueue.add(Integer.parseInt(line));
//                }
//            }
//
//            // Esegui il merge finché ci sono termid da processare
//            while (!termQueue.isEmpty()) {
//                int lowestTermId = termQueue.poll();
//                writer.write(lowestTermId + " ");
//
//                // Trova le postings lists per il termid e scrivile nel file di output
//                for (BufferedReader reader : readers) {
//                    String line = reader.readLine();
//                    if (line != null) {
//                        int termId = Integer.parseInt(line);
//                        if (termId == lowestTermId) {
//                            String postingsList = reader.readLine();
//                            writer.write(postingsList + " ");
//                            termQueue.add(termId); // Aggiungi nuovamente il termid alla coda
//                        }
//                    }
//                }
//            }
//
//            // Chiudi i lettori
//            for (BufferedReader reader : readers) {
//                reader.close();
//            }
//
//            System.out.println("Merge completed.");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

    }
}

