package it.unipi.dii.mircv.index.structures;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * The BlockDescriptorList class represents a list of BlockDescriptors.
 * It is used to read BlockDescriptors from the index file.
 */
public class BlockDescriptorList {
    private ArrayList<BlockDescriptor> blockDescriptors;
    private Iterator<BlockDescriptor> blockDescriptorIterator; // iterator for block descriptors list
    private BlockDescriptor actualBlockDescriptor; // current block descriptor during iteration

    /**
     * Constructor to initialize a BlockDescriptorList.
     *
     * @param offset   The offset in the file where reading begins.
     * @param numBlock The number of BlockDescriptors to read from the file.
     * @param filePath The path to the binary file containing BlockDescriptors.
     */
    public BlockDescriptorList(long offset, int numBlock, String filePath) {
        blockDescriptors = readBlockDescriptorList(offset, numBlock, filePath);
    }

    /**
     * Open the block descriptor list for iteration.
     */
    public void openBlock() {
        blockDescriptorIterator = blockDescriptors.iterator();
    }

    /**
     * Close the block descriptor list after iteration.
     */
    public void closeBlock() {
        blockDescriptorIterator = null;
    }

    /**
     * Retrieve the next BlockDescriptor in the list.
     *
     * @return The next BlockDescriptor or null if there are no more.
     */
    public BlockDescriptor next() {
        if (blockDescriptorIterator.hasNext())
            actualBlockDescriptor = blockDescriptorIterator.next();
        else
            actualBlockDescriptor = null;
        return actualBlockDescriptor;
    }

    /**
     * Check if there are more BlockDescriptors in the list.
     *
     * @return True if there are more BlockDescriptors, otherwise false.
     */
    public boolean hasNext() {
        return blockDescriptorIterator.hasNext();
    }

    /**
     * Get the maximum DocID of the current BlockDescriptor.
     *
     * @return The maximum DocID.
     */
    public int getMaxDocID() {
        return actualBlockDescriptor.getMaxDocID();
    }

    /**
     * Get the number of postings in the current BlockDescriptor.
     *
     * @return The number of postings.
     */
    public int getNumPosting() {
        return actualBlockDescriptor.getNumPosting();
    }

    /**
     * Get the offset of the posting list in the current BlockDescriptor.
     *
     * @return The offset of the posting list.
     */
    public long getPostingListOffset() {
        return actualBlockDescriptor.getPostingListOffset();
    }

    /**
     * Read a list of BlockDescriptors from a binary file starting at a specified offset.
     *
     * @param startOffset The offset in the file where reading begins.
     * @param numBlocks   The number of BlockDescriptors to read from the file.
     * @param filePath    The path to the binary file containing BlockDescriptors.
     * @return An ArrayList of BlockDescriptors read from the file.
     */
    public ArrayList<BlockDescriptor> readBlockDescriptorList(long startOffset, int numBlocks, String filePath) {
        ArrayList<BlockDescriptor> result = new ArrayList<>();
        int bufferSize = 16 * numBlocks; // size is 16 bytes for block structure multiplied by the number of block descriptors in the list

        try (FileChannel fileChannel = FileChannel.open(Paths.get(filePath))) {
            ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
            buffer.clear();
            fileChannel.position(startOffset);
            fileChannel.read(buffer);
            buffer.flip();

            // read each block descriptor from the buffer
            for (int i = 0; i < numBlocks; i++) {
                BlockDescriptor blockDescriptor = new BlockDescriptor();
                blockDescriptor.setMaxDocID(buffer.getInt());
                blockDescriptor.setNumPosting(buffer.getInt());
                blockDescriptor.setPostingListOffset(buffer.getLong());
                result.add(blockDescriptor);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

}
