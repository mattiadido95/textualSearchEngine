package it.unipi.dii.mircv.index.structures;

/**
 * This class represents an element in the lexicon.
 */
public class LexiconElem {
    private int df; // document frequency, is the number of documents containing the term
    private long cf; // collection frequency, is the number of occurrences of the term in the entire collection
    private long offset; // offset of posting list in SPIMI / offset of first block descriptor in MERGER
    private int numBlock; // number of blocks
    private double TUB_bm25; // term upper bound for bm25
    private double TUB_tfidf; // term upper bound for tfidf

    /**
     * Default constructor to initialize LexiconElem.
     */
    public LexiconElem() {
        this.cf = 0;
        this.df = 0;
    }

    /**
     * Constructor to create LexiconElem with specified values.
     *
     * @param df        Document frequency.
     * @param cf        Collection frequency.
     * @param offset    Offset of posting list or block descriptor.
     * @param numBlock  Number of blocks.
     * @param TUB_bm25  Term upper bound for BM25.
     * @param TUB_tfidf Term upper bound for TF-IDF.
     */
    public LexiconElem(int df, long cf, long offset, int numBlock, double TUB_bm25, double TUB_tfidf) {
        this.df = df;
        this.cf = cf;
        this.offset = offset;
        this.numBlock = numBlock;
        this.TUB_bm25 = TUB_bm25;
        this.TUB_tfidf = TUB_tfidf;
    }

    /**
     * Set the number of blocks for this term.
     *
     * @param numBlock Number of blocks.
     */
    public void setNumBlock(int numBlock) {
        this.numBlock = numBlock;
    }

    /**
     * Get the document frequency (df) for this term.
     *
     * @return Document frequency.
     */
    public int getDf() {
        return this.df;
    }

    /**
     * Get the collection frequency (cf) for this term.
     *
     * @return Collection frequency.
     */
    public long getCf() {
        return this.cf;
    }

    /**
     * Get the offset of the posting list or block descriptor for this term.
     *
     * @return Offset value.
     */
    public long getOffset() {
        return this.offset;
    }

    /**
     * Set the offset of the posting list or block descriptor for this term.
     *
     * @param offset Offset value to set.
     */
    public void setOffset(long offset) {
        this.offset = offset;
    }

    /**
     * Set the document frequency (df) for this term to a new value.
     *
     * @param newDf New document frequency value.
     */
    public void setDf(int newDf) {
        this.df = newDf;
    }

    /**
     * Increment the collection frequency (cf) for this term by 1.
     */
    public void incrementCf() {
        this.cf += 1;
    }

    /**
     * Get the term upper bound for BM25.
     *
     * @return TUB_bm25 value.
     */
    public double getTUB_bm25() {
        return TUB_bm25;
    }

    /**
     * Set the term upper bound for BM25.
     *
     * @param TUB_bm25 TUB_bm25 value to set.
     */
    public void setTUB_bm25(double TUB_bm25) {
        this.TUB_bm25 = TUB_bm25;
    }

    /**
     * Get the term upper bound for TF-IDF.
     *
     * @return TUB_tfidf value.
     */
    public double getTUB_tfidf() {
        return TUB_tfidf;
    }

    /**
     * Set the term upper bound for TF-IDF.
     *
     * @param TUB_tfidf TUB_tfidf value to set.
     */
    public void setTUB_tfidf(double TUB_tfidf) {
        this.TUB_tfidf = TUB_tfidf;
    }

    /**
     * Merge the values of another LexiconElem into this one (used during merging).
     *
     * @param lexiconElem LexiconElem to merge with.
     */
    public void mergeLexiconElem(LexiconElem lexiconElem) {
        this.df += lexiconElem.getDf();
        this.cf += lexiconElem.getCf();
    }

    /**
     * Override toString to represent the LexiconElem object as a string.
     *
     * @return A string representation of the LexiconElem object.
     */
    @Override
    public String toString() {
        return "LexiconElem{" +
                "df=" + df +
                ", cf=" + cf +
                ", offset=" + offset +
                ", numBlock=" + numBlock +
                ", TUB_bm25=" + TUB_bm25 +
                ", TUB_tfidf=" + TUB_tfidf +
                '}';
    }

    /**
     * Compare two LexiconElem objects based on their TUB_bm25 values.
     *
     * @param lexiconElem LexiconElem to compare with.
     * @return 1 if this.TUB_bm25 > lexiconElem.TUB_bm25, -1 if this.TUB_bm25 < lexiconElem.TUB_bm25, and 0 if they are equal.
     */
    public int compareBM25(LexiconElem lexiconElem) {
        if (this.TUB_bm25 > lexiconElem.getTUB_bm25()) {
            return 1;
        } else if (this.TUB_bm25 < lexiconElem.getTUB_bm25()) {
            return -1;
        } else {
            return 0;
        }
    }

    /**
     * Compare two LexiconElem objects based on their TUB_tfidf values.
     *
     * @param lexiconElem LexiconElem to compare with.
     * @return 1 if this.TUB_tfidf > lexiconElem.TUB_tfidf, -1 if this.TUB_tfidf < lexiconElem.TUB_tfidf, and 0 if they are equal.
     */
    public int compareTFIDF(LexiconElem lexiconElem) {
        if (this.TUB_tfidf > lexiconElem.getTUB_tfidf()) {
            return 1;
        } else if (this.TUB_tfidf < lexiconElem.getTUB_tfidf()) {
            return -1;
        } else {
            return 0;
        }
    }

    /**
     * Get the number of blocks for this term.
     *
     * @return Number of blocks.
     */
    public Integer getBlocksNumber() {
        return this.numBlock;
    }
}
