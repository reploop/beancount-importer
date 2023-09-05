package org.reploop.beancount;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public abstract class BillImporter<R> {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    abstract BiConsumer<R, String> setter(int idx, String name);

    public List<R> importCsv(List<String> headers, Path path) throws Exception {
        Map<Integer, BiConsumer<R, String>> setters = new HashMap<>();
        int idx = 0;
        for (String header : headers) {
            BiConsumer<R, String> consumer = setter(idx, header);
            setters.put(idx, consumer);
            idx++;
        }
        List<R> records = new ArrayList<>();
        AutoDetectParser parser = new AutoDetectParser();
        ParseContext context = new ParseContext();
        Metadata metadata = new Metadata();
        metadata.set(TikaCoreProperties.CONTENT_TYPE_PARSER_OVERRIDE, MediaType.text("csv").toString());
        parser.parse(Files.newInputStream(path), billHandler(records, headers, setters), metadata, context);
        System.out.println(records.size());
        return records;
    }

    abstract BillHandler<R> billHandler(List<R> records, List<String> headers, Map<Integer, BiConsumer<R, String>> setters);
}
