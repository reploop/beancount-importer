package org.reploop.beancount.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Account {
    @Id
    Long id;
    String name;
}
