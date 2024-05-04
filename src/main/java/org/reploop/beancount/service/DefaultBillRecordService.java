package org.reploop.beancount.service;


import org.reploop.beancount.entity.BillRecord;
import org.reploop.beancount.repository.BillRecordRepository;

public class DefaultBillRecordService implements BillRecordService {
    BillRecordRepository billRecordRepository;

    @Override
    public void save(BillRecord billRecord) {
        billRecordRepository.save(billRecord);
    }

    @Override
    public BillRecord getByOrder(String order) {
        return billRecordRepository.findById(order).orElseThrow();
    }
}
