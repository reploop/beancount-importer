package org.reploop.beancount.alipay;

import lombok.extern.slf4j.Slf4j;
import org.reploop.beancount.BillHandler;
import org.reploop.beancount.BillImporter;
import org.reploop.beancount.CsvBillImporter;
import org.reploop.beancount.LocalDateTimeUtils;
import org.reploop.beancount.Platform;
import org.reploop.beancount.Transaction;
import org.reploop.beancount.Type;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

@Slf4j
public class AlipayImporter implements BillImporter, CsvBillImporter<AlipayRecord> {
    private final AlipayRecordConverter converter = new AlipayRecordConverter();

    public List<Transaction> importCsv(Path path) throws Exception {
        //交易时间	交易分类	交易对方	对方账号	商品说明	收/支	金额	收/付款方式	交易状态	交易订单号	商家订单号	备注
        var headers = Arrays.stream("交易时间	交易分类	交易对方	对方账号	商品说明	收/支	金额	收/付款方式	交易状态	交易订单号	商家订单号	备注".split("\\s+")).toList();
        var records = importCsv(headers, path);
        return converter.convert(records, new HashMap<>());
    }

    @Override
    public BiConsumer<AlipayRecord, String> setter(int idx, String name) {
        return switch (idx) {
            case 0 -> (record, s) -> record.setCreatedAt(LocalDateTimeUtils.parseQuietly(s, formatter));
            case 1 -> AlipayRecord::setCategory;
            case 2 -> AlipayRecord::setPeer;
            case 3 -> AlipayRecord::setPeerAccount;
            case 4 -> AlipayRecord::setGoods;
            case 5 -> (r, s) -> r.setType(Type.textOf(s));
            case 6 -> (record, text) -> record.setAmount(new BigDecimal(text));
            case 7 -> AlipayRecord::setMethod;
            case 8 -> AlipayRecord::setStatus;
            case 9 -> AlipayRecord::setOrder;
            case 10 -> AlipayRecord::setMerchantOrder;
            case 11 -> AlipayRecord::setRemarks;
            default -> throw new IllegalStateException();
        };
    }

    @Override
    public Platform platform() {
        return Platform.ALIPAY;
    }

    @Override
    public List<Transaction> doImportFile(Path path) throws Exception {
        log.info("Importing {}", path);
        return importCsv(path);
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
