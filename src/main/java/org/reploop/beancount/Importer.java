package org.reploop.beancount;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Importer {
    public static void main(String... args) {
        AlipayImporter alipayImporter = new AlipayImporter();
        AlipayOldImporter alipayOldImporter = new AlipayOldImporter();
        WechatCsvImporter csvImporter = new WechatCsvImporter();
        WechatXlsxImporter xlsxImporter = new WechatXlsxImporter();
        Path dir = Paths.get("/Users/george/personal-projects/beancount/bills");
        try (var s = Files.list(dir)) {
            s.filter(Files::isReadable)
                    .forEach(path -> {
                        var filename = path.getFileName().toString();
                        try {
                            if (filename.startsWith("支付宝交易明细")) {
                                try {
                                    alipayImporter.importCsv(path);
                                } catch (Exception e) {
                                    alipayOldImporter.importCsv(path);
                                }
                            } else if (filename.startsWith("微信支付账单")) {
                                if (filename.endsWith(".xlsx")) {
                                    //xlsxImporter.importBill(path);
                                } else if (filename.endsWith(".csv")) {
                                    //csvImporter.importBill(path);
                                }
                            }
                        } catch (Exception e) {
                            System.err.println(path);
                            e.printStackTrace();
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
