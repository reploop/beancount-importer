package org.reploop.beancount.jd;

import lombok.Data;
import org.reploop.beancount.Type;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class JdRecord {
    String merchantOrderNo;
    String orderNo;
    String notes;
    String status;
    String category;
    Type type;
    String method;
    BigDecimal amount;
    String description;
    String merchantName;
    LocalDateTime transactionTime;

    String amountText;

}
