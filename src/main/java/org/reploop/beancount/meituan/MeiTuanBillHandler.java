package org.reploop.beancount.meituan;

import org.reploop.beancount.BillHandler;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static java.util.Objects.nonNull;

public class MeiTuanBillHandler extends BillHandler<MeiTuanRecord> {
    public MeiTuanBillHandler(List<MeiTuanRecord> records, List<String> headers, Map<Integer, BiConsumer<MeiTuanRecord, String>> setters) {
        super(records, headers, setters);
    }

    @Override
    protected MeiTuanRecord newInstance() {
        return new MeiTuanRecord();
    }

    @Override
    protected boolean validate(MeiTuanRecord meiTuanRecord) {
        return super.validate(meiTuanRecord) && nonNull(meiTuanRecord.getCreateTime());
    }
}
