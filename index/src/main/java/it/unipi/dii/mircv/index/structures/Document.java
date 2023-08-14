package it.unipi.dii.mircv.index.structures;

public class Document {
    private String id = null;
    private String body = null;

    private String rawDocument;

    public Document(String rawDocument) {
        this.rawDocument = rawDocument;
        parseDocument();
    }

    private void parseDocument() {
        // parse the raw document and set id and body
        String split[] = rawDocument.split("\t");
        this.id = split[0]; // TODO CONTROLLARE SE è VERAMENTE L'ID
        this.body = split[1];
    }

    public String getDocID() {
        return this.id;
    }

    public String getBody() {
        return this.body;
    }
}
