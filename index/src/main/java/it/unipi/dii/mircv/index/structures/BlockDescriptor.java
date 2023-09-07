package it.unipi.dii.mircv.index.structures;

import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;

public class BlockDescriptor {
    private int maxDocID;
    private int numPosting;
    private long postingListOffset;

    public BlockDescriptor(long postingOffsetStart, List<Posting> subList) {
        this.maxDocID = subList.get(subList.size() - 1).getDocID();
        this.numPosting = subList.size();
        this.postingListOffset = postingOffsetStart;
    }

    public long saveBlockDescriptorToDisk() {
        String filePath;
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


}
