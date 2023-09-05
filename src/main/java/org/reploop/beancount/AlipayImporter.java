package org.reploop.beancount;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class AlipayImporter extends BillImporter<AlipayRecord> {

    public static void main(String... args) throws Exception {
        AlipayImporter importer = new AlipayImporter();
    }

    public void importCsv(Path path) throws Exception {
        var headers = Arrays.stream("交易时间	交易分类	交易对方	对方账号	商品说明	收/支	金额	收/付款方式	交易状态	交易订单号	商家订单号	备注".split("\\s+")).toList();
        var records = super.importCsv(headers, path);
        for (var record : records) {
            System.out.println(record);
        }

        var types = records.stream().collect(Collectors.groupingBy(AlipayRecord::getStatus));
        ;
        System.out.println(types);
    }

    @Override
    BiConsumer<AlipayRecord, String> setter(int idx, String name) {
        return switch (idx) {
            case 0 -> (record, s) -> {
                try {
                    record.setCreatedAt(LocalDateTime.parse(s, formatter));
                } catch (DateTimeParseException ignored) {
                }
            };
            case 1 -> AlipayRecord::setCategory;
            case 2 -> AlipayRecord::setPeer;
            case 3 -> AlipayRecord::setPeerAccount;
            case 4 -> AlipayRecord::setGoods;
            case 5 -> AlipayRecord::setType;
            case 6 -> (record, text) -> record.setAmount(new BigDecimal(text));
            case 7 -> AlipayRecord::setMethod;
            case 8 -> AlipayRecord::setStatus;
            case 9 -> AlipayRecord::setOrder;
            case 10 -> AlipayRecord::setMerchantOrder;
            case 11 -> AlipayRecord::setComment;
            default -> throw new IllegalStateException();
        };
    }

    @Override
    BillHandler<AlipayRecord> billHandler(List<AlipayRecord> records, List<String> headers, Map<Integer, BiConsumer<AlipayRecord, String>> setters) {
        return new AlipayBillHandler(records, headers, setters);
    }
}
