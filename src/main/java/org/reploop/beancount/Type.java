package org.reploop.beancount;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public enum Type {
    INCOME("收入"),
    EXPENSE("支出"),
    OTHER("其他"),
    NOT_APPLICABLE("不计入收支", "不计收支");
    private static final Map<String, Type> cache;

    static {
        Map<String, Type> map = new HashMap<>();
        for (Type type : Type.values()) {
            for (var val : type.text) {
                map.put(val, type);
            }
        }
        cache = Collections.unmodifiableMap(map);
    }

    final Set<String> text = new HashSet<>();

    Type(String... names) {
        this.text.addAll(Arrays.asList(names));
    }

    public static Type textOf(String text) {
        return cache.get(text);
    }
}
