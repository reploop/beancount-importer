package org.reploop.beancount.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;

@Slf4j
@Configuration
public class ImporterConfig {

    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSourceProperties dataSourceProperties(@Value("classpath:/data/rich-book.db") Resource db) throws IOException {
        var properties = new DataSourceProperties();
        properties.setDriverClassName("org.hsqldb.jdbc.JDBCDriver");
        String url = String.join(":", "jdbc", "hsqldb", db.getURI().toString());
        log.debug("JDBC url: {}", url);
        properties.setUrl(url);
        return properties;
    }
}
