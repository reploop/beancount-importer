package org.reploop.beancount;

import org.reploop.beancount.entity.BillRecord;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.function.BiConsumer;

import static java.util.Objects.nonNull;

public class WechatBillHandler extends BillHandler<BillRecord> {

    public WechatBillHandler(List<BillRecord> records) {
        super(records);
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
            case GOODS -> BillRecord::setGoods;
            case TYPE -> (record, text) -> record.setType(parse(text));
            case AMOUNT -> (record, text) -> record.setAmount(new BigDecimal(text.substring(1)));
            case METHOD -> BillRecord::setMethod;
            case STATUS -> (record, text) -> {
                if ("已存入零钱".equals(text) && Type.INGRESS == record.getType() && "/".equals(record.getMethod())) {
                    record.setMethod("零钱");
                }
                record.setStatus(text);
            };
            case ORDER -> BillRecord::setOrder;
            case MERCHANT_ORDER -> BillRecord::setMerchantOrder;
            case COMMENT -> BillRecord::setComment;
            default -> throw new IllegalStateException();
        };
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
