package it.unipi.dii.mircv.index.structures;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
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

    public Document(int docID,String docNo, int length){
        this.docID = docID;
        this.docNo = docNo;
        this.length = length;
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

    public String getDocNo() {
        return docNo;
    }

    public int getLength() {
        return length;
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

    public static void saveDocumentsToDisk(ArrayList<Document> docs) {
        String filePath = "data/index/documents/documents.bin";

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(filePath, true);
            FileChannel fileChannel = fileOutputStream.getChannel();

            // Creare un buffer ByteBuffer per migliorare le prestazioni di scrittura
            ByteBuffer buffer = ByteBuffer.allocate(1024);

            for (Document doc : docs) {
                buffer.putInt(doc.getDocID());

                String docNo = doc.getDocNo();
                byte[] docNoBytes = docNo.getBytes(StandardCharsets.UTF_8);
                if (docNoBytes.length > 50) {
                    throw new IllegalArgumentException("Document number exceeds maximum length.");
                }

                // Scrivi la stringa come array di byte, riempita o tagliata per adattarsi alla dimensione fissa
                byte[] paddedDocNoBytes = new byte[50];
                System.arraycopy(docNoBytes, 0, paddedDocNoBytes, 0, docNoBytes.length);
                buffer.put(paddedDocNoBytes);

                buffer.putInt(doc.getLength());

                // Se il buffer è pieno, scrivi il suo contenuto sul file
                if (!buffer.hasRemaining()) {
                    buffer.flip();
                    fileChannel.write(buffer);
                    buffer.clear();
                }
            }

            // Scrivi eventuali dati rimanenti nel buffer sul file
            if (buffer.position() > 0) {
                buffer.flip();
                fileChannel.write(buffer);
            }

            // Chiudi le risorse
            fileChannel.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public static ArrayList<Document> readDocuments() {
        String filePath = "data/index/documents/documents.bin";
        ArrayList<Document> documents = new ArrayList<>();

        try {
            FileInputStream fileInputStream = new FileInputStream(filePath);
            FileChannel fileChannel = fileInputStream.getChannel();

            // Creare un buffer ByteBuffer per migliorare le prestazioni di lettura
            ByteBuffer buffer = ByteBuffer.allocate(1024);

            // Leggi i dati dal file in blocchi di 1024 byte
            while (fileChannel.read(buffer) > 0) {
                buffer.flip();

                // Leggi i dati dal buffer
                while (buffer.hasRemaining()) {
                    Document doc = new Document();
                    doc.docID = buffer.getInt();

                    byte[] docNoBytes = new byte[50];
                    buffer.get(docNoBytes);
                    doc.docNo = new String(docNoBytes, StandardCharsets.UTF_8).trim();

                    doc.length = buffer.getInt();

                    documents.add(doc);
                }

                buffer.clear();
            }

            // Chiudi le risorse
            fileChannel.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return documents;
    }

}
