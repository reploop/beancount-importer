package org.reploop.beancount;

import java.util.Arrays;
import java.util.Set;

import static java.util.Objects.nonNull;

/**
 * 交易时间	交易类型	交易对方	商品	收/支	金额(元)	支付方式	当前状态	交易单号	商户单号	备注
 * 交易时间	交易分类	交易对方	对方账号	商品说明	收/支	金额	收/付款方式	交易状态	交易订单号	商家订单号	备注
 */
public enum Header {
    CREATED_AT("交易时间"),
    CATEGORY("交易分类", "交易类型"),
    PEER("交易对方"),
    PEER_ACCOUNT("对方账号"),
    GOODS("商品说明", "商品"),
    TYPE("收/支"),
    AMOUNT("金额", "金额(元)"),
    METHOD("收/付款方式", "支付方式"),
    STATUS("交易状态", "当前状态"),
    ORDER("交易订单号", "交易单号"),
    MERCHANT_ORDER("商家订单号", "商户单号"),
    COMMENT("备注");
    private final Set<String> titles;

    Header(String title) {
        this.titles = Set.of(title);
    }

    Header(String... titles) {
        this.titles = Set.of(titles);
    }

    public static Header findByText(String text) {
        return Arrays.stream(Header.values()).filter(h -> h.contains(text)).findAny().orElse(null);
    }

    public boolean contains(String text) {
        return nonNull(text) && titles.contains(text);
    }
}
