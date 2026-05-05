package org.reploop.beancount.meituan;

import lombok.Data;
import org.reploop.beancount.Type;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MeiTuanRecord {
    LocalDateTime createTime;
    LocalDateTime successTime;
    String category;
    String goods;
    Type type;
    String method;
    BigDecimal amount;
    BigDecimal actualAmount;
    String orderNo;
    String merchantOrderNo;
    String remarks;
}
