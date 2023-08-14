package it.unipi.dii.mircv.index.structures;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import it.unipi.dii.mircv.index.structures.Document;
import it.unipi.dii.mircv.index.structures.Posting;
import it.unipi.dii.mircv.index.structures.PostingList;

// TODO test not working

public class PostingListTest {

    @Test
    public void testPostingListConstructorAndPrint() {
        Document document = new Document("doc123    This is a test document.");
        PostingList postingList = new PostingList("test", document);

        assertNotNull(postingList);

        // Create a StringBuilder to capture the output of printPostingList()
        StringBuilder output = new StringBuilder();
        System.setOut(new java.io.PrintStream(new java.io.ByteArrayOutputStream() {
            public void write(byte[] buf, int off, int len) {
                output.append(new String(buf, off, len));
            }
        }));

        postingList.printPostingList();

        // Verify that the printed output matches the expected format
        String expectedOutput = "Term: test\nDocID: doc123 Freq: 1\n";
        assertEquals(expectedOutput, output.toString());
    }
}
