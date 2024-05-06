package org.reploop.beancount.service.impl;


import org.reploop.beancount.entity.BillRecord;
import org.reploop.beancount.repository.BillRecordRepository;
import org.reploop.beancount.service.BillRecordService;
import org.springframework.stereotype.Service;

@Service
public class BillRecordServiceImpl implements BillRecordService {
    private final BillRecordRepository billRecordRepository;

    public BillRecordServiceImpl(BillRecordRepository billRecordRepository) {
        this.billRecordRepository = billRecordRepository;
    }

    @Override
    public void save(BillRecord billRecord) {
        billRecordRepository.save(billRecord);
    }

    @Override
    public BillRecord getByOrder(String order) {
        return billRecordRepository.findById(order).orElseThrow();
    }
}
