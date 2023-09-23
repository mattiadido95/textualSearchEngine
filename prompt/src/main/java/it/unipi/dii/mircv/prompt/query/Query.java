package it.unipi.dii.mircv.prompt.query;

import it.unipi.dii.mircv.index.preprocessing.Preprocessing;

import java.util.ArrayList;

public class Query {
    private String query; // query string
    private boolean porterStemmerOption; // porter stemmer option
    private ArrayList<String> queryTerms = new ArrayList<>(); // list of query terms

    public Query(String query, boolean porterStemmerOption) {
        this.query = query;
        this.porterStemmerOption = porterStemmerOption;
        parseQuery();
    }
    private void parseQuery() {
        queryTerms.clear();
        Preprocessing preprocessing = new Preprocessing(query, porterStemmerOption);
        queryTerms = (ArrayList<String>) preprocessing.tokens;
    }
    public ArrayList<String> getQueryTerms() {
        return this.queryTerms;
    }
}
