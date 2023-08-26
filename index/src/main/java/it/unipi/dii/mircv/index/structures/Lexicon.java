package it.unipi.dii.mircv.index.structures;

import it.unipi.dii.mircv.index.utility.Logs;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;


public class Lexicon {
    //TODO cambiare in treeMap
    TreeMap<String, LexiconElem> lexicon;

    Logs log = new Logs();

    public Lexicon() {
        this.lexicon = new TreeMap<>();
    }

    public TreeMap<String, LexiconElem> getLexicon() {
        return this.lexicon;
    }

    public void addLexiconElem(LexiconElem lexiconElem) {
        this.lexicon.put(lexiconElem.getTerm(), lexiconElem);
    }

    public void addLexiconElem(String term) {
        // lexicon contains the term
        if (this.lexicon.containsKey(term)) {
            LexiconElem lexiconElem = this.lexicon.get(term);
            lexiconElem.incrementCf();
        } else {
            // lexicon does not contain the term
            LexiconElem lexiconElem = new LexiconElem(term);
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

//    public void sortLexicon() {
//        /**
//         * this.lexicon: Questo fa riferimento a una mappa chiamata lexicon nell'oggetto corrente (presumibilmente una variabile di istanza nella classe).
//         * .entrySet().stream(): Converti la mappa in uno stream di oggetti Map.Entry, che rappresentano le coppie chiave-valore della mappa.
//         * .sorted((e1, e2) -> e1.getValue().getTerm().compareTo(e2.getValue().getTerm())): Ordini gli elementi dello stream in base al valore dell'oggetto Lexicon associato alla chiave. La funzione di comparazione prende due oggetti Map.Entry (e quindi coppie chiave-valore) e confronta i termini (getTerm()) degli oggetti Lexicon associati ai rispettivi valori.
//         * .collect(...): Raccogli gli elementi ordinati in una nuova mappa.
//         * HashMap::new: Fornisce un costruttore di HashMap per creare una nuova mappa.
//         * (m, e) -> m.put(e.getKey(), e.getValue()): Definisce come mettere gli elementi nella mappa di destinazione durante la raccolta. Per ogni elemento nell'stream, mette la coppia chiave-valore nell'oggetto HashMap di destinazione (m).
//         * HashMap::putAll: Combinare le mappe. Questo passo consente di inserire tutte le coppie chiave-valore dalla mappa raccolta nell'oggetto this.lexicon originale.
//         */
//        this.lexicon = this.lexicon.entrySet().stream()
//                .sorted((e1, e2) -> e1.getValue().getTerm().compareTo(e2.getValue().getTerm()))
//                .collect(HashMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), HashMap::putAll);
//    }

    public void setDf(String term, int newDf) {
        this.lexicon.get(term).setDf(newDf);
    }

    public static byte[] encodeString(String input, int targetByteLength) {
        byte[] inputBytes = input.getBytes(StandardCharsets.UTF_8);
        byte[] encodedBytes = new byte[targetByteLength];

        System.arraycopy(inputBytes, 0, encodedBytes, 0, Math.min(inputBytes.length, targetByteLength));

        return encodedBytes;
    }

    public static String decodeBytes(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8).trim();
    }

    public void saveLexiconToDisk(int indexCounter) {
        String filePath = "data/index/lexicon/lexicon_" + indexCounter + ".bin";
        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(filePath, "rw");

            for (String term : lexicon.keySet()) {
                LexiconElem lexiconElem = lexicon.get(term);
                randomAccessFile.writeUTF(term);
                randomAccessFile.writeInt(lexiconElem.getDf());
                randomAccessFile.writeLong(lexiconElem.getCf());
                randomAccessFile.writeLong(lexiconElem.getOffset());
            }

//            FileOutputStream fileOutputStream = new FileOutputStream(filePath);
//            FileChannel fileChannel = fileOutputStream.getChannel();
//            ByteBuffer buffer = ByteBuffer.allocate(1008); // Dimensione del buffer
//
//            int targetByteLength = 64; // Lunghezza in byte del termine
//
//            for (String term : lexicon.keySet()) {
//                LexiconElem lexiconElem = lexicon.get(term);
//
//                buffer.clear();
//
//                // Scrivi i dati nel buffer
////                byte[] termBytes = lexiconElem.getTerm().getBytes();
//
//                byte[] termBytes = encodeString(term, targetByteLength);
//                buffer.putInt(termBytes.length);
//                buffer.put(termBytes);
//
//                buffer.putInt(lexiconElem.getDf());
//                buffer.putLong(lexiconElem.getCf());
//                buffer.putLong(lexiconElem.getOffset());
//
//                buffer.flip();
//
//                // Scrivi il contenuto del buffer nel canale del file
//                fileChannel.write(buffer);
//            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void readLexiconFromDisk(int indexCounter) {
        String filePath = "data/index/lexicon/lexicon_" + indexCounter + ".bin";

        try {

//            FileChannel fileChannel = FileChannel.open(Path.of(filePath));
//            ByteBuffer buffer = ByteBuffer.allocate(1008); // Dimensione del buffer
//
//            while (fileChannel.position() < fileChannel.size()) {
//                buffer.clear();
//
//                int bytesRead = fileChannel.read(buffer);
//
//                if (bytesRead == -1) {
//                    // Non ci sono abbastanza dati nel file
//                    break;
//                }
//
//                buffer.flip();
//
//                int termLength = buffer.getInt();
//                byte[] termBytes = new byte[termLength];
//                buffer.get(termBytes);
////                String term = new String(termBytes);
//                String term = decodeBytes(termBytes);
//                int df = buffer.getInt();
//                long cf = buffer.getLong();
//                long offset = buffer.getLong();
//
//                // Creare un nuovo oggetto LexiconElem e inserirlo nell'HashMap
//                LexiconElem lexiconElem = new LexiconElem(term, df, cf, offset);
//                this.lexicon.put(term, lexiconElem);
//            }

            RandomAccessFile randomAccessFile = new RandomAccessFile(filePath, "rw");

            while (randomAccessFile.getFilePointer() < randomAccessFile.length()) {
                String term = randomAccessFile.readUTF();
                int df = randomAccessFile.readInt();
                long cf = randomAccessFile.readLong();
                long offset = randomAccessFile.readLong();

                // Creare un nuovo oggetto LexiconElem e inserirlo nell'HashMap
                LexiconElem lexiconElem = new LexiconElem(term, df, cf, offset);
                this.lexicon.put(term, lexiconElem);
            }
            randomAccessFile.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static LexiconElem readEntry(ArrayList<RandomAccessFile> araf, long[] arrayOffset, int i) throws IOException {
        //get right file and offset
        RandomAccessFile raf = araf.get(i);
        raf.seek(arrayOffset[i]);

        String term = raf.readUTF();
        int df = raf.readInt();
        long cf = raf.readLong();
        long offset = raf.readLong();

        arrayOffset[i] = raf.getFilePointer();
        // Creare un nuovo oggetto LexiconElem e inserirlo nell'HashMap
        LexiconElem lexiconElem = new LexiconElem(term, df, cf, offset);
        return lexiconElem;
    }

    public static String writeEntry(RandomAccessFile raf,String term, int df,long cf, long offset) throws IOException {
        raf.writeUTF(term);
        raf.writeInt(df);
        raf.writeLong(cf);
        raf.writeLong(offset);
        return term;
    }

}
