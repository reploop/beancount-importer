package org.reploop.beancount;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static java.util.Objects.nonNull;

public interface RecordConverter<R extends BillRecord> {
    List<Transaction> convert(List<R> records, Map<ReverseKey, ReverseContext> reverseTransactions);

    default List<String> segment(String val) {
        return segment(val, Comparator.comparing(String::length).reversed());
    }

    default List<String> segment(String val, Comparator<String> cmp) {
        var values = val.split("[\\s-_&（）·:，|()【】\\[\\]•]+");
        var s = Arrays.stream(values).map(String::trim);
        if (nonNull(cmp)) {
            return s.sorted(cmp).toList();
        } else {
            return s.toList();
        }
    }
}
