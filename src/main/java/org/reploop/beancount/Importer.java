package org.reploop.beancount;

import lombok.extern.slf4j.Slf4j;
import org.reploop.beancount.alipay.AlipayImporter;
import org.reploop.beancount.jd.JdCsvImporter;
import org.reploop.beancount.meituan.MeiTuanCsvImporter;
import org.reploop.beancount.wechat.WechatCsvImporter;
import org.reploop.beancount.wechat.WechatXlsxImporter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

@Slf4j
public class Importer {
    public static void main(String... args) {
        var billImporters = getBillImporters().stream().collect(Collectors.groupingBy(BillImporter::platform, Collectors.toList()));
        Path dir = Paths.get("/Users/george/personal-projects/beancount/bills");

        DateTimeFormatter yearMonth = DateTimeFormatter.ofPattern("yyyy_MM");
        try (var s = Files.find(dir, Integer.MAX_VALUE, (p, attrs) -> Files.isReadable(p))) {
            var files = s.toList();
            billImporters.forEach((platform, list) -> {
                List<Transaction> transactions = new ArrayList<>();
                files.forEach(path -> {
                    try {
                        for (var billImporter : list) {
                            transactions.addAll(billImporter.importFile(path));
                        }
                    } catch (Exception e) {
                        log.error("Cannot import file {} , platform: {}", path, platform, e);
                    }
                });
                var monthList = transactions.stream()
                        .sorted(Comparator.comparing(Transaction::getDateTime).reversed())
                        .collect(Collectors.groupingBy(txn -> txn.getDateTime().format(yearMonth), Collectors.toList()));
                output(platform, monthList);
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void output(Platform platform, Map<String, List<Transaction>> txnList) {
        Path dir = Paths.get("/Users/george/personal-projects/beancount/bills");
        txnList.forEach((ym, list) -> {
            String filename = platform.name() + "_" + ym + ".beancount";
            var path = dir.resolve(filename);
            try (var writer = Files.newBufferedWriter(path, UTF_8, CREATE, TRUNCATE_EXISTING, WRITE)) {
                for (var txn : list) {
                    writer.write(txn.toString());
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static List<BillImporter> getBillImporters() {
        AlipayImporter alipayImporter = new AlipayImporter();
        WechatCsvImporter csvImporter = new WechatCsvImporter();
        WechatXlsxImporter xlsxImporter = new WechatXlsxImporter();
        MeiTuanCsvImporter mtCsvImporter = new MeiTuanCsvImporter();
        JdCsvImporter jdImporter = new JdCsvImporter();
        return List.of(jdImporter);
    }
}
