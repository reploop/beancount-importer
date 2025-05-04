package org.reploop.beancount;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.csv.TextAndCSVParser;
import org.reploop.beancount.account.AccountMapping;
import org.reploop.beancount.account.AccountType;
import org.reploop.beancount.entity.BillRecord;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Objects.nonNull;

public abstract class BillImporter {
    public static final String REFUND = "退款";
    public static final String DATE_KEY = "date";
    public static final String TIME_KEY = "time";
    public static final String STATUS_KEY = "status";

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

    protected BigDecimal reverse(BigDecimal amount, BigDecimal refund) {
        var sig = BigDecimal.valueOf(amount.signum()).negate();
        return amount.add(refund.multiply(sig)).subtract(amount);
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

    protected String first(String val) {
        return val.split("-")[0];
    }


    abstract BillHandler<BillRecord> billHandler(List<BillRecord> records);
}
