package org.reploop.beancount;

import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

public interface BillImporter {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    default void importFile(Path path) throws Exception {
        if (support(path)) {
            doImportFile(path);
        }
    }

    void doImportFile(Path path) throws Exception;

    boolean support(Path path);

    default List<String> segment(String val) {
        var values = val.split("[\\s-_&（）·:，|()【】\\[\\]]+");
        return Arrays.stream(values).map(String::trim).sorted((o1, o2) -> Integer.compare(o2.length(), o1.length())).toList();
    }
}
