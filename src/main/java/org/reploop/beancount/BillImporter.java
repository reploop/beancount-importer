package org.reploop.beancount;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.csv.TextAndCSVParser;
import org.reploop.beancount.entity.BillRecord;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public abstract class BillImporter {

    public abstract void process(Path path) throws Exception;

    abstract boolean support(Source source);

    public List<BillRecord> importCsv(Path path) throws Exception {
        List<BillRecord> records = new ArrayList<>();
        TextAndCSVParser parser = new TextAndCSVParser();
        ParseContext context = new ParseContext();
        Metadata metadata = new Metadata();
        metadata.set(TextAndCSVParser.DELIMITER_PROPERTY, ",");
        metadata.set(TikaCoreProperties.CONTENT_TYPE_USER_OVERRIDE, "text/csv;delimiter=,");
        parser.parse(Files.newInputStream(path), billHandler(records), metadata, context);
        System.out.println(records.size());
        return records.stream().sorted(Comparator.comparing(org.reploop.beancount.entity.BillRecord::getCreatedAt)).toList();
    }


    abstract BillHandler<BillRecord> billHandler(List<BillRecord> records);
}
