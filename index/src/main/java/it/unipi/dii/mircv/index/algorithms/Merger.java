package it.unipi.dii.mircv.index.algorithms;

import it.unipi.dii.mircv.index.structures.*;
import it.unipi.dii.mircv.index.utility.Logs;

import java.io.*;
import java.util.ArrayList;

public class Merger {

    private String INDEX_PATH;
    private int numberOfFiles;
    private Logs log;
    private ArrayList<ArrayList<String>> terms; // matrix of terms, each row is a list of terms and columns are the files
    private static final int NUMBER_OF_POSTING = 10;
    private static final int BLOCK_POSTING_LIST_SIZE = (4 * 2) * NUMBER_OF_POSTING; // 4 byte per docID, 4 byte per freq and postings
    private static final int BLOCK_DESCRIPTIOR_SIZE = (4 * 2 + 8); // 4 byte per docID, 4 byte per freq, 8 byte per offset

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

    // return the smallest term and remove it from the list
    // file_index contains the index of the files that have the smallest term
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
        log.getLog("Start merging ...");
        //load in memory first file index_0
        ArrayList<Integer> term_index = new ArrayList<>(); // lista degli indici dei file che hanno il termine più piccolo
        ArrayList<String> lexiconFiles = new ArrayList<>(); // Lista dei file da unire

        for (int i = 0; i < numberOfFiles; i++) {
            lexiconFiles.add(INDEX_PATH + "/lexicon/lexicon_" + i + ".bin"); // utf-8,int,long,long
        }
        String lexiconFinal = INDEX_PATH + "/lexicon.bin"; // File di output // utf-8,int,long,long,int

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
                PostingList newPostingList = new PostingList();
                PostingList mergePostingList = new PostingList();
                LexiconElem newLexiconElem = new LexiconElem();

                for (int i = 0; i < term_index.size(); i++) {
                    // farsi ritornare un lexiconElem fare il merge delle posting list e scrivere il risultato nel file index
                    LexiconElem lexiconElem = Lexicon.readEntry(readers, readOffset, term_index.get(i));
                    // recupero la posting list dal file index_i dove i è dato da term_index(i)
                    newPostingList.readPostingList(term_index.get(i), lexiconElem.getDf(), lexiconElem.getOffset());
                    //merge delle posting list
                    mergePostingList.mergePosting(newPostingList);
                    //aggiorno il newLexiconElem con i dati di lexiconElem appena letto per merge
                    newLexiconElem.mergeLexiconElem(lexiconElem);
                }

                // scrittura newPostingList nel file index
                long postingOffsetStart = mergePostingList.savePostingListToDisk(-1);

                //scorri la newPostingList e ogni NUMBER_OF_POSTING elementi salva il block descriptor
                BlockDescriptor blockDescriptor;
                int blockCounter = 0;
                long blockDescriptorOffset;
                for (int i = 0; i < mergePostingList.getPostingListSize(); i++) {
                    if ((i + 1) % NUMBER_OF_POSTING == 0) {
                        //salva il block descriptor
                        blockDescriptor = new BlockDescriptor(postingOffsetStart, mergePostingList.getPostings().subList(i + 1 - NUMBER_OF_POSTING, i + 1));
                        postingOffsetStart += BLOCK_POSTING_LIST_SIZE;
                        blockDescriptorOffset = blockDescriptor.saveBlockDescriptorToDisk();
                        if (blockCounter == 0)
                            //salva inzio del block descriptor nel newLexiconElem
                            newLexiconElem.setOffset(blockDescriptorOffset);
                        blockCounter++;
                    } else if ((mergePostingList.getPostingListSize() - (blockCounter * NUMBER_OF_POSTING)) < NUMBER_OF_POSTING) {
                        //salva il block descriptor
                        blockDescriptor = new BlockDescriptor(postingOffsetStart, mergePostingList.getPostings().subList(i, mergePostingList.getPostingListSize()));
                        blockDescriptorOffset = blockDescriptor.saveBlockDescriptorToDisk();
                        if (blockCounter == 0)
                            //salva inzio del block descriptor nel newLexiconElem
                            newLexiconElem.setOffset(blockDescriptorOffset);
                        blockCounter++;
                        break;
                    }

                }

                //salvo il nuovo elemento lessico nel file lessico
                Lexicon.writeEntry(writer, term, newLexiconElem.getDf(), newLexiconElem.getCf(), newLexiconElem.getOffset(), blockCounter);
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
        log.getLog("End merging ...");
    }
}

