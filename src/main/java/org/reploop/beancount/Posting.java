package org.reploop.beancount;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class Posting {
    String account;
    BigDecimal amount;
    @Builder.Default
    Currency currency = Currency.CNY;
}
