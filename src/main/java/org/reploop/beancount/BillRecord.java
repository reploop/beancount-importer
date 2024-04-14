package org.reploop.beancount;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
public class BillRecord {
    LocalDateTime createdAt;
    String category;
    String peer;
    String peerAccount;
    String goods;
    Type type;
    BigDecimal amount;
    String method;
    String status;
    String order;
    String merchantOrder;
    String comment;
}
