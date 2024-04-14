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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

public abstract class BillImporter<R extends BillRecord> {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    abstract BiConsumer<R, String> setter(Header name);

    public List<R> importCsv(Set<Header> headers, Path path) throws Exception {
        Map<Header, BiConsumer<R, String>> setters = new HashMap<>();
        for (var header : headers) {
            BiConsumer<R, String> consumer = setter(header);
            setters.put(header, consumer);
        }
        List<R> records = new ArrayList<>();
        AutoDetectParser parser = new AutoDetectParser();
        ParseContext context = new ParseContext();
        Metadata metadata = new Metadata();
        metadata.set(TikaCoreProperties.CONTENT_TYPE_PARSER_OVERRIDE, MediaType.text("csv").toString());
        parser.parse(Files.newInputStream(path), billHandler(records, headers, setters), metadata, context);
        System.out.println(records.size());
        return records.stream().sorted(Comparator.comparing(BillRecord::getCreatedAt)).toList();
    }

    Type parse(String text) {
        return switch (text) {
            case "不计收支", "收入" -> Type.INGRESS;
            case "支出" -> Type.EGRESS;
            default -> throw new IllegalStateException(text);
        };
    }

    abstract BillHandler<R> billHandler(List<R> records, Set<Header> headers, Map<Header, BiConsumer<R, String>> setters);
}
