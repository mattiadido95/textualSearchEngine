package it.unipi.dii.mircv.index.structures;

import it.unipi.dii.mircv.index.utility.Logs;

import java.util.HashMap;

public class Lexicon {
    HashMap<String, LexiconElem> lexicon;

    Logs log = new Logs();

    public Lexicon() {
        this.lexicon = new HashMap<>();
    }

    public HashMap<String, LexiconElem> getLexicon() {
        return this.lexicon;
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

    public void sortLexicon() {
        /**
         * this.lexicon: Questo fa riferimento a una mappa chiamata lexicon nell'oggetto corrente (presumibilmente una variabile di istanza nella classe).
         * .entrySet().stream(): Converti la mappa in uno stream di oggetti Map.Entry, che rappresentano le coppie chiave-valore della mappa.
         * .sorted((e1, e2) -> e1.getValue().getTerm().compareTo(e2.getValue().getTerm())): Ordini gli elementi dello stream in base al valore dell'oggetto Lexicon associato alla chiave. La funzione di comparazione prende due oggetti Map.Entry (e quindi coppie chiave-valore) e confronta i termini (getTerm()) degli oggetti Lexicon associati ai rispettivi valori.
         * .collect(...): Raccogli gli elementi ordinati in una nuova mappa.
         * HashMap::new: Fornisce un costruttore di HashMap per creare una nuova mappa.
         * (m, e) -> m.put(e.getKey(), e.getValue()): Definisce come mettere gli elementi nella mappa di destinazione durante la raccolta. Per ogni elemento nell'stream, mette la coppia chiave-valore nell'oggetto HashMap di destinazione (m).
         * HashMap::putAll: Combinare le mappe. Questo passo consente di inserire tutte le coppie chiave-valore dalla mappa raccolta nell'oggetto this.lexicon originale.
         */
        this.lexicon = this.lexicon.entrySet().stream()
                .sorted((e1, e2) -> e1.getValue().getTerm().compareTo(e2.getValue().getTerm()))
                .collect(HashMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), HashMap::putAll);
    }
}
