package it.unipi.dii.mircv.index.structures;

import it.unipi.dii.mircv.index.utility.Logs;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;


public class Lexicon {

    TreeMap<String, LexiconElem> lexicon;

    Logs log = new Logs();

    public Lexicon() {
        this.lexicon = new TreeMap<>();
    }

    public TreeMap<String, LexiconElem> getLexicon() {
        return this.lexicon;
    }

    public ArrayList<String> getLexiconKeys() {
        return new ArrayList<>(this.lexicon.keySet());
    }

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

    public void printLexicon(String timestamp){
        System.out.println("["+timestamp+"] Lexicon status: ");
        System.out.println(" -> Size: " + this.lexicon.size());
        System.out.println("**************************************");
    }

    @Override
    public String toString() {
        return "Lexicon{" +
                "lexicon=" + lexicon +
                '}';
    }

    public LexiconElem getLexiconElem(String term) {
        return this.lexicon.get(term);
    }

    //sort lexicon by TUB in termElem
    public static LinkedHashMap<String, LexiconElem> sortLexicon(LinkedHashMap<String,LexiconElem> lexicon, String scoringFunction){

        // Ordina la lexicon per LexiconELem.TUB_bm25 in ordine decrescente
        LinkedHashMap<String, LexiconElem> sorted = new LinkedHashMap<>();
        if(scoringFunction.equals("TF-IDF")){
            sorted = lexicon.entrySet().stream()
                .sorted((e1, e2) -> e1.getValue().compareTFIDF(e2.getValue()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (x, y) -> {throw new AssertionError();},
                        LinkedHashMap::new
                ));
        }else if(scoringFunction.equals("BM25")) {
            sorted = lexicon.entrySet().stream()
                .sorted((e1, e2) -> e1.getValue().compareBM25(e2.getValue()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (x, y) -> {throw new AssertionError();},
                        LinkedHashMap::new
                ));
        }
        return sorted;
    }


    public void setDf(String term, int newDf) {
        this.lexicon.get(term).setDf(newDf);
    }

    public void saveLexiconToDisk(int indexCounter, String filePath) {
//        String filePath = "data/index/lexicon/lexicon_" + indexCounter + ".bin";

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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void readLexiconFromDisk(int indexCounter, String filePath){

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
                // Creare un nuovo oggetto LexiconElem e inserirlo nell'HashMap
                LexiconElem lexiconElem = new LexiconElem(df, cf, offset, numblock, tub_bm25, tub_tfidf);
                this.lexicon.put(term, lexiconElem);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static LexiconElem readEntry(ArrayList<RandomAccessFile> araf, long[] arrayOffset, int i) throws IOException {
        //get right file and offset
        RandomAccessFile raf = araf.get(i);
        raf.seek(arrayOffset[i]);

        raf.readUTF();
        int df = raf.readInt();
        long cf = raf.readLong();
        long offset = raf.readLong();

        arrayOffset[i] = raf.getFilePointer();
        // Creare un nuovo oggetto LexiconElem e inserirlo nell'HashMap
        LexiconElem lexiconElem = new LexiconElem(df, cf, offset,-1,-1,-1);
        return lexiconElem;
    }

    public static String writeEntry(RandomAccessFile raf,String term, int df,long cf, long offset, int numBlock) throws IOException {
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