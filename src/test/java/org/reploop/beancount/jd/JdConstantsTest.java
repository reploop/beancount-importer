package org.reploop.beancount.jd;

import org.junit.jupiter.api.Test;

class JdConstantsTest {
    @Test
    void name() {
        String val = "223(已退款3.4)";
        //val = "223(已全额退款4.2)";
        var m = JdConstants.REVERSE_PATTERN.matcher(val);
        if (m.find()) {
            val = m.group(1);
            System.out.println(m.group(1));
        }
    }
}