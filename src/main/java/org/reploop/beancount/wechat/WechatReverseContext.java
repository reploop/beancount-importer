package org.reploop.beancount.wechat;

import org.reploop.beancount.ReverseContext;
import org.reploop.beancount.Transaction;

public record WechatReverseContext(Transaction txn) implements ReverseContext {
    @Override
    public Transaction getTxn() {
        return txn;
    }
}
