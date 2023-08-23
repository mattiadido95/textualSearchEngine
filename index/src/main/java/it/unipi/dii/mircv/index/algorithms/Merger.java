package it.unipi.dii.mircv.index.algorithms;

import it.unipi.dii.mircv.index.utility.Logs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

public class Merger {

    private String COLLECTION_PATH;
    private int numberOfFiles;
    private Logs log;

    public Merger(String COLLECTION_PATH, int numberOfFiles) {
        this.COLLECTION_PATH = COLLECTION_PATH;
        this.log = new Logs();
        this.numberOfFiles = numberOfFiles;
    }

    public void execute() {
        // TODO implement merge algorithm
        log.getLog("Start merging ...");
        //load in memory first file index_0

        // get list of files in data/index/ and put them in an array
        ArrayList<String> fileList = new ArrayList<>();
        File directory = new File("data/index/");
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        fileList.add(file.getAbsolutePath());
                    }
                }
            }
        }

        // open each file and read it importing posting lists in memory and search in other files for the same token
        for (String file : fileList) {
           try{
               FileInputStream fileInputStream = new FileInputStream(file);
               FileChannel fileChannel = fileInputStream.getChannel();
               ByteBuffer buffer = ByteBuffer.allocate(1024); // Adjust buffer size as needed

               while (fileChannel.read(buffer) != -1) {
                   buffer.flip();


                   buffer.clear();
               }

               fileChannel.close();
               fileInputStream.close();


           } catch (IOException e) {
               throw new RuntimeException(e);
           }
        }




//        for (int index_counter = 1; index_counter < numberOfFiles; index_counter++) {
//            //load in memory index_i
//            //merge index_0 with index_i
//        }


    }
}

