package it.unipi.dii.mircv.index.algorithms;

import it.unipi.dii.mircv.index.utility.Logs;

public class Merger {

    private String COLLECTION_PATH;
    private int numberOfFiles;
    private Logs log;

    public Merger(String COLLECTION_PATH,int numberOfFiles) {
        this.COLLECTION_PATH = COLLECTION_PATH;
        this.log = new Logs();
        this.numberOfFiles = numberOfFiles;
    }

    public void execute() {
        // TODO implement merge algorithm
        log.getLog("Start merging ...");
        //load in memory first file index_0

        for(int index_counter = 1; index_counter < numberOfFiles; index_counter++){
            //load in memory index_i
            //merge index_0 with index_i
        }

    }
}
