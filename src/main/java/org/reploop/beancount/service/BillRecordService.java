package org.reploop.beancount.service;


import org.reploop.beancount.entity.BillRecord;

public interface BillRecordService {
    void save(BillRecord billRecord);

    BillRecord getByOrder(String order);
}
