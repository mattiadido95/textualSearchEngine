package it.unipi.dii.mircv.index.utility;

public class MemoryManager {

    private long freeMemoryPercentage = 0;

    public MemoryManager() {
        setFreeMemoryPercentage();
    }

    public void printMemory() {
        long freeMemoryMB = bytesToMegabytes(getFreeMemory());
        long totalMemoryMB = bytesToMegabytes(getTotalMemory());

        System.out.println("Memory status:");
        System.out.println(" -> Free memory: " + freeMemoryMB + " MB");
        System.out.println(" -> Total memory: " + totalMemoryMB + " MB");
        System.out.println(" -> Free memory percentage: " + this.freeMemoryPercentage + "%");
        System.out.println("**************************************");
    }

    private long bytesToMegabytes(long bytes) {
        return bytes / (1024 * 1024); // 1 megabyte = 1024 * 1024 bytes
    }


    private long getFreeMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.freeMemory();
    }

    private long getTotalMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory();
    }

    private void setFreeMemoryPercentage() {
        this.freeMemoryPercentage = (bytesToMegabytes(getFreeMemory()) * 100) / bytesToMegabytes(getTotalMemory());
    }

    public boolean checkFreeMemory() {
        return this.freeMemoryPercentage > 30 ? false : true;
    }

    public void saveInvertedIndexToDisk() {
        // TODO implement method to save inverted index to disk
    }
}
