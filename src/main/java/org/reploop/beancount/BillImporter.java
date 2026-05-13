package org.reploop.beancount;

import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

public interface BillImporter {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    Platform platform();

    default List<Transaction> importFile(Path path) throws Exception {
        if (support(path)) {
            return doImportFile(path);
        }
        return Collections.emptyList();
    }

    List<Transaction> doImportFile(Path path) throws Exception;

    boolean support(Path path);
}
