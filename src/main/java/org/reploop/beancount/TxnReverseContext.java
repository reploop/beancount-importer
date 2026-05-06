package org.reploop.beancount;

public record TxnReverseContext(Transaction txn) implements ReverseContext {
    @Override
    public Transaction getTxn() {
        return txn;
    }
}
