package org.reploop.beancount;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import static java.util.Objects.nonNull;

public class AlipayBillHandler extends BillHandler<BillRecord> {

    public AlipayBillHandler(List<BillRecord> records, Set<Header> headers, Map<Header, BiConsumer<BillRecord, String>> setters) {
        super(records, headers, setters);
    }

    @Override
    protected BillRecord newInstance() {
        return new BillRecord();
    }

    @Override
    protected boolean validate(BillRecord record) {
        return nonNull(record.getCreatedAt());
    }
}
