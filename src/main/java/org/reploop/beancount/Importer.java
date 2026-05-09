package org.reploop.beancount;

import lombok.extern.slf4j.Slf4j;
import org.reploop.beancount.alipay.AlipayImporter;
import org.reploop.beancount.alipay.AlipayOldImporter;
import org.reploop.beancount.jd.JdCsvImporter;
import org.reploop.beancount.meituan.MeiTuanCsvImporter;
import org.reploop.beancount.wechat.WechatCsvImporter;
import org.reploop.beancount.wechat.WechatXlsxImporter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class Importer {
    public static void main(String... args) {
        AlipayImporter alipayImporter = new AlipayImporter();
        AlipayOldImporter alipayOldImporter = new AlipayOldImporter();
        WechatCsvImporter csvImporter = new WechatCsvImporter();
        WechatXlsxImporter xlsxImporter = new WechatXlsxImporter();
        MeiTuanCsvImporter mtCsvImporter = new MeiTuanCsvImporter();
        JdCsvImporter jdImporter = new JdCsvImporter();
        List<BillImporter> billImporters = new ArrayList<>();
        //billImporters.add(xlsxImporter);
        billImporters.add(mtCsvImporter);
        Path dir = Paths.get("/Users/gc/Downloads/bills");
        try (var s = Files.find(dir, Integer.MAX_VALUE, (p, attrs) -> Files.isReadable(p))) {
            s.forEach(path -> {
                try {
                    for (var billImporter : billImporters) {
                        billImporter.importFile(path);
                    }
                } catch (Exception e) {
                    log.error("Cannot import file {}", path, e);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
