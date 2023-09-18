package it.unipi.dii.mircv.index.structures;

import it.unipi.dii.mircv.index.utility.Logs;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class PostingList {
    private ArrayList<Posting> postings;
    private Iterator<Posting> postingIterator;
    private Posting actualPosting;

    private static final String INDEX_PATH = "data/index/index.bin";

    Logs log = new Logs();

    public PostingList(Document doc) {
        postings = new ArrayList<>();
        postings.add(new Posting(doc.getDocID(), 1));
        postingIterator = null;
        actualPosting = null;
    }

    public PostingList() {
        postings = new ArrayList<>();
        postingIterator = null;
        actualPosting = null;
    }

    public ArrayList<Posting> getPostings() {
        return postings;
    }

    public void mergePosting(PostingList postingList) {
        this.postings.addAll(postingList.getPostings());
        log.getLog(this.postings);
    }

    public Posting getActualPosting() {
        return actualPosting;
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

    public void openList() {
        postingIterator = postings.iterator();
    }

    public void closeList() {
        postingIterator = null;
    }

    public Posting next() {
        if (postingIterator.hasNext())
            actualPosting = postingIterator.next();
        else
            actualPosting = null;
        return actualPosting;
    }

    public Posting nextGEQ(int docId, BlockDescriptorList bdl, int numBlocks) {
        bdl.openBlock();
        // cerca il blocco che contiene il docId
        while (numBlocks > 0 && docId > bdl.next().getMaxDocID()) {
            numBlocks--;
        }
        // carica la relativa posting list
        //controllo se postinglist caricata è quella del blocco di interesse
        if (postings.get(bdl.getNumPosting() - 1).getDocID() != bdl.getMaxDocID()) {
            this.readPostingList(-1, bdl.getNumPosting(), bdl.getPostingListOffset(),INDEX_PATH);
            this.openList();
            this.next();
        }
        // scorri la posting list fino a trovare il docId
        while(actualPosting.getDocID() < docId && postingIterator.hasNext()) {
            actualPosting = postingIterator.next();
        }

        return actualPosting;
    }

    public int getDocId() {
        return actualPosting.getDocID();
    }

    public int getFreq() {
        return actualPosting.getFreq();
    }

    public boolean hasNext() {
        return postingIterator.hasNext();
    }

    public int getMinDocId() {
        return this.postings.get(0).getDocID();
    }

    public int getPostingListSize() {
        return this.getPostings().size();
    }

    public long savePostingListToDisk(int indexCounter, String filePath) {
        if (indexCounter != -1)
            filePath += indexCounter + ".bin";

        long offset = -1;

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(filePath, true);
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
            fileOutputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return offset;

    }

    public ArrayList<Posting> readPostingList(int indexCounter, int df, long offset, String filePath) {
//        {
//            filePath = "data/index/index.bin";
//        }else if(indexCounter == -2) { // test folder
//            filePath = "src/test/data/index.bin";
//        }else {
        if (indexCounter != -1)
            filePath += indexCounter + ".bin";

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
            fileChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //TODO rendere la funzione statica ed eliminare le due righe sottostanti oppure eliminare la return
        this.postings = result;

        return result; // serve forse dopo per ricostruire l'indice
    }


}
