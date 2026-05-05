package org.reploop.beancount.alipay;

import org.reploop.beancount.BillHandler;
import org.reploop.beancount.BillImporter;
import org.reploop.beancount.CsvBillImporter;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class AlipayImporter implements BillImporter, CsvBillImporter<AlipayRecord> {

    public void importCsv(Path path) throws Exception {
        //交易时间	交易分类	交易对方	对方账号	商品说明	收/支	金额	收/付款方式	交易状态	交易订单号	商家订单号	备注
        var headers = Arrays.stream("交易时间	交易分类	交易对方	对方账号	商品说明	收/支	金额	收/付款方式	交易状态	交易订单号	商家订单号	备注".split("\\s+")).toList();
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
    public BiConsumer<AlipayRecord, String> setter(int idx, String name) {
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
    public void doImportFile(Path path) throws Exception {
        importCsv(path);
    }

    @Override
    public boolean support(Path path) {
        var filename = path.getFileName().toString();
        return (filename.startsWith("alipay_") || filename.startsWith("支付宝交易明细")) && filename.endsWith(".csv");
    }

    @Override
    public BillHandler<AlipayRecord> billHandler(List<AlipayRecord> records, List<String> headers, Map<Integer, BiConsumer<AlipayRecord, String>> setters) {
        // Will fall back to older version
        return new AlipayBillHandler(records, headers, setters);
    }
}
