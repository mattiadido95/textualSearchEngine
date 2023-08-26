package it.unipi.dii.mircv.index.structures;

import it.unipi.dii.mircv.index.utility.Logs;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PostingList {
    private ArrayList<Posting> postings;
    int size;

    Logs log = new Logs();

    public PostingList(Document doc) {
        postings = new ArrayList<>();
        postings.add(new Posting(doc.getDocID(), 1));
        size = 1;
    }

    public PostingList() {
        postings = new ArrayList<>();
        size = 0;
    }

    public ArrayList<Posting> getPostings() {
        return postings;
    }

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder();
        output.append("[");
        for (int i = 0; i < postings.size(); i++) {
            Posting posting = postings.get(i);
            int docID = posting.getDocID();
            int freq = posting.getFreq();

            output.append("(").append(docID).append(", ").append(freq).append(")");

            if (i < postings.size() - 1) {
                output.append(" -> ");
            }
        }
        output.append("]\n");
        //System.out.println(output);
        //System.out.println("**************************************");
        return output.toString();
    }

    // binary search on posting list to find the document return the posting
    public void updatePostingList(Document doc) {
        int docID = doc.getDocID();
        int low = 0;
        int high = this.postings.size() - 1;

        while (low <= high) {
            int mid = (low + high) / 2;
            Posting posting = this.postings.get(mid);
            int postingDocID = posting.getDocID();

            if (postingDocID == docID) {
                posting.updateFreq();
                return;
            } else if (postingDocID < docID) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        // posting list doesn't contain the document, create new posting
        Posting newPosting = new Posting(doc.getDocID(), 1); // create new posting
        this.postings.add(newPosting); // add posting to posting list
        this.size++;
    }

    public int getPostingListSize() {
        return this.size;
    }

    public long savePostingListToDisk(int indexCounter) {
        String filePath;
        if (indexCounter == -1) {
            // TODO implementare scrittura postinglist merge
            filePath = "data/index/index.bin";
        } else {
            filePath = "data/index/index_" + indexCounter + ".bin";
        }
            long offset = -1;

            try {

//            RandomAccessFile randomAccessFile = new RandomAccessFile(filePath, "rw");
//            // Posizionati alla fine del file per l'aggiunta dei dati
//            randomAccessFile.seek(randomAccessFile.length());
//
//            // Memorizza la posizione di inizio nel file
//            offset = randomAccessFile.getFilePointer();
////            System.out.println("Initial offset: " + offset); // Debug: Stampa l'offset
//
//            for (Posting posting : this.postings) {
//                randomAccessFile.writeInt(posting.getDocID());
//                randomAccessFile.writeInt(posting.getFreq());
//            }

                FileOutputStream fileOutputStream = new FileOutputStream(filePath, true);
//            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
//            DataOutputStream dataOutputStream = new DataOutputStream(bufferedOutputStream);
                FileChannel fileChannel = fileOutputStream.getChannel();

                // Memorizza la posizione di inizio nel file
                offset = fileChannel.position();

                // Creare un buffer ByteBuffer per migliorare le prestazioni di scrittura
                ByteBuffer buffer = ByteBuffer.allocate(1024);

                for (Posting posting : this.postings) {
                    buffer.putInt(posting.getDocID());
                    buffer.putInt(posting.getFreq());

                    // Se il buffer è pieno, scrivi il suo contenuto sul file
                    if (!buffer.hasRemaining()) {
                        buffer.flip();
                        fileChannel.write(buffer);
                        buffer.clear();
                    }
                }

                // Scrivi eventuali dati rimanenti nel buffer sul file
                if (buffer.position() > 0) {
                    buffer.flip();
                    fileChannel.write(buffer);
                }

                // Chiudi le risorse
                fileChannel.close();
//            dataOutputStream.close();
//            bufferedOutputStream.close();

            } catch (Exception e) {
                e.printStackTrace();
            }

            return offset;
    }

    public ArrayList<Posting> readPostingList(int indexCounter, int df, long offset) {
        String filePath;
        if(indexCounter == -1) {
            filePath = "data/index/index.bin";
        }else {
            filePath = "data/index/index_" + indexCounter + ".bin";
        }

        ArrayList<Posting> result = new ArrayList<>();

        try {
            FileChannel fileChannel = FileChannel.open(Path.of((filePath)));
            ByteBuffer buffer = ByteBuffer.allocate(8); // Buffer per leggere due interi

            // Posizionati nella posizione desiderata
            fileChannel.position(offset);

            for (int i = 0; i < df; i++) {
                buffer.clear();
                int bytesRead = fileChannel.read(buffer);

                if (bytesRead == -1) {
                    // Non ci sono abbastanza dati nel file
                    break;
                }

                buffer.flip();
                int docID = buffer.getInt();
                int freq = buffer.getInt();
                result.add(new Posting(docID, freq));
            }

//            RandomAccessFile randomAccessFile = new RandomAccessFile(filePath, "r");
//
////            System.out.println("File path: " + filePath); // Debug: Stampa il percorso del file
////            System.out.println("Offset: " + offset); // Debug: Stampa l'offset
//
//            // Posizionati nella posizione desiderata
//            randomAccessFile.seek(offset);
////            System.out.println("Size: " + df); // Debug: Stampa la dimensione della posting list
//
//            for(int i = 0; i < df; i++) {
//                int docID = randomAccessFile.readInt();
//                int freq = randomAccessFile.readInt();
//                result.add(new Posting(docID, freq));
//            }
//
//            randomAccessFile.close();
//
//
////            System.out.println("Dimensione della PostingList: " + result.size());
////            System.out.println("PostingList letta, docID e freq " + result.get(0).getDocID() + ", " + result.get(0).getFreq());

        } catch (IOException e) {
            e.printStackTrace();
        }

        //TODO rendere la funzione statica ed eliminare le due righe sottostanti oppure eliminare la return
        this.postings = result;
        this.size = df;

        return result; // serve forse dopo per ricostruire l'indice
    }


}
