package it.unipi.dii.mircv.index.structures;

import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The BlockDescriptor class represents metadata for a block of postings within the index file.
 * It includes information such as the maximum document ID, the number of postings in the block, and the offset
 * of the associated posting list in the index file.
 */
public class BlockDescriptor {
    private int maxDocID;
    private int numPosting; // number of posting in the block, is dynamic
    private long postingListOffset;

    /**
     * Constructs a BlockDescriptor for a block of postings.
     *
     * @param postingOffsetStart The starting offset of the posting list associated with this block.
     * @param subList            The list of postings within the block.
     */
    public BlockDescriptor(long postingOffsetStart, List<Posting> subList) {
        this.maxDocID = subList.get(subList.size() - 1).getDocID();
        this.numPosting = subList.size();
        this.postingListOffset = postingOffsetStart;
    }

     /**
     * Constructs an empty BlockDescriptor.
     */
    public BlockDescriptor() {
    }

    /**
     GETTER AND SETTER
     */

    /**
     * Retrieves the maximum document ID in the block.
     *
     * @return The maximum document ID.
     */
    public int getMaxDocID() {
        return this.maxDocID;
    }

    /**
     * Retrieves the number of postings in the block.
     *
     * @return The number of postings.
     */
    public int getNumPosting() {
        return this.numPosting;
    }

    /**
     * Retrieves the offset of the posting list associated with this block in the index file.
     *
     * @return The posting list offset.
     */
    public long getPostingListOffset() {
        return this.postingListOffset;
    }

    /**
     * Sets the offset of the posting list associated with this block in the index file.
     *
     * @param postingListOffset The posting list offset to set.
     */
    public void setPostingListOffset(long postingListOffset) {
        this.postingListOffset = postingListOffset;
    }

     /**
     * Sets the number of postings in the block.
     *
     * @param numPosting The number of postings to set.
     */
    public void setNumPosting(int numPosting) {
        this.numPosting = numPosting;
    }

    /**
     * Sets the maximum document ID in the block.
     *
     * @param maxDocID The maximum document ID to set.
     */
    public void setMaxDocID(int maxDocID) {
        this.maxDocID = maxDocID;
    }

    /**
     * Saves the block descriptor to the index file.
     *
     * @param filePath The path to the index file.
     * @return The offset where the block descriptor is stored in the index file.
     */
    public long saveBlockDescriptorToDisk(String filePath) {
        long offset = -1;

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(filePath, true);
            FileChannel fileChannel = fileOutputStream.getChannel();
            // Memorizza la posizione di inizio nel file
            offset = fileChannel.position();
            // Creare un buffer ByteBuffer per migliorare le prestazioni di scrittura
            ByteBuffer buffer = ByteBuffer.allocate(16);

            buffer.putInt(maxDocID);
            buffer.putInt(numPosting);
            buffer.putLong(postingListOffset);

            // Se il buffer è pieno, scrivi il suo contenuto sul file
            if (!buffer.hasRemaining()) {
                buffer.flip();
                fileChannel.write(buffer);
                buffer.clear();
            }
            // Scrivi eventuali dati rimanenti nel buffer sul file
            if (buffer.position() > 0) {
                buffer.flip();
                fileChannel.write(buffer);
            }
            // Chiudi le risorse
            fileChannel.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return offset;

    }

    /**
     * Reads and retrieves the first block descriptor from the index file.
     *
     * @param offset   The offset where the block descriptor is stored in the index file.
     * @param filePath The path to the index file.
     * @return The BlockDescriptor object read from the index file.
     */

    public static BlockDescriptor readFirstBlock(long offset, String filePath) {
        BlockDescriptor result = new BlockDescriptor();

        try {
            FileChannel fileChannel = FileChannel.open(Path.of((filePath)));
            // Memorizza la posizione di inizio nel file
            fileChannel.position(offset);
            // Creare un buffer ByteBuffer per migliorare le prestazioni di scrittura
            ByteBuffer buffer = ByteBuffer.allocate(16);

            buffer.clear();
            fileChannel.read(buffer);
            buffer.flip();

            result.setMaxDocID(buffer.getInt());
            result.setNumPosting(buffer.getInt());
            result.setPostingListOffset(buffer.getLong());

            // Chiudi le risorse
            fileChannel.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
