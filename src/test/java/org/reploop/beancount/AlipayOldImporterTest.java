package org.reploop.beancount;

import java.nio.file.Path;

class AlipayOldImporterTest {

    @org.junit.jupiter.api.Test
    void importCsv() throws Exception {
        AlipayImporter importer = new AlipayImporter();
        importer.importCsv(Path.of(""));
    }

    @org.junit.jupiter.api.Test
    void setter() {
    }

    @org.junit.jupiter.api.Test
    void billHandler() {
    }
}