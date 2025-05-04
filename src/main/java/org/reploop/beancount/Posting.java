package org.reploop.beancount;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class Posting {
    private String account;
    private BigDecimal amount;
    @Builder.Default
    private Currency currency = Currency.CNY;
}
