package org.reploop.beancount;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class AlipayOldImporter extends BillImporter<AlipayRecord> {
    public void importCsv(Path path) throws Exception {
        var headers = Arrays.stream("收/支\t交易对方\t对方账号\t商品说明\t收/付款方式\t金额\t交易状态\t交易分类\t交易订单号\t商家订单号\t交易时间".split("\\s+")).toList();
        var records = importCsv(headers, path);
        for (var record : records) {
            //System.out.println(record);
        }
        var types = records.stream().map(AlipayRecord::getType).sorted().distinct().toList();
        System.out.println(types);
        var methods = records.stream().map(AlipayRecord::getMethod).sorted().distinct().toList();
        System.out.println(methods);
        var goods = records.stream().map(AlipayRecord::getGoods)
                .map(s -> s.replaceAll("\\d+", ""))
                .sorted().distinct().toList();
        System.out.println(goods);
        var payee = records.stream().map(AlipayRecord::getPeer)
                .sorted()
                .distinct().toList();
        System.out.println(payee);
    }

    @Override
    BiConsumer<AlipayRecord, String> setter(int idx, String name) {
        return switch (idx) {
            case 10 -> (record, s) -> {
                try {
                    record.setCreatedAt(LocalDateTime.parse(s, formatter));
                } catch (DateTimeParseException ignored) {
                }
            };
            case 7 -> AlipayRecord::setCategory;
            case 1 -> AlipayRecord::setPeer;
            case 2 -> AlipayRecord::setPeerAccount;
            case 3 -> AlipayRecord::setGoods;
            case 0 -> AlipayRecord::setType;
            case 5 -> (record, text) -> record.setAmount(new BigDecimal(text));
            case 4 -> AlipayRecord::setMethod;
            case 6 -> AlipayRecord::setStatus;
            case 8 -> AlipayRecord::setOrder;
            case 9 -> AlipayRecord::setMerchantOrder;
            case 11 -> AlipayRecord::setComment;
            default -> throw new IllegalStateException();
        };
    }

    @Override
    BillHandler<AlipayRecord> billHandler(List<AlipayRecord> records, List<String> headers, Map<Integer, BiConsumer<AlipayRecord, String>> setters) {
        return new AlipayBillHandler(records, headers, setters);
    }
}
