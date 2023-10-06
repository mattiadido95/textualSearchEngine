package it.unipi.dii.mircv.index.structures;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class Document {
    private static final int DOCNO_LENGTH = 64;
    private int docID;
    private String docNo;
    private String body;
    private int length;
    private String rawDocument;
    private double DUB_tfidf;
    private double DUB_bm25;

    /**
     * Constructor to initialize a Document from raw text.
     *
     * @param rawDocument The raw text content of the document.
     * @param docID       The unique identifier for the document.
     */
    public Document(String rawDocument, int docID) {
        this.docID = docID;
        this.rawDocument = rawDocument;
        length = 0;
        parseDocument();
        DUB_tfidf = 0;
        DUB_bm25 = 0;
    }

    /**
     * Constructor to initialize a Document with specified attributes.
     *
     * @param docID  The unique identifier for the document.
     * @param docNo  The document number.
     * @param length The length of the document.
     */
    public Document(int docID, String docNo, int length) {
        this.docID = docID;
        this.docNo = docNo;
        this.length = length;
        DUB_tfidf = 0;
        DUB_bm25 = 0;
    }

    /**
     * Default constructor for a Document.
     */
    public Document() {

    }

    /**
     * Parse document by dividing it into its components: docid and body.
     */
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

    /**
     * Get the unique identifier for the document.
     *
     * @return The document ID.
     */
    public int getDocID() {
        return this.docID;
    }

    /**
     * Get the document number.
     *
     * @return The document number.
     */
    public String getDocNo() {
        return docNo;
    }

    /**
     * Get the length of the document.
     *
     * @return The length of the document.
     */
    public int getLength() {
        return length;
    }

    /**
     * Get the body content of the document.
     *
     * @return The document body.
     */
    public String getBody() {
        return this.body;
    }

    /**
     * Set the length of the document.
     *
     * @param length The new length to set.
     */
    public void setLength(int length) {
        this.length = length;
    }

    public double getDUB_tfidf() {
        return DUB_tfidf;
    }

    public void setDUB_tfidf(double DUB_tfidf) {
        this.DUB_tfidf = DUB_tfidf;
    }

    public double getDUB_bm25() {
        return DUB_bm25;
    }

    public void setDUB_bm25(double DUB_bm25) {
        this.DUB_bm25 = DUB_bm25;
    }

    /**
     * Save a list of Document objects to a binary file.
     *
     * @param docs     The list of Document objects to save.
     * @param index    The index (used for filename) or -1 if not used.
     * @param filePath The path to the binary file to save the documents.
     */
    public static void saveDocumentsToDisk(ArrayList<Document> docs, int index, String filePath) {
        if (index != -1)
            filePath += index + ".bin";
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(filePath, true);
            FileChannel fileChannel = fileOutputStream.getChannel();

            // Creare un buffer ByteBuffer per migliorare le prestazioni di scrittura
            ByteBuffer buffer = ByteBuffer.allocate(1024);

            for (Document doc : docs) {
                buffer.putInt(doc.getDocID());
                String docNo = doc.getDocNo();
                byte[] docNoBytes = docNo.getBytes(StandardCharsets.UTF_8);
                if (docNoBytes.length > DOCNO_LENGTH) {
                    throw new IllegalArgumentException("Document number exceeds maximum length.");
                }
                // Scrivi la stringa come array di byte, riempita o tagliata per adattarsi alla dimensione fissa
                byte[] paddedDocNoBytes = new byte[DOCNO_LENGTH];
                System.arraycopy(docNoBytes, 0, paddedDocNoBytes, 0, docNoBytes.length);
                buffer.put(paddedDocNoBytes);
                buffer.putInt(doc.getLength());
                buffer.putDouble(doc.getDUB_tfidf());
                buffer.putDouble(doc.getDUB_bm25());
                // Se il buffer è pieno, scrivi il suo contenuto sul file
                //if (!buffer.hasRemaining()) {
                if (buffer.remaining() < (paddedDocNoBytes.length + 4 + 4 + 8 + 8)) {
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

    /**
     * Read a list of Document objects from a binary file.
     *
     * @param index    The index (used for filename) or -1 if not used.
     * @param filePath The path to the binary file containing the documents.
     * @return An ArrayList of Document objects read from the file.
     */
    public static ArrayList<Document> readDocumentsFromDisk(int index, String filePath) {
        if (index != -1) {
            filePath += index + ".bin";
        }
        ArrayList<Document> documents = new ArrayList<>();

        try {
            FileInputStream fileInputStream = new FileInputStream(filePath);
            FileChannel fileChannel = fileInputStream.getChannel();
            // Creare un buffer ByteBuffer per migliorare le prestazioni di lettura
            ByteBuffer buffer = ByteBuffer.allocate(1024); // Usiamo un buffer di 1024 byte

            while (fileChannel.read(buffer) > 0) {
                buffer.flip();
                while (buffer.remaining() >= (DOCNO_LENGTH + 4 + 4 + 8 + 8)) {
                    Document doc = new Document();
                    doc.docID = buffer.getInt();
                    byte[] docNoBytes = new byte[DOCNO_LENGTH];
                    buffer.get(docNoBytes);
                    doc.docNo = new String(docNoBytes, StandardCharsets.UTF_8).trim();
                    doc.length = buffer.getInt();
                    doc.DUB_tfidf = buffer.getDouble();
                    doc.DUB_bm25 = buffer.getDouble();
                    documents.add(doc);
                }
                buffer.compact();
            }
            // Chiudi le risorse
            fileChannel.close();
            fileInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return documents;
    }

    /**
     * Concatenate a list of files into a single output file.
     *
     * @param fileNames      The list of file names to concatenate.
     * @param outputFileName The name of the output file.
     */
    public static void ConcatenateFiles(ArrayList<String> fileNames, String outputFileName) {
        try {
            FileOutputStream outputStream = new FileOutputStream(outputFileName);
            FileChannel outputChannel = outputStream.getChannel();

            for (String fileName : fileNames) {
                FileInputStream inputStream = new FileInputStream(fileName);
                FileChannel inputChannel = inputStream.getChannel();
                outputChannel.transferFrom(inputChannel, outputChannel.size(), inputChannel.size());
                inputChannel.close();
                inputStream.close();
            }

            outputChannel.close();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
