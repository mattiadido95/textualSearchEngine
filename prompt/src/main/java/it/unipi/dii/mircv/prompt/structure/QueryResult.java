package it.unipi.dii.mircv.prompt.structure;

public class QueryResult implements Comparable<QueryResult>{
    String docNo;
    double scoring;

    public QueryResult(String docNo, double scoring) {
        this.docNo = docNo;
        this.scoring = scoring;
    }

    public String getDocNo() {
        return docNo;
    }

    public void setDocNo(String docNo) {
        this.docNo = docNo;
    }

    public double getScoring() {
        return scoring;
    }

    public void setScoring(double scoring) {
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
