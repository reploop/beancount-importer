package org.reploop.beancount.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import org.reploop.beancount.Type;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
@Entity
public class BillRecord {
    LocalDateTime createdAt;
    String category;
    String peer;
    String peerAccount;
    String goods;
    Type type;
    BigDecimal amount;
    String method;
    String status;
    @Id
    String order;
    String merchantOrder;
    String comment;
}
