package it.unipi.dii.mircv.index.structures;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BlockDescriptorTest {


    private BlockDescriptor blockDescriptor;

    @BeforeAll
    public void setUp() {
        // Inizializza l'oggetto BlockDescriptor qui, se necessario
        blockDescriptor = new BlockDescriptor();
    }

    @Test
    public void testBlockDescriptorSaveAndRead() {
        // Test per verificare la funzionalità di salvataggio e lettura di BlockDescriptor
        // Imposta i valori di maxDocID, numPosting e postingListOffset
        blockDescriptor.setMaxDocID(100);
        blockDescriptor.setNumPosting(50);
        blockDescriptor.setPostingListOffset(12345L);

        // Salva BlockDescriptor su disco e ottieni l'offset
        long offset = blockDescriptor.saveBlockDescriptorToDisk(true);

        // Leggi BlockDescriptor dal disco utilizzando l'offset
        BlockDescriptor readBlockDescriptor = BlockDescriptor.readFirstBlock(offset,true);

        // Verifica che i valori letti corrispondano ai valori impostati
        assertEquals(100, readBlockDescriptor.getMaxDocID());
        assertEquals(50, readBlockDescriptor.getNumPosting());
        assertEquals(12345L, readBlockDescriptor.getPostingListOffset());
    }

    // Aggiungi altri test se necessario

//    @AfterAll
//    static void removeFile() {
//        Path path = Path.of("src/test/data/blockDescriptorTest.bin");
//        path.toFile().delete();
//    }

}
