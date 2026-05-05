package org.reploop.beancount.account;


import tools.jackson.core.json.JsonReadFeature;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;

import static java.util.Objects.isNull;

public class AccountMapping {

    private static Map<String, Map<String, String>> mappings;

    static void loadMapping() {
        var url = AccountMapping.class.getResource("/account-mapping.json");
        assert url != null;
        loadMapping(url);
    }

    static void loadMapping(URL url) {
        var mapper = JsonMapper.builder()
                .enable(JsonReadFeature.ALLOW_UNQUOTED_PROPERTY_NAMES)
                .build();
        try (var in = url.openStream()) {
            var map = mapper.readValue(in, new TypeReference<Map<String, Map<String, String>>>() {
            });
            mappings = Collections.unmodifiableMap(map);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static String account(AccountType accountType, String method) {
        while (isNull(mappings)) {
            loadMapping();
        }
        return mappings.getOrDefault(accountType.name().toLowerCase(), Collections.emptyMap()).get(method);
    }
}
