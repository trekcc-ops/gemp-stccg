package com.gempukku.stccg;

import com.gempukku.stccg.game.SampleGameLibrary;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SampleGameLibraryTest extends AbstractServerTest {

    @Test
    public void createLibraryTest() {
        SampleGameLibrary library = new SampleGameLibrary(_cardLibrary, _formatLibrary);
        assertTrue(library.loadedSuccessfully());
    }
}