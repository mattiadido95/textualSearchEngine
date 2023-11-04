package it.unipi.dii.mircv.prompt.structure;

/**
 * Class that represents a single result of a query
 */
public class QueryResult implements Comparable<QueryResult>{
    String docNo;
    double scoring;

    /**
     * Constructs a QueryResult object with the specified document number and scoring.
     *
     * @param docNo   The document number or identifier associated with the result.
     * @param scoring The scoring value of the result.
     */
    public QueryResult(String docNo, double scoring) {
        this.docNo = docNo;
        this.scoring = scoring;
    }

    /**
     * Get the document number associated with this query result.
     *
     * @return The document number as a string.
     */
    public String getDocNo() {
        return docNo;
    }

    /**
     * Get the scoring value associated with this query result.
     *
     * @return The scoring value as a double.
     */
    public double getScoring() {
        return scoring;
    }

    /**
     * Returns a string representation of the QueryResult object.
     *
     * @return A string representation containing document number and scoring.
     */
    @Override
    public String toString() {
        return "[" +
                "docNo='" + docNo + '\'' +
                ", scoring=" + scoring +
                ']';
    }

    /**
     * Compares QueryResult objects based on their scoring values.
     *
     * @param o The QueryResult to compare to.
     * @return A negative value if this result has a higher scoring, a positive value if o has a higher scoring, and 0 if they are equal.
     */
    @Override
    public int compareTo(QueryResult o) {
        return Double.compare(o.scoring, this.scoring);
    }
}
