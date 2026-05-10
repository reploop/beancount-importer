package org.reploop.beancount.jd;

import java.util.regex.Pattern;

public interface JdConstants {
    Pattern REVERSE_PATTERN = Pattern.compile("([\\d.]+)\\(已退款([\\d.]+)\\)");
    Pattern REVERSE_ALL_PATTERN = Pattern.compile("([\\d.]+)\\(已全额退款\\)");
}
