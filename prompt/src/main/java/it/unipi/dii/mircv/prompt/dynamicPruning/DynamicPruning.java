package it.unipi.dii.mircv.prompt.dynamicPruning;

import it.unipi.dii.mircv.index.preprocessing.Preprocessing;
import it.unipi.dii.mircv.index.structures.Document;
import it.unipi.dii.mircv.index.structures.Lexicon;
import it.unipi.dii.mircv.index.structures.PostingList;
import it.unipi.dii.mircv.prompt.query.Searcher;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * This class is used to compute the TUB scores for each term in the lexicon.
 * The TUB score is computed using the DAAT algorithm with a single term query.
 * The score function used is the one specified in the input.
 * The lexicon is updated with the new TUB scores.
 */
public class DynamicPruning {
    private Lexicon lexicon;
    private Searcher searcher;
    private ArrayList<String> queryTerms;
    private String LEXICON_PATH = "data/index/lexicon.bin";
    private String DOCUMENTS_PATH = "data/index/documents.bin";
    private boolean compressed_reading;
    private boolean porterStemmer;
    private String COLLECTION_PATH;

    /**
     * Constructor
     *
     * @param lexicon   is the lexicon to be updated
     * @param documents are the documents used to compute the TUB scores
     *                                                                                      TODO
     */
    public DynamicPruning(Lexicon lexicon, ArrayList<Document> documents, String COLLECTION_PATH, boolean compressed_reading, boolean porterStemmer) {
        this.lexicon = lexicon;
        this.searcher = new Searcher(lexicon, documents);
        this.queryTerms = new ArrayList<>(this.lexicon.getLexiconKeys());
        this.COLLECTION_PATH = COLLECTION_PATH;
        this.compressed_reading = compressed_reading;
        this.porterStemmer = porterStemmer;
    }

    /**
     * This method computes the TUB scores for each term in the lexicon.
     * The score function used is the one specified in the input.
     * The lexicon is updated with the new TUB scores.
     */
    private void TUB_processing() {
        for (String term : this.queryTerms) {
            ArrayList<String> arrayTerm = new ArrayList<>();
            arrayTerm.add(term); // used to call the search function, made for compatibility
            this.searcher.DAAT(arrayTerm, 1, "disjunctive", "BM25");
            this.lexicon.getLexiconElem(term).setTUB_bm25(this.searcher.getQueryResults().get(0).getScoring());
            this.searcher.DAAT(arrayTerm, 1, "disjunctive", "TFIDF");
            this.lexicon.getLexiconElem(term).setTUB_tfidf(this.searcher.getQueryResults().get(0).getScoring());
        }
        // delete lexicon.bin file
        File file = new File("data/index/lexicon.bin");
        file.delete();
        // save the updated lexicon to disk
        this.lexicon.saveLexiconToDisk(-1, LEXICON_PATH);
    }

    private void DUB_processing() {
        int documentCounter = 0;
        TarArchiveInputStream tarArchiveInputStream = null;
        // open buffer to read documents
        try {
            BufferedReader br;

            if (compressed_reading) {
                tarArchiveInputStream = new TarArchiveInputStream(new GzipCompressorInputStream(new FileInputStream(COLLECTION_PATH)));
                tarArchiveInputStream.getNextEntry();
                br = new BufferedReader(new InputStreamReader(tarArchiveInputStream, "UTF-8"));
            } else {
                br = new BufferedReader(new InputStreamReader(new FileInputStream(COLLECTION_PATH), "UTF-8"));
            }

            ArrayList<Document> documents = Document.readDocumentsFromDisk(-1, DOCUMENTS_PATH);

            String line; // start reading document by document
            while ((line = br.readLine()) != null) {
                Preprocessing preprocessing = new Preprocessing(line, documentCounter, porterStemmer);
                Document currentDoc = documents.get(documentCounter);
                List<String> tokens = preprocessing.tokens; // and return a list of tokens

                double dub_bm25 = 0;
                double dub_tfidf = 0;
                for (String token : tokens) {
                    if (lexicon.getLexicon().containsKey(token)) {
                        dub_bm25 += lexicon.getLexiconElem(token).getTUB_bm25();
                        dub_tfidf += lexicon.getLexiconElem(token).getTUB_tfidf();
                    }
                }
                currentDoc.setDUB_bm25(dub_bm25);
                currentDoc.setDUB_tfidf(dub_tfidf);
                documentCounter++;
//                if(documentCounter % 100000 == 0)
//                    System.out.println("DUB scores computed for document " + documentCounter);
            }
            // save the updated documents to disk
            Document.saveDocumentsToDisk(documents, -1, DOCUMENTS_PATH);
            br.close();
            if (compressed_reading) {
                tarArchiveInputStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void execute() {
        System.out.println("Computing TUB scores...");
        this.TUB_processing();
        System.out.println("Computing DUB scores...");
        this.DUB_processing();
    }
}
