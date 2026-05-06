package org.reploop.beancount.alipay;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.reploop.beancount.BillRecord;


@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
public class AlipayRecord extends BillRecord {

}
