package org.reploop.beancount;

import org.reploop.beancount.entity.BillRecord;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.function.BiConsumer;

import static java.util.Objects.nonNull;

public class AlipayBillHandler extends BillHandler<BillRecord> {

    public AlipayBillHandler(List<BillRecord> records) {
        super(records);
    }

    @Override
    protected BillRecord newInstance() {
        return new BillRecord();
    }

    @Override
    protected BiConsumer<BillRecord, String> getSetter(Header name) {
        return switch (name) {
            case CREATED_AT -> (record, s) -> {
                try {
                    record.setCreatedAt(LocalDateTime.parse(s, formatter));
                } catch (DateTimeParseException ignored) {
                }
            };
            case CATEGORY -> BillRecord::setCategory;
            case PEER -> BillRecord::setPeer;
            case PEER_ACCOUNT -> BillRecord::setPeerAccount;
            case GOODS -> BillRecord::setGoods;
            case TYPE -> (record, text) -> record.setType(parse(text));
            case AMOUNT -> (record, text) -> record.setAmount(new BigDecimal(text));
            case METHOD -> BillRecord::setMethod;
            case STATUS -> BillRecord::setStatus;
            case ORDER -> BillRecord::setOrder;
            case MERCHANT_ORDER -> BillRecord::setMerchantOrder;
            case COMMENT -> BillRecord::setComment;
        };
    }

    @Override
    protected boolean validate(BillRecord record) {
        return nonNull(record.getCreatedAt());
    }
}
