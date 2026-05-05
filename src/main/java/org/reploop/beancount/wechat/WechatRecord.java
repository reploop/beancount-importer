package org.reploop.beancount.wechat;

import lombok.Data;
import org.reploop.beancount.Type;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
public class WechatRecord {
    LocalDateTime createdAt;
    String category;
    String peer;
    String  goods;
    Type type;
    BigDecimal amount;
    String method;
    String status;
    String order;
    String merchantOrder;
    String comment;

    public void setDoubleAmount(Double amount) {
        this.amount = BigDecimal.valueOf(amount);
    }
}
