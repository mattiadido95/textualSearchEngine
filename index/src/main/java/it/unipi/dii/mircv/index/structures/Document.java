package it.unipi.dii.mircv.index.structures;

public class Document {
    private String docID = null;
    private String docNo = null;
    private String URL = null;
    private String PR = null;
    private String body = null;
    private int length = 0; // TODO da implementare

    private String rawDocument;

    public Document(String rawDocument) {
        this.rawDocument = rawDocument;
        parseDocument();
    }

    private void parseDocument() {
        // parse the raw document and set id and body
        String split[] = rawDocument.split("\t");
        this.docID = split[0]; // TODO CONTROLLARE SE è VERAMENTE L'ID, docno != da pid
        this.body = split[1];
    }

    public String getDocID() {
        return this.docID;
    }

    public String getBody() {
        return this.body;
    }
}
