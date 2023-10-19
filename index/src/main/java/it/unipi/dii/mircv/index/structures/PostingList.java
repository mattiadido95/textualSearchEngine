package it.unipi.dii.mircv.index.structures;

import it.unipi.dii.mircv.index.utility.Logs;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * The PostingList class implements a posting list for a term in the inverted index.
 * It stores the list of documents containing the term and the frequency of the term in each document.
 */
public class PostingList {
    private ArrayList<Posting> postings; // array of postings containing docID and frequency relative to a term
    private Iterator<Posting> postingIterator; // iterator for the postings
    private Posting actualPosting; // current posting in the iterator
    Logs log = new Logs();

    /**
     * Constructs a new PostingList object.
     *
     * @param doc The first document containing the term.
     */
    public PostingList(Document doc) {
        postings = new ArrayList<>();
        postings.add(new Posting(doc.getDocID(), 1));
        postingIterator = null;
        actualPosting = null;
    }

    /**
     * Constructs a new PostingList object.
     */
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

    /**
     * Updates the posting list with a new document.
     * If the document is already in the posting list, the frequency is increased by 1.
     * The posting list is sorted by docID and the search is performed with a binary search.
     * If the document is not in the posting list, a new posting is created and added to the posting list.
     *
     * @param doc The document to be added to the posting list.
     */
    public void updatePostingList(Document doc) {
        int docID = doc.getDocID();
        int low = 0;
        int high = this.postings.size() - 1;

        // binary search to find the document in the posting list
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

    /**
     * Initializes the iterator for the posting list
     */
    public void openList() {
        postingIterator = postings.iterator();
    }

    /**
     * Closes the iterator for the posting list and sets the actual posting to null
     */
    public void closeList() {
        actualPosting = null;
        postingIterator = null;
    }

    public Iterator<Posting> getPostingIterator() {
        return postingIterator;
    }

    /**
     * Returns the next posting in the posting list.
     *
     * @return The next posting in the posting list.
     */
    public Posting next() {
        // check if there is another posting in the posting list
        if (postingIterator.hasNext())
            actualPosting = postingIterator.next(); // if there is another posting, set actual posting to the next posting
        else // if there is not another posting, set actual posting to null
            actualPosting = null; // actual posting is null, posting list is finished
        return actualPosting;
    }

    /**
     * Returns the next posting in the posting list with docID greater than or equal to the given docID.
     *
     * @param docId     The docID to be compared with the docID of the next posting.
     * @param bdl       The block descriptor list.
     * @param numBlocks The number of blocks in the block descriptor list.
     * @return The next posting in the posting list with docID greater than or equal to the given docID.
     */
    public Posting nextGEQ(int docId, BlockDescriptorList bdl, int numBlocks, String path) {
        //todo inserire controllo se posting è null dove chiami nextGEQ

        bdl.openBlock(); // initialize the block descriptor list iterator

        // find block that contains the docId
        while (numBlocks > 0 && docId > bdl.next().getMaxDocID()) {
            numBlocks--;
        }
        if (numBlocks == 0) {
            //if no block contains the docId, return null
            return null;
        }
        // docid found, load the posting list
        // check if the postinglist that I need is the one that I have already loaded
        // TODO controllare con matteo. che faccio se non è quello il blocco giusto?
        if (postings.get(bdl.getNumPosting() - 1).getDocID() != bdl.getMaxDocID()) {
            this.readPostingList(-1, bdl.getNumPosting(), bdl.getPostingListOffset(), path);
            this.openList();
            this.next();
        }
        // search in the posting list the docid that i need
        while (actualPosting.getDocID() < docId && postingIterator.hasNext()) {
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

    /**
     * Saves the posting list to disk.
     *
     * @param indexCounter The indexCounter indicates the number of portion of the posting list.
     * @param filePath     The path of the file where the posting list should be saved.
     * @return The start offset of the portion of posting list saved in the file.
     */
    public long savePostingListToDisk(int indexCounter, String filePath) {
        if (indexCounter != -1) // if indexCounter is not -1, the posting list is saved in portions
            filePath += indexCounter + ".bin";

        long offset = -1;

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(filePath, true);
            FileChannel fileChannel = fileOutputStream.getChannel();
            // starting offset in the file
            offset = fileChannel.position();
            ByteBuffer buffer = ByteBuffer.allocate(1024);

            for (Posting posting : this.postings) {
                buffer.putInt(posting.getDocID());
                buffer.putInt(posting.getFreq());
                // if the buffer is full, write the buffer to the file
                if (!buffer.hasRemaining()) {
                    buffer.flip(); // todo che minchia fa?
                    fileChannel.write(buffer);
                    buffer.clear();
                }
            }
            // write the remaining data in the buffer to the file
            if (buffer.position() > 0) {
                buffer.flip();
                fileChannel.write(buffer);
            }
            fileChannel.close();
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return offset;
    }

//    public long savePostingListToDiskTEST(int indexCounter, String filePath) {
//        if (indexCounter != -1) // if indexCounter is not -1, the posting list is saved in portions
//            filePath += indexCounter + ".bin";
//
//        long offset = -1;
//
//        try {
//            boolean fileExists = Files.exists(Paths.get(filePath));
//
//            // Create or open the file in read-write mode
//            RandomAccessFile file = new RandomAccessFile(filePath, "rw");
//            FileChannel fileChannel = file.getChannel();
//
//            // Get the file length before appending
//            offset = file.length();
//
//            // Map the file into memory
//            MappedByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, offset, this.postings.size() * 8L);
//
//            // Write postings to the mapped buffer
//            for (Posting posting : this.postings) {
//                buffer.putInt(posting.getDocID());
//                buffer.putInt(posting.getFreq());
//            }
//
//            // Close the file and release resources
//            fileChannel.close();
//            file.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return offset;
//    }


    /**
     * Reads the posting list from disk.
     *
     * @param indexCounter The indexCounter indicates the number of portion of the posting list.
     * @param df           The df indicates the number of postings in the posting list.
     * @param offset       The offset indicates the start offset of the portion of posting list in the file.
     * @param filePath     The path of the file where the posting list should be read.
     */
    public void readPostingList(int indexCounter, int df, long offset, String filePath) {
    // If indexCounter is not -1, the posting list is saved in portions
    if (indexCounter != -1)
        filePath += indexCounter + ".bin";

    ArrayList<Posting> result = new ArrayList<>(df); // Initialize with the expected size

    try (FileChannel fileChannel = FileChannel.open(Paths.get(filePath))) {
        int bufferSize = 8 * df;
        ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
        buffer.clear();
        fileChannel.position(offset);

        int bytesRead = fileChannel.read(buffer);
        buffer.flip();

        for (int i = 0; i < df; i++) {
            int docID = buffer.getInt();
            int freq = buffer.getInt();
            result.add(new Posting(docID, freq));
        }
    } catch (IOException e) {
        e.printStackTrace();
    }

    this.postings = result;
}

//    public void readPostingList(int indexCounter, int df, long offset, String filePath) {
//        // if indexCounter is not -1, the posting list is saved in portions
//        if (indexCounter != -1)
//            filePath += indexCounter + ".bin";
//
//        ArrayList<Posting> result = new ArrayList<>(); // array of postings initialized to contain the posting list read from disk
//
//        try {
//            FileChannel fileChannel = FileChannel.open(Path.of((filePath)));
//            ByteBuffer buffer = ByteBuffer.allocate(8); // Buffer per leggere due interi
//            // put the file pointer at the start offset of the portion of posting list
//            fileChannel.position(offset);
//            for (int i = 0; i < df; i++) {
//                buffer.clear();
//                int bytesRead = fileChannel.read(buffer);
//                if (bytesRead == -1) // not enough bytes in the file to read
//                    break;
//
//                buffer.flip();
//                int docID = buffer.getInt();
//                int freq = buffer.getInt();
//                result.add(new Posting(docID, freq));
//            }
//            fileChannel.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        this.postings = result;
//    }

}
