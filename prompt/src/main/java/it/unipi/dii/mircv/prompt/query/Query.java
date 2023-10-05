package it.unipi.dii.mircv.prompt.query;

import it.unipi.dii.mircv.index.preprocessing.Preprocessing;

import java.util.ArrayList;

/**
 * This class represents a query.
 * It is used to parse the query string and to store the query terms.
 */
public class Query {
    private String query; // query string
    private boolean porterStemmerOption; // porter stemmer option
    private ArrayList<String> queryTerms = new ArrayList<>(); // list of query terms
    private Preprocessing preprocessing;

    public Query( boolean porterStemmerOption, Preprocessing preprocessing) {
//        this.query = query;
        this.porterStemmerOption = porterStemmerOption;
        this.preprocessing = preprocessing;
//        parseQuery();
    }

    private void parseQuery() {
        queryTerms.clear();
        this.preprocessing.queryPreprocess(query, porterStemmerOption);
        queryTerms = (ArrayList<String>) preprocessing.tokens;
    }

    public ArrayList<String> getQueryTerms() {
        return this.queryTerms;
    }

    public void setQuery(String queryInput) {
        this.queryTerms.clear();
        this.query = queryInput;
        parseQuery();
    }

    public void clearQuery() {
        this.queryTerms.clear();
        this.query = "";
    }
}
