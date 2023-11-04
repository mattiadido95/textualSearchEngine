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

    /**
     * Constructor for Query class.
     *
     * @param porterStemmerOption True if Porter stemming should be applied, false otherwise.
     * @param preprocessing Preprocessing instance for query processing.
     */
    public Query( boolean porterStemmerOption, Preprocessing preprocessing) {
        this.porterStemmerOption = porterStemmerOption;
        this.preprocessing = preprocessing;
    }

    /**
     * Parse the query string and store the query terms in the queryTerms list.
     */
    private void parseQuery() {
        queryTerms.clear();
        this.preprocessing.queryPreprocess(query, porterStemmerOption);
        queryTerms = (ArrayList<String>) preprocessing.tokens;
    }

    /**
     * Get the list of query terms.
     *
     * @return ArrayList of query terms.
     */
    public ArrayList<String> getQueryTerms() {
        return this.queryTerms;
    }

    /**
     * Set the query string and parse it to update the query terms.
     *
     * @param queryInput The query string to be set.
     * @return This Query instance.
     */
    public Query setQuery(String queryInput) {
        this.queryTerms.clear();
        this.query = queryInput;
        parseQuery();
        return this;
    }

    /**
     * Clear the query and query terms.
     */
    public void clearQuery() {
        this.queryTerms.clear();
        this.query = "";
    }
}
