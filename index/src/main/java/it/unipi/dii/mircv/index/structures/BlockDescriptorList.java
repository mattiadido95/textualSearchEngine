package it.unipi.dii.mircv.index.structures;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;

public class BlockDescriptorList {
    private ArrayList<BlockDescriptor> blockDescriptors;
    private Iterator<BlockDescriptor> blockDescriptorIterator;
    private BlockDescriptor actualBlockDescriptor;

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
        try {
            FileChannel fileChannel = FileChannel.open(Path.of((filePath)));
            // Memorizza la posizione di inizio nel file
            fileChannel.position(startOffset);
            // Creare un buffer ByteBuffer per migliorare le prestazioni di scrittura
            ByteBuffer buffer = ByteBuffer.allocate(16);

            for (int i = 0; i < numBlocks; i++) {
                buffer.clear();
                fileChannel.read(buffer);
                buffer.flip();
                BlockDescriptor blockDescriptor = new BlockDescriptor();
                blockDescriptor.setMaxDocID(buffer.getInt());
                blockDescriptor.setNumPosting(buffer.getInt());
                blockDescriptor.setPostingListOffset(buffer.getLong());
                result.add(blockDescriptor);
            }
            // Chiudi le risorse
            fileChannel.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
