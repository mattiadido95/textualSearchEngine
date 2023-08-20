package it.unipi.dii.mircv.index.structures;

import java.io.*;
import java.util.ArrayList;

public class Document{
    private int docID;
    private String docNo;
    private String URL;
    private String PR;
    private String body;
    private int length;
    private String rawDocument;

    public Document(String rawDocument, int docID) {
        this.docID = docID;
        this.rawDocument = rawDocument;
        length = 0;
        parseDocument();
    }

    public Document() {

    }

    private void parseDocument() {
        // parse the raw document and set id and body
        String split[] = rawDocument.split("\t");
        this.docNo = split[0]; // TODO CONTROLLARE SE è VERAMENTE L'ID, docno != da pid
        this.body = split[1];
    }

    @Override
    public String toString() {
        return "Document{" +
                "docID=" + docID +
                ", docNo='" + docNo + '\'' +
                ", length=" + length +
                '}';
    }

    public int getDocID() {
        return this.docID;
    }

    public String getBody() {
        return this.body;
    }

    public void setLength(int length){
        this.length = length;
    }

    public void saveDocumentToDisk(){
        String filePath = "data/index/documents.bin";
        try {
            // Crea un ObjectOutputStream per scrivere oggetti su un file
            FileOutputStream fileOutputStream = new FileOutputStream(filePath, true);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);

            // Scrivi l'oggetto nel file
            objectOutputStream.writeInt(docID);
            objectOutputStream.writeUTF(docNo);
            objectOutputStream.writeInt(length);

            // Chiudi lo stream di output
            objectOutputStream.close();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<Document> loadDocumentFromDisk(int numberOfDocuments){
        String filePath = "data/index/documents.bin";
        ArrayList<Document> documents = new ArrayList<>();
        try {
            FileInputStream fileInputStream = new FileInputStream(filePath);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

            for(int i = 0; i<numberOfDocuments; i++){
                Document doc = new Document();
                doc.docID  = objectInputStream.readInt();
                doc.docNo = objectInputStream.readUTF();
                doc.length = objectInputStream.readInt();
                documents.add(doc);
            }
            // Chiudi lo stream di input
            objectInputStream.close();
            fileInputStream.close();

            System.out.println("DocumentIndex caricato");

        } catch (IOException e) {
            e.printStackTrace();
        }
        return documents;
    }

}
