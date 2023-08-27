package it.unipi.dii.mircv.prompt.query;

import it.unipi.dii.mircv.index.preprocessing.Preprocessing;

import java.util.ArrayList;

public class Query {

    private String query; // query string
    private ArrayList<String> queryTerms = new ArrayList<String>(); // list of query terms

    public Query(String query) {
        this.query = query;
        parseQuery();
    }
    private void parseQuery() {
        queryTerms.clear();
        Preprocessing preprocessing = new Preprocessing(query);
        queryTerms = (ArrayList<String>) preprocessing.tokens;
    }
    public ArrayList<String> getQueryTerms() {
        return this.queryTerms;
    }
}
