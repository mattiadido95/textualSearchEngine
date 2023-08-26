package it.unipi.dii.mircv.index.algorithms;

import it.unipi.dii.mircv.index.structures.*;
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
        for (int i = 0; i < numberOfFiles; i++) {
            Lexicon lexicon = new Lexicon();
            lexicon.readLexiconFromDisk(i);

            //get key from lexicon
            terms.add(new ArrayList<>(lexicon.getLexicon().keySet()));
        }
    }

    @Override
    public String toString() {
        String result = "";
        for (ArrayList<String> term : terms) {
            result += term.toString() + "\n";
        }
        result += "\n";
        return result;
    }

    private String nextTerm(ArrayList<Integer> file_index) {
        String smallestTerm = null;

        // Trova il termine più piccolo e il suo indice
        for (int i = 0; i < terms.size(); i++) {
            //get first term if terms is not empty
            if (terms.get(i).isEmpty()) {
                continue;
            }
            String currentTerm = terms.get(i).get(0);

            if (smallestTerm == null || currentTerm.compareTo(smallestTerm) < 0) {
                smallestTerm = currentTerm;
                file_index.clear();
                file_index.add(i);
                // TODO MATTEO se io cancello la lista e poi inserisco quando ho un termine piu piccolo che fine fanno gli indici cancellati?
            } else if (currentTerm.equals(smallestTerm)) {
                file_index.add(i);
            }
        }
        if (smallestTerm != null) {
            for (int i = 0; i < file_index.size(); i++) {
                int index = file_index.get(i);
                terms.get(index).remove(0);
            }
        }
        return smallestTerm;
    }

    public void execute() {
        // TODO implement merge algorithm
        log.getLog("Start merging ...");
        //load in memory first file index_0
        ArrayList<Integer> term_index = new ArrayList<>(); // lista degli indici dei file che hanno il termine più piccolo
        ArrayList<String> lexiconFiles = new ArrayList<>(); // Lista dei file da unire

        for (int i = 0; i < numberOfFiles; i++) {
            lexiconFiles.add(INDEX_PATH + "/lexicon/lexicon_" + i + ".bin");
        }
        String lexiconFinal = INDEX_PATH + "/lexicon.bin"; // File di output

        long[] readOffset = new long[numberOfFiles]; // offset dei file di lettura

        try (RandomAccessFile writer = new RandomAccessFile(lexiconFinal, "rw")) {
            ArrayList<RandomAccessFile> readers = new ArrayList<>();

            // Apri tutti i file per la lettura
            for (String inputFile : lexiconFiles) {
                readers.add(new RandomAccessFile(inputFile, "r"));
            }
            //get the term to be processed
            String term = this.nextTerm(term_index); // termine piu piccolo trovato nei file lessico

            while (term != null) {
                System.out.println(term);
                PostingList newPostingList = new PostingList();
                LexiconElem newLexiconElem = new LexiconElem(term);

                for (int i = 0; i < term_index.size(); i++) {
                    // farsi ritornare un lexiconElem fare il merge delle posting list e scrivere il risultato nel file index
                    LexiconElem lexiconElem = Lexicon.readEntry(readers, readOffset, term_index.get(i));
                    // recupero la posting list dal file index_i dove i è dato da term_index(i)
                    newPostingList.readPostingList(term_index.get(i), lexiconElem.getDf(), lexiconElem.getOffset());
                    //aggiorno il newLexiconElem con i dati di lexiconElem appena letto per merge
                    newLexiconElem.mergeLexiconElem(lexiconElem);
                    // scrittura newPostingList nel file index
                    long offset = newPostingList.savePostingListToDisk(-1);
                    //aggiorno il newLexiconElem con l'offset della posting list appena scritta
                    newLexiconElem.setOffset(offset);
                    //salvo il nuovo elemento lessico nel file lessico
                    Lexicon.writeEntry(writer, term, newLexiconElem.getDf(), newLexiconElem.getCf(), newLexiconElem.getOffset());
                    // TODO siamo sicuri che il nuovo lessico entri tutto in memoria?
                }
                term = this.nextTerm(term_index);
            }
            // chiudi tutti i file
            for (RandomAccessFile reader : readers) {
                reader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        //merge dei documents
        ArrayList<String> documentsFiles = new ArrayList<>(); // Lista dei file da unire
        for (int i = 0; i < numberOfFiles; i++) {
            documentsFiles.add(INDEX_PATH + "/documents/documents_" + i + ".bin");
        }
        String documentsFinal = INDEX_PATH + "/documents.bin"; // File di output
        // concat dei file documents
        Document.ConcatenateFiles(documentsFiles, documentsFinal);

        // cancellare i file di indice parziali
        for (int i = 0; i < numberOfFiles; i++) {
            File file = new File(INDEX_PATH + "/index_" + i + ".bin");
            file.delete();
        }
        // cancellare i file lessico parziali
        for (int i = 0; i < numberOfFiles; i++) {
            File file = new File(INDEX_PATH + "/lexicon/lexicon_" + i + ".bin");
            file.delete();
        }
        // rimuovi cartella lexicon
        File dir = new File(INDEX_PATH + "/lexicon");
        dir.delete();
        // cancellare i file documents parziali
        for (int i = 0; i < numberOfFiles; i++) {
            File file = new File(INDEX_PATH + "/documents/documents_" + i + ".bin");
            file.delete();
        }
        // rimuovi cartella lexicon
        dir = new File(INDEX_PATH + "/documents");
        dir.delete();
    }
}

