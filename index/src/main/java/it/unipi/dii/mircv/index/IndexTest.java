package it.unipi.dii.mircv.index;

import it.unipi.dii.mircv.index.algorithms.Merger;
import it.unipi.dii.mircv.index.algorithms.Spimi;
import it.unipi.dii.mircv.index.structures.*;

import java.io.IOException;

public class IndexTest {
    private static final String COLLECTION_PATH = "data/collection/collectionTest.tsv";
    private static final String COMPRESSED_COLLECTION_PATH = "data/collection/collection.tar.gz";
    private static final String INDEX_PATH = "data/index";
    private static final String LEXICON_PATH = "data/index/lexicon.bin";
    private static final String DOCUMENTS_PATH = "data/index/documents.bin";

    public static void main(String[] args) throws IOException {
        Index index = new Index();
        Spimi spimi = new Spimi(COLLECTION_PATH);
        spimi.execute();
//          System.out.println(spimi.getIndexCounter());
        new Merger(INDEX_PATH, spimi.getIndexCounter()).execute();

         index.getLexicon().readLexiconFromDisk(-1,LEXICON_PATH);
        // per ogni chiave del lexicon, leggi il posting list dal file
        for (String key : index.getLexicon().getLexicon().keySet()) {
            //get lexicon elem
            LexiconElem lexiconElem = index.getLexicon().getLexiconElem(key);
            BlockDescriptor bd= BlockDescriptor.readFirstBlock(lexiconElem.getOffset(),INDEX_PATH + "/blockDescriptor.bin");
            PostingList postingList = new PostingList();
            postingList.readPostingList(-1, bd.getNumPosting(), bd.getPostingListOffset(),INDEX_PATH + "/index.bin");
            System.out.println(key);
            System.out.println(lexiconElem);
            System.out.println(postingList);
        }
        index.setDocuments(Document.readDocumentsFromDisk(-1,DOCUMENTS_PATH));
        System.out.println(index.getDocuments());

    }

}

//0    [bomb,bomb, ciao, civil, prova, war]
//1    [intellij, retriv, text]
//2    [engin, search]
//3    [artifici, engin, intellig]
//4    [deep, deep, learn, multimedia]
//5    [data, learn, machin, mine]
//6    [mine, process]
//7    [internet]
//8    [databas, larg, multistructur, scale]
//9    [databas, nosql, sql]

// 0 6
// 1 3
// 2 2
// 3 3
// 4 4
// 5 4
// 6 2
// 7 1
// 8 4
// 9 3 <---- perso

//bomb 0,2
//ciao 0,1
//civil 0,1
//prova 0,1
//war 0,1
//intellij 1,1
//retriv 1,1
//text 1,1
//engin 2,1 3,1
//search 2,1
//artifici 3,1
//intellig 3,1
//deep 4,2
//learn 4,1 5,1
//multimedia 4,1
//data 5,1
//machin 5,1
//mine 5,1 6,1
//process 6,1
//internet 7,1
//databas 8,1 9,1 <--- perso
//larg 8,1
//multistructur 8,1
//scale 8,1
//nosql 9,1
//sql 9,1