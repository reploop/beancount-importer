package org.reploop.beancount;

import org.apache.commons.lang3.StringUtils;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public interface CsvBillImporter<R> extends BillImporter {
    default String getFileExtension() {
        return ".csv";
    }

    BiConsumer<R, String> setter(int idx, String name);

    default List<R> importCsv(List<String> headers, Path path) throws Exception {
        Map<Integer, BiConsumer<R, String>> setters = new HashMap<>();
        int idx = 0;
        for (String header : headers) {
            var columnIndex = idx;
            BiConsumer<R, String> consumer = (r, s) -> {
                try {
                    setter(columnIndex, header).accept(r, StringUtils.trim(s));
                } catch (Exception ignored) {
                }
            };
            setters.put(idx, consumer);
            idx++;
        }
        List<R> records = new ArrayList<>();
        AutoDetectParser parser = new AutoDetectParser();
        ParseContext context = new ParseContext();
        Metadata metadata = new Metadata();

        var mediaType = "text/csv; delimiter=,";
        metadata.set(TikaCoreProperties.CONTENT_TYPE_USER_OVERRIDE, mediaType);
        parser.parse(Files.newInputStream(path), billHandler(records, headers, setters), metadata, context);
        return records;
    }

    BillHandler<R> billHandler(List<R> records, List<String> headers, Map<Integer, BiConsumer<R, String>> setters);

}
