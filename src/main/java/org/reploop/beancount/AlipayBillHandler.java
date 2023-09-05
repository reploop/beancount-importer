package org.reploop.beancount;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static java.util.Objects.nonNull;

public class AlipayBillHandler extends BillHandler<AlipayRecord> {

    public AlipayBillHandler(List<AlipayRecord> records, List<String> headers, Map<Integer, BiConsumer<AlipayRecord, String>> setters) {
        super(records, headers, setters);
    }

    @Override
    protected AlipayRecord newIns() {
        return new AlipayRecord();
    }

    @Override
    protected boolean validate(AlipayRecord record) {
        return nonNull(record.getCreatedAt());
    }
}
