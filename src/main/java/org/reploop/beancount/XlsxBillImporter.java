package org.reploop.beancount;

import java.nio.file.Path;
import java.util.List;

public interface XlsxBillImporter<R> extends BillImporter {
    List<R> importXlsx(Path path) throws Exception;

    default String getFileExtension() {
        return ".xlsx";
    }
}
