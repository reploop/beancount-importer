package org.reploop.beancount;


public interface BillRecordService {
    void save(BillRecord billRecord);

    BillRecord findByOrder(String order);
}
