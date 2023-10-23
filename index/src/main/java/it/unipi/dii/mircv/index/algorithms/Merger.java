package it.unipi.dii.mircv.index.algorithms;

import it.unipi.dii.mircv.index.structures.*;
import it.unipi.dii.mircv.index.utility.Logs;

import java.io.*;
import java.util.ArrayList;

/**
 * The Merger class is responsible for merging the partial index files generated by the SPIMI algorithm.
 * It combines the lexicon, posting lists, and documents into a final index.
 */
public class Merger {
    private String INDEX_PATH;
    private int numberOfFiles;
    private Logs log;
    private ArrayList<ArrayList<String>> terms; // matrix of terms, each row is a list of terms and columns are the files
    private static final int POSTING_SIZE = (4 * 2); // 4 byte per docID, 4 byte per freq into one posting
    private static final String BLOCK_DESCRIPTOR_PATH = "data/index/blockDescriptor.bin";
    private static final String FINAL_INDEX_PATH = "data/index/index.bin";
    private static final String PARTIAL_LEXICON_PATH = "data/index/lexicon/lexicon_";

    /**
     * Constructs a new Merger for merging the partial index files.
     *
     * @param INDEX_PATH    The path to the index files.
     * @param numberOfFiles The number of partial index files to merge.
     */
    public Merger(String INDEX_PATH, int numberOfFiles) {
        this.INDEX_PATH = INDEX_PATH;
        this.log = new Logs();
        this.numberOfFiles = numberOfFiles;
        terms = new ArrayList<>();
        //read all the terms from index files
        for (int i = 0; i < numberOfFiles; i++) {
            Lexicon lexicon = new Lexicon();
            lexicon.readLexiconFromDisk(i, PARTIAL_LEXICON_PATH);
            //get key from lexicon
            terms.add(new ArrayList<>(lexicon.getLexicon().keySet()));
        }
    }

    /**
     * Returns a string representation of the terms currently in memory for debugging purposes.
     *
     * @return A string representation of the terms.
     */
    @Override
    public String toString() {
        String result = "";
        for (ArrayList<String> term : terms) {
            result += term.toString() + "\n";
        }
        result += "\n";
        return result;
    }

    /**
     * Retrieves the next smallest term from the list of terms and removes it from the list.
     *
     * @param file_index A list of indices indicating which files contain the smallest term.
     * @return The smallest term found.
     */
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

    /**
     * Executes the merging process, combining lexicon entries, posting lists, and documents into a final index.
     */
    public void execute() {
        log.getLog("Start merging ...");

        ArrayList<Integer> term_index = new ArrayList<>(); // list of index file containing the smallest term
        ArrayList<String> lexiconFiles = new ArrayList<>(); // list of lexicon files to be merged

        for (int i = 0; i < numberOfFiles; i++) {
            lexiconFiles.add(INDEX_PATH + "/lexicon/lexicon_" + i + ".bin");
        }
        String lexiconFinal = INDEX_PATH + "/lexicon.bin"; // output file

        long[] readOffset = new long[numberOfFiles]; // offset for reading lexicon files

        try (RandomAccessFile writer = new RandomAccessFile(lexiconFinal, "rw")) {
            ArrayList<RandomAccessFile> readers = new ArrayList<>();

            // open all the lexicon files
            for (String inputFile : lexiconFiles) {
                readers.add(new RandomAccessFile(inputFile, "r"));
            }
            //get the smallest term to be processed
            String term = this.nextTerm(term_index);

            while (term != null) {
                PostingList newPostingList = new PostingList();
                PostingList mergePostingList = new PostingList();
                LexiconElem newLexiconElem = new LexiconElem();

                for (int i = 0; i < term_index.size(); i++) { // for each file containing the smallest term
                    LexiconElem lexiconElem = Lexicon.readEntry(readers, readOffset, term_index.get(i)); // read lexicon entry
                    newPostingList.readPostingList(term_index.get(i), lexiconElem.getDf(), lexiconElem.getOffset(), INDEX_PATH + "/index_"); // read posting list
                    mergePostingList.mergePosting(newPostingList); // merge posting list
                    newLexiconElem.mergeLexiconElem(lexiconElem); // merge lexicon entry with the new portion of the lexicon entry
                }

                int blockCounter = saveBlockPosting(mergePostingList, newLexiconElem);
                // write new lexicon entry
                Lexicon.writeEntry(writer, term, newLexiconElem.getDf(), newLexiconElem.getCf(), newLexiconElem.getOffset(), blockCounter);
                term = this.nextTerm(term_index);
            }
            // close all the lexicon files
            for (RandomAccessFile reader : readers) {
                reader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // merge of documents
        ArrayList<String> documentsFiles = new ArrayList<>(); // list of documents files to be merged
        for (int i = 0; i < numberOfFiles; i++) {
            documentsFiles.add(INDEX_PATH + "/documents/documents_" + i + ".bin");
        }
        String documentsFinal = INDEX_PATH + "/documents.bin"; // File di output
        // concatenation of documents files
        Document.ConcatenateFiles(documentsFiles, documentsFinal);

        // delete partial index files
        for (int i = 0; i < numberOfFiles; i++) {
            File file = new File(INDEX_PATH + "/index_" + i + ".bin");
            file.delete();
        }
        // delete partial lexicon files
        for (int i = 0; i < numberOfFiles; i++) {
            File file = new File(INDEX_PATH + "/lexicon/lexicon_" + i + ".bin");
            file.delete();
        }
        // remove lexicon folder
        File dir = new File(INDEX_PATH + "/lexicon");
        dir.delete();
        // delete partial documents files
        for (int i = 0; i < numberOfFiles; i++) {
            File file = new File(INDEX_PATH + "/documents/documents_" + i + ".bin");
            file.delete();
        }
        // remove documents folder
        dir = new File(INDEX_PATH + "/documents");
        dir.delete();
        log.getLog("End merging ...");
    }

    /**
     * Saves a block of postings and corresponding block descriptor to the final index.
     *
     * @param mergePostingList The merged posting list to be saved.
     * @param newLexiconElem   The new lexicon element corresponding to the merged posting list.
     * @return The number of blocks created.
     */
    public static int saveBlockPosting(PostingList mergePostingList, LexiconElem newLexiconElem) {
        // write posting list to disk
        long postingOffsetStart = mergePostingList.savePostingListToDisk(-1, FINAL_INDEX_PATH);

        // organize posting list into blocks and save block descriptor to disk
        BlockDescriptor blockDescriptor;
        int blockCounter = 0;
        long blockDescriptorOffset;
        int numBlocks = mergePostingList.getPostingListSize() > 1024 ? (int) Math.sqrt(mergePostingList.getPostingListSize()) : 1; // get number of block in which the posting list will be divided;
        int numPostingInBlock = (int) Math.ceil(mergePostingList.getPostingListSize() / (double) numBlocks); // get number of posting in each block

        for (int i = 0; i < mergePostingList.getPostingListSize(); i++) {
            if ((i + 1) % numPostingInBlock == 0) {
                // save block descriptor
                blockDescriptor = new BlockDescriptor(postingOffsetStart, mergePostingList.getPostings().subList(i + 1 - numPostingInBlock, i + 1));
                postingOffsetStart += numPostingInBlock * POSTING_SIZE;
                blockDescriptorOffset = blockDescriptor.saveBlockDescriptorToDisk(BLOCK_DESCRIPTOR_PATH);
                if (blockCounter == 0)
                    // save start of block descriptor into newLexiconElem
                    newLexiconElem.setOffset(blockDescriptorOffset);
                blockCounter++;
            } else if ((mergePostingList.getPostingListSize() - (blockCounter * numPostingInBlock)) < numPostingInBlock) { // save remaining postings
                // save block descriptor
                blockDescriptor = new BlockDescriptor(postingOffsetStart, mergePostingList.getPostings().subList(i, mergePostingList.getPostingListSize()));
                blockDescriptorOffset = blockDescriptor.saveBlockDescriptorToDisk(BLOCK_DESCRIPTOR_PATH);
                if (blockCounter == 0)
                    // save start of block descriptor into newLexiconElem
                    newLexiconElem.setOffset(blockDescriptorOffset);
                blockCounter++;
                break;
            }
        }
        if (blockCounter != numBlocks)
            System.out.println("Error in saveBlockPosting: blockCounter != numBlocks");
        return numBlocks;
    }

}


