package org.reploop.beancount;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.csv.TextAndCSVParser;
import org.reploop.beancount.account.AccountMapping;
import org.reploop.beancount.account.AccountType;
import org.reploop.beancount.entity.BillRecord;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;
import static java.util.Objects.nonNull;

public abstract class BillImporter {
    public static final String REFUND = "退款";
    public static final String DATE_KEY = "date";
    public static final String TIME_KEY = "time";
    public static final String STATUS_KEY = "status";

    final Source source;
    final Path outputDirectory;

    protected BillImporter(Source source) {
        this(source, Paths.get("/Users/george/open-source/rich-book"));
    }

    protected BillImporter(Source source, Path dir) {
        this.source = source;
        this.outputDirectory = dir;
    }

    protected void output(Collection<Transaction> transactions) {
        var pathMap = outputFile(transactions);
        pathMap.forEach((path, transactions1) -> {
            try (var writer = Files.newBufferedWriter(path, UTF_8, WRITE, TRUNCATE_EXISTING, CREATE)) {
                for (var txn : transactions1) {
                    writer.write(txn.toString());
                    writer.newLine();
                }
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        });
    }

    public void process(Path path) throws Exception {
        process(List.of(path));
    }

    public abstract void process(List<Path> path) throws Exception;

    boolean support(Source source) {
        return this.source == source;
    }

    public List<BillRecord> importCsv(List<Path> paths) throws Exception {
        List<BillRecord> records = new ArrayList<>();
        for (var path : paths) {
            records.addAll(importCsv(path));
        }
        records.sort(Comparator.comparing(BillRecord::getCreatedAt));
        return records;
    }

    public List<BillRecord> importCsv(Path path) throws Exception {
        List<BillRecord> records = new ArrayList<>();
        TextAndCSVParser parser = new TextAndCSVParser();
        ParseContext context = new ParseContext();
        Metadata metadata = new Metadata();
        metadata.set(TextAndCSVParser.DELIMITER_PROPERTY, ",");
        metadata.set(TikaCoreProperties.CONTENT_TYPE_USER_OVERRIDE, "text/csv;delimiter=,");
        parser.parse(Files.newInputStream(path), billHandler(records), metadata, context);
        System.out.println(records.size());
        return records.stream().sorted(Comparator.comparing(BillRecord::getCreatedAt)).toList();
    }

    protected BigDecimal reverse(BigDecimal amount, BigDecimal refund) {
        var sig = BigDecimal.valueOf(amount.signum());
        return refund.multiply(sig).negate();
    }

    protected Optional<String> searchAccount(String... hints) {
        return searchAccount(EnumSet.allOf(AccountType.class), hints);
    }

    protected Optional<String> searchAccount(Set<AccountType> accountTypes, String... hints) {
        for (AccountType accountType : accountTypes) {
            for (var hint : hints) {
                var account = AccountMapping.account(accountType, hint);
                if (nonNull(account)) {
                    return Optional.of(account);
                }
            }
        }
        return Optional.empty();
    }

    protected static final Pattern stop = Pattern.compile("[-\\s（）()，,]+");

    protected String[] segment(String val) {
        return stop.split(val);
    }

    protected String first(String val) {
        return val.split("[-\\s（）()，,]+")[0];
    }


    Map<Path, List<Transaction>> outputFile(Collection<Transaction> transactions) {
        Map<Integer, List<Transaction>> yearMap = transactions.stream().collect(Collectors.groupingBy(t -> t.getDateTime().getYear(), Collectors.toList()));
        Map<Path, List<Transaction>> pathMap = new HashMap<>();
        yearMap.forEach((year, list) -> {
            int max = Integer.MIN_VALUE;
            int min = Integer.MAX_VALUE;
            for (var txn : list) {
                var datetime = txn.getDateTime();
                var month = datetime.getMonthValue();
                if (month > max) {
                    max = month;
                }
                if (month < min) {
                    min = month;
                }
            }
            var prefix = source.name().toLowerCase();
            var filename = String.format("%s_%02d_%02d.beancount", prefix, min, max);
            var path = outputDirectory.resolve(String.valueOf(year)).resolve(filename);
            pathMap.put(path, list);
        });
        return pathMap;
    }

    abstract BillHandler<BillRecord> billHandler(List<BillRecord> records);
}
