package org.reploop.beancount;

import org.junit.jupiter.api.Test;

class BillImporterTest {

    @Test
    void first() {
        BillImporter bi = new AlipayImporter();
        String s = bi.first("美团/大众点评点餐订单-01294719779901312569983");
        System.out.println(s);
        s = bi.first("Name.com，name.com/chat");
        System.out.println(s);

    }
}