package org.reploop.beancount.alipay;

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

public class AlipayOldImporter implements CsvBillImporter<AlipayRecord> {
    private final AlipayRecordConverter converter = new AlipayRecordConverter();

    public void importCsv(Path path) throws Exception {
        var headers = Arrays.stream("收/支\t交易对方\t对方账号\t商品说明\t收/付款方式\t金额\t交易状态\t交易分类\t交易订单号\t商家订单号\t交易时间".split("\\s+")).toList();
        var records = importCsv(headers, path);
        converter.convert(records, new HashMap<>());
    }

    @Override
    public BiConsumer<AlipayRecord, String> setter(int idx, String name) {
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
            case 0 -> (r, s) -> r.setType(Type.textOf(s));
            case 5 -> (record, text) -> record.setAmount(new BigDecimal(text));
            case 4 -> AlipayRecord::setMethod;
            case 6 -> AlipayRecord::setStatus;
            case 8 -> AlipayRecord::setOrder;
            case 9 -> AlipayRecord::setMerchantOrder;
            case 11 -> AlipayRecord::setRemarks;
            default -> throw new IllegalStateException();
        };
    }

    @Override
    public boolean support(Path path) {
        var filename = path.getFileName().toString();
        return (filename.startsWith("alipay_") || filename.startsWith("支付宝")) && filename.endsWith(".csv");
    }

    @Override
    public void doImportFile(Path path) throws Exception {
        importCsv(path);
    }

    @Override
    public BillHandler<AlipayRecord> billHandler(List<AlipayRecord> records, List<String> headers, Map<Integer, BiConsumer<AlipayRecord, String>> setters) {
        return new AlipayBillHandler(records, headers, setters);
    }
}
