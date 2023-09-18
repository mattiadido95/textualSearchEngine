package it.unipi.dii.mircv.index.structures;

import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BlockDescriptor {
    private int maxDocID;
    private int numPosting; // number of posting in the block
    private long postingListOffset;

    public BlockDescriptor(long postingOffsetStart, List<Posting> subList) {
        this.maxDocID = subList.get(subList.size() - 1).getDocID();
        this.numPosting = subList.size();
        this.postingListOffset = postingOffsetStart;
    }

    public BlockDescriptor() {
    }

    public int getMaxDocID() {
        return this.maxDocID;
    }

    public int getNumPosting() {
        return this.numPosting;
    }

    public long getPostingListOffset() {
        return this.postingListOffset;
    }

    public void setPostingListOffset(long postingListOffset) {
        this.postingListOffset = postingListOffset;
    }

    public void setNumPosting(int numPosting) {
        this.numPosting = numPosting;
    }

    public void setMaxDocID(int maxDocID) {
        this.maxDocID = maxDocID;
    }

    public long saveBlockDescriptorToDisk(boolean testing) {
        String filePath;
        if (testing)
            filePath = "src/test/data/blockDescriptorTest.bin";
        else
            filePath = "data/index/blockDescriptor.bin";
        long offset = -1;

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(filePath, true);
            FileChannel fileChannel = fileOutputStream.getChannel();

            // Memorizza la posizione di inizio nel file
            offset = fileChannel.position();

            // Creare un buffer ByteBuffer per migliorare le prestazioni di scrittura
            ByteBuffer buffer = ByteBuffer.allocate(1024);

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


    public static BlockDescriptor readFirstBlock(long offset, boolean testing) {
        String filePath;
        if (testing)
            filePath = "src/test/data/blockDescriptorTest.bin";
        else
            filePath = "data/index/blockDescriptor.bin";

        BlockDescriptor result = new BlockDescriptor();

        try {
            FileChannel fileChannel = FileChannel.open(Path.of((filePath)));

            // Memorizza la posizione di inizio nel file
            fileChannel.position(offset);

            // Creare un buffer ByteBuffer per migliorare le prestazioni di scrittura
            ByteBuffer buffer = ByteBuffer.allocate(1024);

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
