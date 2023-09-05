package org.reploop.beancount;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
public class WechatRecord {
    LocalDateTime createdAt;
    String category;
    String peer;
    String goods;
    String type;
    BigDecimal amount;
    String method;
    String status;
    String order;
    String merchantOrder;
    String comment;
}
