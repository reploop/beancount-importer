package org.reploop.beancount;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication
public class Importer {
    public static void main(String... args) {
        SpringApplication.run(Importer.class, args);
    }

    public static void main() {
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
}
