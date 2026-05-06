package org.reploop.beancount.alipay;

import lombok.Data;
import org.reploop.beancount.BillRecord;
import org.reploop.beancount.Type;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
public class AlipayRecord extends BillRecord {
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
