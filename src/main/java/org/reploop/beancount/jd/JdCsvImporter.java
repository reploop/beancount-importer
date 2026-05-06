package org.reploop.beancount.jd;

import org.reploop.beancount.BillHandler;
import org.reploop.beancount.CsvBillImporter;
import org.reploop.beancount.ReverseContext;
import org.reploop.beancount.ReverseKey;
import org.reploop.beancount.Type;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static org.reploop.beancount.jd.JdConstants.REVERSE_PATTERN;

public class JdCsvImporter implements CsvBillImporter<JdRecord> {
    private final JdRecordConverter converter = new JdRecordConverter();

    @Override
    public BiConsumer<JdRecord, String> setter(int idx, String name) {
        return switch (idx) {
            case 0 -> (r, s) -> r.setCreatedAt(LocalDateTime.parse(s, formatter));
            case 1 -> JdRecord::setPeer;
            case 2 -> JdRecord::setGoods;
            case 3 -> (r, s) -> {
                var m = REVERSE_PATTERN.matcher(s);
                String value = s;
                if (m.find()) {
                    r.setAmountText(s);
                    value = m.group(1);
                }
                r.setAmount(new BigDecimal(value));
            };
            case 4 -> JdRecord::setMethod;
            case 5 -> JdRecord::setStatus;
            case 6 -> (r, s) -> r.setType(Type.textOf(s));
            case 7 -> JdRecord::setCategory;
            case 8 -> JdRecord::setOrder;
            case 9 -> JdRecord::setMerchantOrder;
            case 10 -> JdRecord::setRemarks;
            default -> throw new IllegalStateException("Unexpected value: " + idx);
        };
    }

    @Override
    public BillHandler<JdRecord> billHandler(List<JdRecord> records, List<String> headers, Map<Integer, BiConsumer<JdRecord, String>> setters) {
        return new JdBillHandler(records, headers, setters);
    }

    @Override
    public void doImportFile(Path path) throws Exception {
        var headers = Arrays.stream("交易时间,商户名称,交易说明,金额,收/付款方式,交易状态,收/支,交易分类,交易订单号,商家订单号,备注".split(",")).toList();
        Map<ReverseKey, ReverseContext> reverseTxn = new HashMap<>();
        var records = importCsv(headers, path);
        converter.convert(records, reverseTxn);
    }

    @Override
    public boolean support(Path path) {
        var filename = path.getFileName().toString();
        return filename.startsWith("京东交易流水") && filename.endsWith(getFileExtension());
    }
}
