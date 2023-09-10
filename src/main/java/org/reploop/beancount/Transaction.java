package org.reploop.beancount;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.Objects.nonNull;

@Data
@Builder
public class Transaction {
    LocalDateTime dateTime;
    Flag flag;
    String payee;
    String narration;
    @Builder.Default
    Map<String, Object> meta = new HashMap<>();
    List<Posting> postings;

    private static final String template = """
            %s %s %s %s
                
            ;""";

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private <T> StringBuilder append(StringBuilder sb, T val) {
        return append(sb, val, Function.identity());
    }

    private static final String WS = " ";

    private <T> void indent(StringBuilder sb, int size) {
        sb.append(WS.repeat(Math.max(0, size)));
    }

    private <T, R> StringBuilder append(StringBuilder sb, T val, Function<T, R> func) {
        if (!sb.isEmpty()) {
            indent(sb, 1);
        }
        return sb.append(func.apply(val));
    }

    private void newLine(StringBuilder sb) {
        sb.append(System.lineSeparator());
    }

    private static Function<String, String> quote = s -> "\"" + s + "\"";

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        append(sb, dateTime.format(DATE_FORMAT));
        append(sb, flag);
        append(sb, payee, quote);
        append(sb, narration, quote);
        newLine(sb);
        // meta
        if (nonNull(meta)) {
            meta.forEach((name, value) -> {
                indent(sb, 2);
                sb.append(name);
                append(sb, ":");
                if (value instanceof LocalDate ld) {
                    append(sb, ld.format(DATE_FORMAT));
                } else if (value instanceof LocalTime lt) {
                    append(sb, lt.format(TIME_FORMAT), quote);
                } else {
                    sb.append(value);
                }
                newLine(sb);
            });
        }
        // posting
        if (nonNull(postings)) {
            for (Posting posting : postings) {
                indent(sb, 2);
                sb.append(posting.account);
                append(sb, posting.amount);
                append(sb, posting.currency);
                newLine(sb);
            }
        }
        return sb.toString();
    }
}
