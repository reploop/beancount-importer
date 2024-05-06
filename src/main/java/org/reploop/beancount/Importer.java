package org.reploop.beancount;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@Component
public class Importer implements InitializingBean {

    private final List<BillImporter> importers;

    public Importer(List<BillImporter> importers) {
        this.importers = importers;
    }

    private void run(Map<Source, List<Path>> sources) {
        sources.forEach((source, paths) -> {
            for (var importer : importers) {
                if (importer.support(source)) {
                    for (var path : paths) {
                        try {
                            importer.importCsv(path);
                        } catch (Exception e) {
                            log.error("Path {}", path, e);
                        }
                    }
                }
            }
        });
    }

    private String filename(Path path) {
        return path.getFileName().toString();
    }

    private final Predicate<Path> csvFilter = path -> filename(path).endsWith(".csv");

    public void run() {
        Path dir = Paths.get("/Users/gc/Downloads");
        try (var list = Files.list(dir)) {
            var sources = list.filter(Files::isReadable)
                    .filter(csvFilter)
                    .collect(Collectors.groupingBy(path -> {
                        var filename = filename(path);
                        if (filename.startsWith("alipay_")) {
                            return Source.ALIPAY;
                        } else if (filename.startsWith("微信支付账单")) {
                            return Source.WECHAT;
                        } else {
                            return Source.UNKNOWN;
                        }
                    }));
            run(sources);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        run();
    }
}
