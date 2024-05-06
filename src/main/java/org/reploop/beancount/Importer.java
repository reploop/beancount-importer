package org.reploop.beancount;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Component
public class Importer implements InitializingBean {

    private final List<BillImporter> importers;

    public Importer(List<BillImporter> importers) {
        this.importers = importers;
    }

    public void run() {
        AlipayImporter alipayImporter = new AlipayImporter();
        WechatImporter wechatImporter = new WechatImporter();
        Path dir = Paths.get("/Users/gc/Downloads");
        try (var s = Files.list(dir)) {
            s.filter(Files::isReadable)
                    .forEach(path -> {
                        var filename = path.getFileName().toString();
                        try {
                            if (filename.startsWith("alipay_")) {
                                alipayImporter.importCsv(path);
                            } else if (filename.startsWith("微信支付账单")) {
                                wechatImporter.importCsv(path);
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

    @Override
    public void afterPropertiesSet() throws Exception {
        run();
    }
}
