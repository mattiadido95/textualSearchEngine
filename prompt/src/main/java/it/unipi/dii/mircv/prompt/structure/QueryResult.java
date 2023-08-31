package it.unipi.dii.mircv.prompt.structure;

public class QueryResult implements Comparable<QueryResult>{
    String docNo;
    double scoring;

    public QueryResult(String docNo, double scoring) {
        this.docNo = docNo;
        this.scoring = scoring;
    }

    @Override
    public String toString() {
        return "QueryResult{" +
                "docNo='" + docNo + '\'' +
                ", scoring=" + scoring +
                '}';
    }

    @Override
    public int compareTo(QueryResult o) {
        return Double.compare(o.scoring, this.scoring);
    }
}
