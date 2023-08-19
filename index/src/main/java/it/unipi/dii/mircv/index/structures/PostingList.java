package it.unipi.dii.mircv.index.structures;

import it.unipi.dii.mircv.index.utility.Logs;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PostingList implements Serializable {
    private ArrayList<Posting> postings;

    Logs log = new Logs();

    public PostingList(Document doc) {
        postings = new ArrayList<>();
        postings.add(new Posting(doc.getDocID(), 1));
    }

    public PostingList() {
        postings = new ArrayList<>();
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
    }

    public int getPostingListSize() {
        return this.postings.size();
    }

    public long savePostingListToDisk(int indexCounter)  {
        String filePath = "data/index/index_" + indexCounter + ".bin";

        long offset = -1;

        try (FileChannel channel = new FileOutputStream(filePath, true).getChannel()) {

            offset = channel.position();

            // Serializza l'ArrayList in un array di byte
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(this.postings);
            objectOutputStream.close();

            // Ottieni l'array di byte serializzato
            byte[] serializedArrayList = byteArrayOutputStream.toByteArray();

            // Scrivi la lunghezza dell'ArrayList come un intero
            ByteBuffer intBuffer = ByteBuffer.allocate(Integer.BYTES);
            intBuffer.putInt(serializedArrayList.length);
            intBuffer.flip(); // Prepara il buffer per la lettura

            // Scrivi la lunghezza e l'array serializzato nel FileChannel
            channel.write(intBuffer);
            channel.write(ByteBuffer.wrap(serializedArrayList));

//            System.out.println("ArrayList serialized and written successfully!");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return offset;
    }

    public ArrayList<Posting> readPostingList(int indexCounter, long offset) {
        String filePath = "data/index/index_" + indexCounter + ".bin";
        ArrayList<Posting> postings = new ArrayList<>();

        try (FileChannel channel = new FileInputStream(filePath).getChannel()) {
            // Imposta la posizione di lettura all'offset specificato
            channel.position(offset);

            // Leggi la lunghezza dell'ArrayList (intero) dal FileChannel
            ByteBuffer intBuffer = ByteBuffer.allocate(Integer.BYTES);
            channel.read(intBuffer);
            intBuffer.flip(); // Prepara il buffer per la lettura
            int arrayListLength = intBuffer.getInt();

            // Leggi l'array serializzato dalla lunghezza specificata
            ByteBuffer serializedArrayListBuffer = ByteBuffer.allocate(arrayListLength);
            channel.read(serializedArrayListBuffer);
            byte[] serializedArrayList = serializedArrayListBuffer.array();

            // Deserializza l'ArrayList dall'array serializzato
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(serializedArrayList);
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            postings = (ArrayList<Posting>) objectInputStream.readObject();
            objectInputStream.close();

//            System.out.println("ArrayList read from file successfully!");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return postings;
    }
}
