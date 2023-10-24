package it.unipi.dii.mircv.index.structures;

import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
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
            // get the current position of the file pointer
            offset = fileChannel.position();
            // create a ByteBuffer buffer to improve write performance
            ByteBuffer buffer = ByteBuffer.allocate(16);
            buffer.putInt(maxDocID);
            buffer.putInt(numPosting);
            buffer.putLong(postingListOffset);

            // check if the buffer is full, if so, write it to the file
            if (!buffer.hasRemaining()) {
                buffer.flip();
                fileChannel.write(buffer);
                buffer.clear();
            }
            // write the remaining bytes to the file
            if (buffer.position() > 0) {
                buffer.flip();
                fileChannel.write(buffer);
            }
            // close file channel
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
            // get the current position of the file pointer
            fileChannel.position(offset);
            // create a ByteBuffer buffer to improve read performance
            ByteBuffer buffer = ByteBuffer.allocate(16);
            buffer.clear();
            fileChannel.read(buffer);
            buffer.flip();

            result.setMaxDocID(buffer.getInt());
            result.setNumPosting(buffer.getInt());
            result.setPostingListOffset(buffer.getLong());

            // close file channel
            fileChannel.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
