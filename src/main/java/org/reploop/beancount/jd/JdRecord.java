package org.reploop.beancount.jd;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.reploop.beancount.BillRecord;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
public class JdRecord extends BillRecord {
    String amountText;
}
