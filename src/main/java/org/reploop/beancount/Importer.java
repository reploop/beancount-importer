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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

@Slf4j
public class Importer {
    public static void main(String... args) {
        var billImporters = getBillImporters().stream().collect(Collectors.groupingBy(BillImporter::platform, Collectors.toList()));
        Path scanDir = Paths.get("/Users/george/personal-projects/beancount/bills");
        Path outDir = Paths.get("/Users/george/personal-projects/beancount");

        Map<Platform, List<Path>> includes = new TreeMap<>();
        try (var s = Files.find(scanDir, Integer.MAX_VALUE, (p, attrs) -> Files.isReadable(p))) {
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
                        .distinct()
                        .collect(Collectors.groupingBy(txn -> txn.getDateTime().toLocalDate().withDayOfMonth(1), LinkedHashMap::new, Collectors.toList()));
                var filenames = output(outDir, platform, monthList);

                includes.putAll(filenames);
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        includes.forEach((pf, paths) -> {
            System.out.println("; " + pf);
            paths.forEach(path -> {
                String cmd = "include \"" + path.toString() + "\"";
                System.out.println(cmd);
            });
        });
    }

    private static void makeParentDirectories(Path path) {
        try {
            Files.createDirectories(path.getParent());
        } catch (IOException e) {
            throw new RuntimeException(path.toString(), e);
        }
    }

    private static Map<Platform, List<Path>> output(Path dir, Platform platform, Map<LocalDate, List<Transaction>> txnList) {
        Map<Platform, List<Path>> filenames = new TreeMap<>();
        txnList.forEach((ym, list) -> {
            String filename = platform.name().toLowerCase() + "_" + ym.getMonthValue() + ".beancount";
            Path f = Path.of("beans", String.valueOf(ym.getYear()), filename);
            var path = dir.resolve(f);
            makeParentDirectories(path);
            try (var writer = Files.newBufferedWriter(path, UTF_8, CREATE, TRUNCATE_EXISTING, WRITE)) {
                for (var txn : list) {
                    writer.write(txn.toString());
                    writer.newLine();
                }
                filenames.computeIfAbsent(platform, k -> new ArrayList<>()).add(f);
            } catch (IOException e) {
                log.error("Cannot write file {} , platform: {}", path, platform, e);
            }
        });
        return filenames;
    }

    private static List<BillImporter> getBillImporters() {
        AlipayImporter alipayImporter = new AlipayImporter();
        WechatCsvImporter csvImporter = new WechatCsvImporter();
        WechatXlsxImporter xlsxImporter = new WechatXlsxImporter();
        MeiTuanCsvImporter mtCsvImporter = new MeiTuanCsvImporter();
        JdCsvImporter jdImporter = new JdCsvImporter();
        return List.of(mtCsvImporter, alipayImporter, csvImporter, xlsxImporter, jdImporter);
    }
}
