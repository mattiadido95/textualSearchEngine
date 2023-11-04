package it.unipi.dii.mircv.index.structures;

import it.unipi.dii.mircv.index.utility.Logs;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class represents the lexicon of the index.
 * It is implemented as a TreeMap<String, LexiconElem> where the key is the term and the value is a LexiconElem object.
 * */
public class Lexicon {
    TreeMap<String, LexiconElem> lexicon;

    /**
     * Default constructor to initialize the lexicon.
     */
    public Lexicon() {
        this.lexicon = new TreeMap<>();
    }

    /**
     * Get the lexicon as a TreeMap.
     *
     * @return The lexicon TreeMap.
     */
    public TreeMap<String, LexiconElem> getLexicon() {
        return this.lexicon;
    }

    /**
     * Get the keys (terms) in the lexicon.
     *
     * @return An ArrayList of lexicon keys.
     */
    public ArrayList<String> getLexiconKeys() {
        return new ArrayList<>(this.lexicon.keySet());
    }

    /**
     * Add a term to the lexicon. If the term is already present, increment its cf; otherwise, create a new entry.
     *
     * @param term The term to add.
     */
    public void addLexiconElem(String term) {
        // lexicon contains the term
        if (this.lexicon.containsKey(term)) {
            LexiconElem lexiconElem = this.lexicon.get(term);
            lexiconElem.incrementCf();
        } else {
            // lexicon does not contain the term
            LexiconElem lexiconElem = new LexiconElem();
            lexiconElem.incrementCf();
            this.lexicon.put(term, lexiconElem);
        }
    }

    /**
     * Print the status of the lexicon.
     *
     * @param timestamp A timestamp to include in the printout.
     */
    public void printLexicon(String timestamp) {
        System.out.println("[" + timestamp + "] Lexicon status: ");
        System.out.println(" -> Size: " + this.lexicon.size());
        System.out.println("**************************************");
    }

    /**
     * Override toString to represent the Lexicon object as a string.
     *
     * @return A string representation of the Lexicon object.
     */
    @Override
    public String toString() {
        return "Lexicon{" +
                "lexicon=" + lexicon +
                '}';
    }

    /**
     * Get a LexiconElem object associated with a term.
     *
     * @param term The term to retrieve information for.
     * @return The LexiconElem object associated with the term.
     */
    public LexiconElem getLexiconElem(String term) {
        return this.lexicon.get(term);
    }

    /**
     * Sort the lexicon by LexiconElem.TUB_bm25 or LexiconElem.TUB_tfidf in descending order.
     *
     * @param lexicon         The lexicon to be sorted.
     * @param scoringFunction The scoring function ("TFIDF" or "BM25") to use for sorting.
     * @return The sorted lexicon as a LinkedHashMap.
     */
    public static LinkedHashMap<String, LexiconElem> sortLexicon(LinkedHashMap<String, LexiconElem> lexicon, String scoringFunction) {
        // Ordina la lexicon per LexiconELem.TUB_bm25 in ordine decrescente
        LinkedHashMap<String, LexiconElem> sorted = new LinkedHashMap<>();
        if (scoringFunction.equals("TFIDF")) {
            sorted = lexicon.entrySet().stream()
                    .sorted((e1, e2) -> e1.getValue().compareTFIDF(e2.getValue()))
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (x, y) -> {
                                throw new AssertionError();
                            },
                            LinkedHashMap::new
                    ));
        } else if (scoringFunction.equals("BM25")) {
            sorted = lexicon.entrySet().stream()
                    .sorted((e1, e2) -> e1.getValue().compareBM25(e2.getValue()))
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (x, y) -> {
                                throw new AssertionError();
                            },
                            LinkedHashMap::new
                    ));
        }
        return sorted;
    }

    /**
     * Set the document frequency (df) for a term in the lexicon.
     *
     * @param term  The term to set the df for.
     * @param newDf The new document frequency to set.
     */
    public void setDf(String term, int newDf) {
        this.lexicon.get(term).setDf(newDf);
    }

    /**
     * Save the lexicon to disk.
     *
     * @param indexCounter The index counter used to distinguish SPIMI phases.
     * @param filePath     The path where the lexicon will be saved.
     */
    public void saveLexiconToDisk(int indexCounter, String filePath) {
        if (indexCounter != -1)
            filePath += indexCounter + ".bin";

        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(filePath, "rw");
            for (String term : lexicon.keySet()) {
                LexiconElem lexiconElem = lexicon.get(term);
                randomAccessFile.writeUTF(term);
                randomAccessFile.writeInt(lexiconElem.getDf());
                randomAccessFile.writeLong(lexiconElem.getCf());
                randomAccessFile.writeLong(lexiconElem.getOffset());
                if (indexCounter == -1) {
                    randomAccessFile.writeInt(lexiconElem.getBlocksNumber());
                    randomAccessFile.writeDouble(lexiconElem.getTUB_bm25());
                    randomAccessFile.writeDouble(lexiconElem.getTUB_tfidf());
                }
            }
            randomAccessFile.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    public void saveLexiconToDiskTEST(int indexCounter, String filePath) {
//        if (indexCounter != -1)
//            filePath += indexCounter + ".bin";
//
//        try (RandomAccessFile randomAccessFile = new RandomAccessFile(filePath, "rw");
//             BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(randomAccessFile.getFD()))) {
//
//            DataOutputStream dataOutputStream = new DataOutputStream(bufferedOutputStream);
//
//            for (String term : lexicon.keySet()) {
//                LexiconElem lexiconElem = lexicon.get(term);
//                dataOutputStream.writeUTF(term);
//                dataOutputStream.writeInt(lexiconElem.getDf());
//                dataOutputStream.writeLong(lexiconElem.getCf());
//                dataOutputStream.writeLong(lexiconElem.getOffset());
//                if (indexCounter == -1) {
//                    dataOutputStream.writeInt(lexiconElem.getBlocksNumber());
//                    dataOutputStream.writeDouble(lexiconElem.getTUB_bm25());
//                    dataOutputStream.writeDouble(lexiconElem.getTUB_tfidf());
//                }
//            }
//
//            dataOutputStream.flush(); // Assicurati che tutti i dati siano scritti
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }


    /**
     * Read the lexicon from disk.
     *
     * @param indexCounter The index counter used to distinguish merge phases.
     * @param filePath     The path where the lexicon will be read from.
     */
    public void readLexiconFromDisk(int indexCounter, String filePath) {
        if (indexCounter != -1)
            filePath = "data/index/lexicon/lexicon_" + indexCounter + ".bin";

        try (RandomAccessFile randomAccessFile = new RandomAccessFile(filePath, "rw");
             BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(randomAccessFile.getFD()))) {
            DataInputStream dataInputStream = new DataInputStream(bufferedInputStream);
            while (dataInputStream.available() > 0) {
                String term = dataInputStream.readUTF();
                int df = dataInputStream.readInt();
                long cf = dataInputStream.readLong();
                long offset = dataInputStream.readLong();
                int numblock = -1;
                double tub_bm25 = -1;
                double tub_tfidf = -1;
                if (indexCounter == -1) {
                    numblock = dataInputStream.readInt();
                    tub_bm25 = dataInputStream.readDouble();
                    tub_tfidf = dataInputStream.readDouble();
                }
                LexiconElem lexiconElem = new LexiconElem(df, cf, offset, numblock, tub_bm25, tub_tfidf);
                this.lexicon.put(term, lexiconElem);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Read a single lexicon entry from disk.
     *
     * @param araf        An ArrayList of RandomAccessFiles for lexicon entries.
     * @param arrayOffset An array of offsets.
     * @param i           The index to read from.
     * @return The LexiconElem object read.
     * @throws IOException If there is an error reading the entry.
     */
    public static LexiconElem readEntry(ArrayList<RandomAccessFile> araf, long[] arrayOffset, int i) throws IOException {
        RandomAccessFile raf = araf.get(i);
        raf.seek(arrayOffset[i]);
        raf.readUTF();
        int df = raf.readInt();
        long cf = raf.readLong();
        long offset = raf.readLong();
        arrayOffset[i] = raf.getFilePointer();
        LexiconElem lexiconElem = new LexiconElem(df, cf, offset, -1, -1, -1);
        return lexiconElem;
    }

    /**
     * Write a single lexicon entry to disk.
     *
     * @param raf      The RandomAccessFile to write to.
     * @param term     The term to write.
     * @param df       The document frequency to write.
     * @param cf       The collection frequency to write.
     * @param offset   The offset to write.
     * @param numBlock The number of blocks to write.
     * @return The written term.
     * @throws IOException If there is an error writing the entry.
     */
    public static String writeEntry(RandomAccessFile raf, String term, int df, long cf, long offset, int numBlock) throws IOException {
        raf.writeUTF(term);
        raf.writeInt(df);
        raf.writeLong(cf);
        raf.writeLong(offset);
        raf.writeInt(numBlock);
        raf.writeDouble(-1); // TUB_bm25
        raf.writeDouble(-1); // TUB_tfidf
        return term;
    }

}