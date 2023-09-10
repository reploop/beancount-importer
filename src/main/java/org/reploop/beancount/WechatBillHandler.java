package org.reploop.beancount;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static java.util.Objects.nonNull;

public class WechatBillHandler extends BillHandler<WechatRecord> {

    public WechatBillHandler(List<WechatRecord> records, List<String> headers, Map<Integer, BiConsumer<WechatRecord, String>> setters) {
        super(records, headers, setters);
    }

    @Override
    protected WechatRecord newInstance() {
        return new WechatRecord();
    }

    @Override
    protected boolean validate(WechatRecord wechatRecord) {
        return nonNull(wechatRecord.getCreatedAt());
    }
}
