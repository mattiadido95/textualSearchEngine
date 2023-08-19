package it.unipi.dii.mircv.index.structures;

import it.unipi.dii.mircv.index.utility.Logs;

import java.util.HashMap;

public class Lexicon {
    HashMap<String, LexiconElem> lexicon;

    Logs log = new Logs();

    public Lexicon() {
        this.lexicon = new HashMap<>();
    }

    public void addLexiconElem(String term) {
        // lexicon contains the term
        if (this.lexicon.containsKey(term)) {
            LexiconElem lexiconElem = this.lexicon.get(term);
//            lexiconElem.incrementDf();
            lexiconElem.incrementCf();
        } else {
            // lexicon does not contain the term
            LexiconElem lexiconElem = new LexiconElem(term);
//            lexiconElem.incrementDf();
            lexiconElem.incrementCf();
            this.lexicon.put(term, lexiconElem);
        }
    }

    public void printLexicon() {
        System.out.println("Lexicon status: ");
        System.out.println(" -> Size: " + this.lexicon.size());
        System.out.println("**************************************");
    }

    @Override
    public String toString() {
        return "Lexicon{" +
                "lexicon=" + lexicon +
                '}';
    }

    public LexiconElem getLexiconElem(String term) {
        return this.lexicon.get(term);
    }
}
