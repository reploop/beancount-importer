package org.reploop.beancount.account;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

public class AccountMapping {

    private static final Map<String, Map<String, String>> mappings;

    static {
        var mapper = JsonMapper.builder()
                .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
                .build();
        try {
            var input = AccountMapping.class.getResourceAsStream("/config.json");
            var map = mapper.readValue(input, new TypeReference<Map<String, Map<String, String>>>() {
            });
            mappings = Collections.unmodifiableMap(map);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static String account(AccountType accountType, String method) {
        return mappings.getOrDefault(accountType.name().toLowerCase(), Collections.emptyMap()).get(method);
    }
}
