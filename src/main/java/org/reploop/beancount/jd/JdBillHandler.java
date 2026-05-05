package org.reploop.beancount.jd;

import org.reploop.beancount.BillHandler;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static java.util.Objects.nonNull;

public class JdBillHandler extends BillHandler<JdRecord> {
    public JdBillHandler(List<JdRecord> records, List<String> headers, Map<Integer, BiConsumer<JdRecord, String>> setters) {
        super(records, headers, setters);
    }

    @Override
    protected JdRecord newInstance() {
        return new JdRecord();
    }

    @Override
    protected boolean validate(JdRecord jdRecord) {
        return super.validate(jdRecord) && nonNull(jdRecord.getTransactionTime());
    }
}
