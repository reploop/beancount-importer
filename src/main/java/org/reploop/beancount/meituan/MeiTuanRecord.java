package org.reploop.beancount.meituan;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.reploop.beancount.BillRecord;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
public class MeiTuanRecord extends BillRecord {
    LocalDateTime successTime;
    BigDecimal actualAmount;
}
