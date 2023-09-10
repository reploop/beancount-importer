package org.reploop.beancount;

import java.math.BigDecimal;
import java.time.LocalDateTime;


public class BillRecord {
    LocalDateTime createdAt;
    String category;
    String peer;
    String peerAccount;
    String goods;
    String type;
    BigDecimal amount;
    String method;
    String status;
    String order;
    String merchantOrder;
    String comment;
    Boolean processed = false;
}
