package org.reploop.beancount.meituan;

import org.reploop.beancount.BillHandler;
import org.reploop.beancount.CsvBillImporter;
import org.reploop.beancount.Type;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class MeiTuanCsvImporter implements CsvBillImporter<MeiTuanRecord> {
    private final MeiTuanRecordConverter converter = new MeiTuanRecordConverter();

    @Override
    public BiConsumer<MeiTuanRecord, String> setter(int idx, String name) {
        return switch (idx) {
            case 0 -> (mtr, s) -> mtr.setCreateTime(LocalDateTime.parse(s, formatter));
            case 1 -> (mtr, s) -> mtr.setSuccessTime(LocalDateTime.parse(s, formatter));
            case 2 -> MeiTuanRecord::setCategory;
            case 3 -> MeiTuanRecord::setGoods;
            case 4 -> (mtr, s) -> mtr.setType(Type.textOf(s));
            case 5 -> MeiTuanRecord::setMethod;
            case 6 -> (mtr, s) -> mtr.setAmount(new BigDecimal(s));
            case 7 -> (mtr, s) -> mtr.setActualAmount(new BigDecimal(s));
            case 8 -> MeiTuanRecord::setOrderNo;
            case 9 -> MeiTuanRecord::setMerchantOrderNo;
            case 10 -> MeiTuanRecord::setRemarks;
            default -> throw new IllegalStateException("Unexpected value: " + idx);
        };
    }

    @Override
    public BillHandler<MeiTuanRecord> billHandler(List<MeiTuanRecord> records, List<String> headers, Map<Integer, BiConsumer<MeiTuanRecord, String>> setters) {
        return new MeiTuanBillHandler(records, headers, setters);
    }

    @Override
    public void doImportFile(Path path) throws Exception {
        var headers = Arrays.stream("交易创建时间\t交易成功时间\t交易类型\t订单标题\t收/支\t支付方式\t订单金额\t实付金额\t交易单号\t商家单号\t备注".split("\t")).toList();
        var records = importCsv(headers, path);
        converter.convert(records, new HashMap<>());
    }

    @Override
    public boolean support(Path path) {
        var filename = path.getFileName().toString();
        return filename.startsWith("美团账单") && filename.endsWith(getFileExtension());
    }
}
