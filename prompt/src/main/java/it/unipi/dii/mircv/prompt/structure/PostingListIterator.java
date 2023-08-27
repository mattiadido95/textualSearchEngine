package it.unipi.dii.mircv.prompt.structure;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.ArrayList;

public class PostingListIterator {

    private static final Integer POSTING_DIM = 8;

    private ArrayList<Long> offset;
    private ArrayList<Long> cursor;
    private ArrayList<Integer> df;
    private static FileChannel fileChannel = null;
    private ByteBuffer buffer;

    public PostingListIterator() {
        this.offset = new ArrayList<>();
        this.cursor = new ArrayList<>();
        this.df = new ArrayList<>();
        this.buffer = ByteBuffer.allocate(4);
    }

    public void addOffset(long offset) {
        this.offset.add(offset);
        this.cursor.add(offset);
    }

    public void addDf(int df) {
        this.df.add(df);
    }

    public ArrayList<Long> getOffset() {
        return offset;
    }

    public ArrayList<Long> getCursor() {
        return cursor;
    }

    public ArrayList<Integer> getDf() {
        return df;
    }

    public FileChannel openList() {
        if(fileChannel != null)
            return fileChannel;
        try {
            fileChannel = FileChannel.open(Path.of(("data/index/index.bin")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileChannel;
    }

    public void closeList() {
        if(fileChannel == null)
            return;
        try {
            fileChannel.close();
            fileChannel = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getDocId(int index){
        int result = -1;
        try {
            buffer.clear();
            fileChannel.position(cursor.get(index));
            fileChannel.read(buffer);
            buffer.flip();
            if (buffer.remaining() >= 4)
                result = buffer.getInt();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    public int getFreq(int index){
        int result = -1;
        if(cursor.get(index) + 8 > offset.get(index) + (df.get(index) * POSTING_DIM)){
            return result;
        }
        try {
            buffer.clear();
            fileChannel.position(cursor.get(index) + 4);
            fileChannel.read(buffer);
            buffer.flip();
            if (buffer.remaining() >= 4)
                result = buffer.getInt();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    public void next(int index){
        cursor.set(index, cursor.get(index) + POSTING_DIM);
    }

}
