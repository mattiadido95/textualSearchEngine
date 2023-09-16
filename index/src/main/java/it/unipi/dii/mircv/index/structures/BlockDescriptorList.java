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


    public BlockDescriptorList(long offset, int numBlock) {
        blockDescriptors = readBlockDescriptorList(offset, numBlock);
    }

    public void openBlock(){
        blockDescriptorIterator = blockDescriptors.iterator();
    }

    public void closeBlock(){
        blockDescriptorIterator = null;
    }

    public BlockDescriptor next(){
        if(blockDescriptorIterator.hasNext())
            actualBlockDescriptor = blockDescriptorIterator.next();
        else
            actualBlockDescriptor = null;
        return actualBlockDescriptor;
    }

    public boolean hasNext(){
        return blockDescriptorIterator.hasNext();
    }
    public int getMaxDocID() {
        return actualBlockDescriptor.getMaxDocID();
    }

    public int getNumPosting() {
        return actualBlockDescriptor.getNumPosting();
    }
    public long getPostingListOffset() {
        return actualBlockDescriptor.getPostingListOffset();
    }

    public ArrayList<BlockDescriptor> readBlockDescriptorList(long startOffset, int numBlocks) {
        String filePath;
        filePath = "data/index/blockDescriptor.bin";

        ArrayList<BlockDescriptor> result = new ArrayList<>();

        try {
            FileChannel fileChannel = FileChannel.open(Path.of((filePath)));

            // Memorizza la posizione di inizio nel file
            fileChannel.position(startOffset);

            // Creare un buffer ByteBuffer per migliorare le prestazioni di scrittura
            ByteBuffer buffer = ByteBuffer.allocate(1024);

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
