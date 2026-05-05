package org.reploop.beancount.wechat;

import org.reploop.beancount.BillHandler;
import org.reploop.beancount.CsvBillImporter;
import org.reploop.beancount.Type;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class WechatCsvImporter extends WechatImporter implements CsvBillImporter<WechatRecord> {

    @Override
    public BiConsumer<WechatRecord, String> setter(int idx, String name) {
        return switch (idx) {
            case 0 -> (record, s) -> {
                try {
                    record.setCreatedAt(LocalDateTime.parse(s, formatter));
                } catch (DateTimeParseException ignored) {
                }
            };
            case 1 -> WechatRecord::setCategory;
            case 2 -> WechatRecord::setPeer;
            case 3 -> WechatRecord::setGoods;
            case 4 -> (record, text) -> record.setType(Type.textOf(text));
            case 5 -> (record, text) -> record.setAmount(new BigDecimal(text.substring(1)));
            case 6 -> WechatRecord::setMethod;
            case 7 -> (record, text) -> {
                if ("已存入零钱".equals(text) && Type.INCOME == record.getType() && "/".equals(record.getMethod())) {
                    record.setMethod("零钱");
                }
                record.setStatus(text);
            };
            case 8 -> WechatRecord::setOrder;
            case 9 -> WechatRecord::setMerchantOrder;
            case 10 -> WechatRecord::setComment;
            default -> throw new IllegalStateException();
        };
    }

    @Override
    public BillHandler<WechatRecord> billHandler(List<WechatRecord> records, List<String> headers, Map<Integer, BiConsumer<WechatRecord, String>> setters) {
        return new WechatBillHandler(records, headers, setters);
    }

    @Override
    public void doImportFile(Path path) throws Exception {
        var headers = Arrays.stream("交易时间\t交易类型\t交易对方\t商品\t收/支\t金额(元)\t支付方式\t当前状态\t交易单号\t商户单号\t备注".split("\\s+")).toList();
        var records = importCsv(headers, path);
        var transactions = convert(records, new HashMap<>());
    }

    @Override
    boolean supportExtension(String filename) {
        return filename.endsWith(getFileExtension());
    }
}
