package org.reploop.beancount;

import org.reploop.beancount.entity.BillRecord;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import static java.util.Objects.nonNull;

public class WechatBillHandler extends BillHandler<BillRecord> {

    public WechatBillHandler(List<BillRecord> records, Set<Header> headers, Map<Header, BiConsumer<BillRecord, String>> setters) {
        super(records, headers, setters);
    }

    @Override
    protected BillRecord newInstance() {
        return new BillRecord();
    }

    @Override
    protected boolean validate(BillRecord wechatRecord) {
        return nonNull(wechatRecord.getCreatedAt());
    }
}
