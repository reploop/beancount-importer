package org.reploop.beancount;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public interface RecordConverter<R extends BillRecord> {
    List<Transaction> convert(List<R> records, Map<ReverseKey, ReverseContext> reverseTransactions) throws Exception;

    default List<String> segment(String val) {
        var values = val.split("[\\s-_&（）·:，|()【】\\[\\]]+");
        return Arrays.stream(values).map(String::trim).sorted((o1, o2) -> Integer.compare(o2.length(), o1.length())).toList();
    }
}
