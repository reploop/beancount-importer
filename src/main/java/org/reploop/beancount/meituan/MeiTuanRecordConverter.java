package org.reploop.beancount.meituan;

import org.reploop.beancount.AbstractRecordConverter;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public class MeiTuanRecordConverter extends AbstractRecordConverter<MeiTuanRecord> {
    private static final String SUFFIX = "订单详情";

    @Override
    public void start(MeiTuanRecord r) {
        var goods = r.getGoods();
        if (goods.endsWith(SUFFIX)) {
            goods = goods.substring(0, goods.length() - SUFFIX.length()).trim();
            r.setGoods(goods);
        }
        var payee = segment(goods, null).getFirst();
        r.setPeer(payee);
    }

    @Override
    protected boolean test(MeiTuanRecord r) {
        // WeChat payment filter out
        return !(Objects.equals("支付宝支付", r.getMethod()) || Objects.equals("微信支付", r.getMethod()));
    }

    @Override
    protected Set<String> peerSearchList(MeiTuanRecord r) {
        return new LinkedHashSet<>(segment(r.getGoods()));
    }
}
