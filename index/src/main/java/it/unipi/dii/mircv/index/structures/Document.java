package it.unipi.dii.mircv.index.structures;

public class Document {
    private int docID = -1;
    private String docNo;
    private String URL;
    private String PR;
    private String body;
    private int length = 0; // TODO da implementare

    private String rawDocument;

    public Document(String rawDocument, int docID) {
        this.docID = docID;
        this.rawDocument = rawDocument;
        parseDocument();
    }

    private void parseDocument() {
        // parse the raw document and set id and body
        String split[] = rawDocument.split("\t");
        this.docNo = split[0]; // TODO CONTROLLARE SE è VERAMENTE L'ID, docno != da pid
        this.body = split[1];
    }

    public int getDocID() {
        return this.docID;
    }

    public String getBody() {
        return this.body;
    }
}
