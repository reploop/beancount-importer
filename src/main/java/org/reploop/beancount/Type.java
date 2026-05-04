package org.reploop.beancount;

import java.util.Arrays;
import java.util.Objects;

public enum Type {
    INCOME("收入"),
    EXPENSE("支出"),
    OTHER("其他"),
    NOT_APPLICABLE("不计入收支");
    final String text;

    Type(String text) {
        this.text = text;
    }

    public static Type textOf(String text) {
        return Arrays.stream(Type.values())
                .filter(t -> Objects.equals(t.text, text))
                .findFirst()
                .orElseThrow();
    }
}
