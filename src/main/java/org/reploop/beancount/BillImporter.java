package org.reploop.beancount;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.reploop.beancount.entity.BillRecord;

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

public abstract class BillImporter {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public abstract void importCsv(Path path) throws Exception;

    abstract boolean support(Source source);

    abstract BiConsumer<BillRecord, String> setter(Header name);

    public List<BillRecord> importCsv(Set<Header> headers, Path path) throws Exception {
        Map<Header, BiConsumer<BillRecord, String>> setters = new HashMap<>();
        for (var header : headers) {
            BiConsumer<BillRecord, String> consumer = setter(header);
            setters.put(header, consumer);
        }
        List<BillRecord> records = new ArrayList<>();
        AutoDetectParser parser = new AutoDetectParser();
        ParseContext context = new ParseContext();
        Metadata metadata = new Metadata();
        metadata.set(TikaCoreProperties.CONTENT_TYPE_PARSER_OVERRIDE, MediaType.text("csv").toString());
        parser.parse(Files.newInputStream(path), billHandler(records, headers, setters), metadata, context);
        System.out.println(records.size());
        return records.stream().sorted(Comparator.comparing(org.reploop.beancount.entity.BillRecord::getCreatedAt)).toList();
    }

    Type parse(String text) {
        return switch (text) {
            case "不计收支", "收入" -> Type.INGRESS;
            case "支出" -> Type.EGRESS;
            default -> throw new IllegalStateException(text);
        };
    }

    abstract BillHandler<BillRecord> billHandler(List<BillRecord> records, Set<Header> headers, Map<Header, BiConsumer<BillRecord, String>> setters);
}
