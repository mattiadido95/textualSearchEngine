package  it.unipi.dii.mircv.main;
import it.unipi.dii.mircv.preprocessing.Preprocessing;

public class Main {
    public static void main(String[] args) {
        System.out.println("Start preprocessing ...");
        // start preprocessing of collection.tsv file
        Preprocessing preprocessing = new Preprocessing("data/collection/collection.tsv");
    }
}