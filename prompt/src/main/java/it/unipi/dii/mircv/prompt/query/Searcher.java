package it.unipi.dii.mircv.prompt.query;

import it.unipi.dii.mircv.index.structures.*;

import java.util.ArrayList;

public class Searcher {

    private String term;
    private Lexicon lexicon;
    private ArrayList<Document> documents;

    public Searcher(Lexicon lexicon, ArrayList<Document> documents) {
        // TODO cosi carico ma poi passo le strutture in copia al searcher e occupo il doppio !!!!
        this.lexicon = lexicon;
        this.documents = documents;
    }

    public Searcher() {
    }

    public ArrayList<String> search(ArrayList<String> queryTerms, Lexicon lexicon, ArrayList<Document> documents) {
        ArrayList<String> pid_results = new ArrayList<String>();
        for (String term : queryTerms) {
            pid_results.addAll(searchTerm(term, lexicon, documents));
        }
        return pid_results;
    }

    public ArrayList<String> searchTerm(String term, Lexicon lexicon, ArrayList<Document> documents) {
        this.term = term;
        ArrayList<String> term_pid_results = new ArrayList<String>();
        if (lexicon.getLexicon().containsKey(term)) {
            System.out.println("Term " + term + " found in lexicon");
            // get lexicon element
            LexiconElem lexiconElem = lexicon.getLexiconElem(term);
            // use lexiconElem.offset and lexiconElem.df to read posting list from index.bin
            // get posting list
            PostingList postingList = new PostingList();
            postingList.readPostingList(-1, lexiconElem.getDf(), lexiconElem.getOffset());
            // for each posting in posting list get document pid
            for( Posting posting : postingList.getPostings()){
                // get document
                Document document = documents.get(posting.getDocID());
                // get document pid
                String pid = document.getDocNo();
                // add pid to results
                term_pid_results.add(pid);
            }
        } else {
            System.out.println("Term " + term + " not found in lexicon");
        }

        return term_pid_results;
    }
}
