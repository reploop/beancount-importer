package org.reploop.beancount;

import java.nio.file.Path;
import java.time.format.DateTimeFormatter;

public interface BillImporter {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    default void importFile(Path path) throws Exception {
        if (support(path)) {
            doImportFile(path);
        }
    }

    void doImportFile(Path path) throws Exception;

    boolean support(Path path);
}
