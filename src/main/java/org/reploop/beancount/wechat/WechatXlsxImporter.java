package org.reploop.beancount.wechat;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.reploop.beancount.Point;
import org.reploop.beancount.Type;
import org.reploop.beancount.XlsxBillImporter;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.nonNull;

public class WechatXlsxImporter extends WechatImporter implements XlsxBillImporter<WechatRecord> {

    @Override
    public List<WechatRecord> importXlsx(Path path) throws Exception {
        List<WechatRecord> records = new ArrayList<>();
        int index = 0;
        // 交易时间	交易类型	交易对方	商品	收/支	金额(元)	支付方式	当前状态	交易单号	商户单号	备注
        List<Point<Cell, WechatRecord, ?>> points = List.of(
                new Point<>(index++, "交易时间", Cell::getLocalDateTimeCellValue, WechatRecord::setCreatedAt),
                new Point<>(index++, "交易类型", Cell::getStringCellValue, WechatRecord::setCategory),
                new Point<>(index++, "交易对方", Cell::getStringCellValue, WechatRecord::setPeer),
                new Point<>(index++, "商品", Cell::getStringCellValue, WechatRecord::setGoods),
                new Point<>(index++, "收/支", Cell::getStringCellValue, (wechatRecord, s) -> wechatRecord.setType(Type.textOf(s))),
                new Point<>(index++, "金额(元)", Cell::getNumericCellValue, WechatRecord::setDoubleAmount),
                new Point<>(index++, "支付方式", Cell::getStringCellValue, WechatRecord::setMethod),
                new Point<>(index++, "当前状态", Cell::getStringCellValue, WechatRecord::setStatus),
                new Point<>(index++, "交易单号", Cell::getStringCellValue, WechatRecord::setOrder),
                new Point<>(index++, "商户单号", Cell::getStringCellValue, WechatRecord::setMerchantOrder),
                new Point<>(index++, "备注", Cell::getStringCellValue, WechatRecord::setComment)
        );
        int columns = points.size();
        try (XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(path.toFile()))) {
            for (Iterator<Sheet> iter = wb.sheetIterator(); iter.hasNext(); ) {
                var sheet = iter.next();
                var headerCount = 0;
                for (var row : sheet) {
                    var count = row.getPhysicalNumberOfCells();
                    if (count < columns) {
                        continue;
                    }
                    WechatRecord wr = new WechatRecord();
                    for (Point<Cell, WechatRecord, ?> point : points) {
                        var cell = row.getCell(point.index(), Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                        if (nonNull(cell)) {
                            if (headerCount < columns
                                    && CellType.STRING == cell.getCellType()
                                    && Objects.equals(point.name(), cell.getStringCellValue())) {
                                headerCount++;
                                continue;
                            }
                            if (headerCount == columns) {
                                try {
                                    point.process(cell, wr);
                                } catch (Exception e) {
                                    break;
                                }
                            }
                        }
                    }
                    if (nonNull(wr.getCreatedAt()) && nonNull(wr.getAmount()) && nonNull(wr.getOrder())) {
                        records.add(wr);
                    }
                }
            }
        }
        return records;
    }

    @Override
    public void doImportFile(Path path) throws Exception {
        var records = importXlsx(path);
        convert(records, new LinkedHashMap<>());

    }

    @Override
    boolean supportExtension(String filename) {
        return filename.endsWith(getFileExtension());
    }
}
