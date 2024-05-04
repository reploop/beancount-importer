package org.reploop.beancount.repository;

import org.reploop.beancount.entity.BillRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillRecordRepository extends JpaRepository<BillRecord, String> {
}
