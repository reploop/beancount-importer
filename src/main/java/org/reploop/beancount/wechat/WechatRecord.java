package org.reploop.beancount.wechat;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.reploop.beancount.BillRecord;

import java.math.BigDecimal;


@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
public class WechatRecord extends BillRecord {

    public void setDoubleAmount(Double amount) {
        this.amount = BigDecimal.valueOf(amount);
    }
}
