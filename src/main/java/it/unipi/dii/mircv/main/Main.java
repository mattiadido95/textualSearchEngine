package  it.unipi.dii.mircv.main;
import it.unipi.dii.mircv.preprocessing.Preprocessing;

public class Main {
    public static void main(String[] args) {
        System.out.println("Start preprocessing");
        // start preprocessing
        Preprocessing preprocessing = new Preprocessing("../dataset");
    }
}