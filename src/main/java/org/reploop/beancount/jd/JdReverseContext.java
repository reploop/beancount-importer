package org.reploop.beancount.jd;

import org.reploop.beancount.ReverseContext;
import org.reploop.beancount.Transaction;

import java.math.BigDecimal;

record JdReverseContext(Transaction txn, BigDecimal amount) implements ReverseContext {
    @Override
    public Transaction getTxn() {
        return txn;
    }
}
