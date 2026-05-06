package org.reploop.beancount.wechat;

import lombok.Data;
import org.reploop.beancount.BillRecord;

import java.math.BigDecimal;


@Data
public class WechatRecord extends BillRecord {

    public void setDoubleAmount(Double amount) {
        this.amount = BigDecimal.valueOf(amount);
    }
}
