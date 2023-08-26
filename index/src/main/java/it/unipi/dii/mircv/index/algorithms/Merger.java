package it.unipi.dii.mircv.index.algorithms;

import it.unipi.dii.mircv.index.structures.Lexicon;
import it.unipi.dii.mircv.index.structures.LexiconElem;
import it.unipi.dii.mircv.index.structures.Posting;
import it.unipi.dii.mircv.index.structures.PostingList;
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
    private ArrayList<ArrayList<String>> terms; // matrix of terms, each row is a list of terms and columns are the files

    public Merger(String INDEX_PATH, int numberOfFiles) {
        this.INDEX_PATH = INDEX_PATH;
        this.log = new Logs();
        this.numberOfFiles = numberOfFiles;
        terms = new ArrayList<>();
        //read all the terms from index files
        for(int i = 0; i < numberOfFiles; i++){
            Lexicon lexicon = new Lexicon();
            lexicon.readLexiconFromDisk(i);

            //get key from lexicon
            terms.add(new ArrayList<>(lexicon.getLexicon().keySet()));
        }
    }

    @Override
    public String toString() {
        String result = "";
        for(ArrayList<String> term : terms){
            result += term.toString() + "\n";
        }
        result += "\n";
        return result;
    }

    private String nextTerm(ArrayList<Integer> file_index){
        String smallestTerm = null;

        // Trova il termine più piccolo e il suo indice
        for (int i = 0; i < terms.size(); i++) {
            //get first term
            String currentTerm = terms.get(i).get(0);

            if (smallestTerm == null || currentTerm.compareTo(smallestTerm) < 0) {
                smallestTerm = currentTerm;
                file_index.clear();
                file_index.add(i);
                // TODO MATTEO se io cancello la lista e poi inserisco quando ho un termine piu piccolo che fine fanno gli indici cancellati?
            }else if (currentTerm.equals(smallestTerm)) {
                file_index.add(i);
            }
        }

        for (int i = 0; i < file_index.size(); i++) {
            int index = file_index.get(i);
            terms.get(index).remove(0);
        }
        return smallestTerm;
    }

    public void execute() {
        // TODO implement merge algorithm
        log.getLog("Start merging ...");
        //load in memory first file index_0
        ArrayList<Integer> term_index = new ArrayList<>(); // lista degli indici dei file che hanno il termine più piccolo
//
//        System.out.print(this.nextTerm(index));
//        System.out.println(index);
//
//
// get list of files in data/index/ and put them in an array
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

        ArrayList<String> lexiconFiles = new ArrayList<>(); // Lista dei file da unire
        for (int i = 0; i < numberOfFiles; i++) {
            lexiconFiles.add(INDEX_PATH + "/lexicon/lexicon_" + i + ".bin");
        }
        String lexiconFinal = INDEX_PATH + "/lexicon.bin"; // File di output

        try (DataOutputStream writer = new DataOutputStream(new FileOutputStream(lexiconFinal))) {
            ArrayList<DataInputStream> readers = new ArrayList<>();

            // Apri tutti i file per la lettura
            for (String inputFile : lexiconFiles) {
                readers.add(new DataInputStream(new FileInputStream(inputFile)));
            }
            //get the term to be processed
            String term = this.nextTerm(term_index); // termine piu piccolo trovato nei file lessico

            while(term != null) {
                Lexicon lexicon = new Lexicon();
                PostingList newPostingList = new PostingList();
                LexiconElem newLexiconElem = new LexiconElem(term);

                for (int i = 0; i < term_index.size(); i++) {
                    // farsi ritornare un lexiconElem fare il merge delle posting list e scrivere il risultato nel file index
                    LexiconElem lexiconElem = lexicon.readEntry(readers.get((i)),i);
                    // recupero la posting list dal file index_i dove i è dato da term_index(i)
                    newPostingList.readPostingList(term_index.get(i), lexiconElem.getDf(), lexiconElem.getOffset());
                    //aggiorno il newLexiconElem con i dati di lexiconElem appena letto per merge
                    newLexiconElem.mergeLexiconElem(lexiconElem);
                    // scrittura newPostingList nel file index
                    long offset = newPostingList.savePostingListToDisk(-1); // TODO c'è la scrittura solo per postinglist parziali non per il file totale
                    //aggiorno il newLexiconElem con l'offset della posting list appena scritta
                    newLexiconElem.setOffset(offset);
                    //aggiungo il newLexiconElem al lessico
                    lexicon.addLexiconElem(newLexiconElem);
                    // TODO siamo sicuri che il nuovo lessico entri tutto in memoria?


                }
                //per ogni elemento nel lexicon fare il merge dei termini dentro lexicon (df,cf)
                //fare concat ti tutte le posting list accedendo agli offset di lexicon il file associato si trova usando la lista term_index
                //scrivere il risultato nel file index
                //ottenere il nuovo offset
                //memorizzarlo nell'elemento di lessico e scrivere il nuovo elemento usando la writeEntry
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

