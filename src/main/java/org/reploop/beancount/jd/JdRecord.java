package org.reploop.beancount.jd;

import lombok.Data;
import org.reploop.beancount.BillRecord;

@Data
public class JdRecord extends BillRecord {
    String amountText;
}
