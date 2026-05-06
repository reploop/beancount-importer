package org.reploop.beancount.alipay;

import org.reploop.beancount.AbstractRecordConverter;
import org.reploop.beancount.ReverseContext;
import org.reploop.beancount.ReverseKey;
import org.reploop.beancount.StringReverseKey;
import org.reploop.beancount.Transaction;
import org.reploop.beancount.TxnReverseContext;
import org.reploop.beancount.Type;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class AlipayRecordConverter extends AbstractRecordConverter<AlipayRecord> {

    @Override
    protected boolean test(AlipayRecord r) {
        // not paid
        return Type.NOT_APPLICABLE != r.getType() || !Objects.equals("交易关闭", r.getStatus());
    }

    @Override
    protected Set<String> mySearchList(AlipayRecord r) {
        var method = r.getMethod();
        return Arrays.stream(method.split("&"))
                .map(String::trim)
                .collect(Collectors.toSet());
    }

    @Override
    protected Set<String> peerSearchList(AlipayRecord r) {
        Set<String> sets = new LinkedHashSet<>();
        sets.add(r.getPeer());
        sets.addAll(segment(r.getPeer()));
        sets.addAll(segment(r.getGoods()));
        sets.add(r.getCategory());
        return sets;
    }

    @Override
    public ReverseKey reverseKey(AlipayRecord r) {
        var order = r.getOrder();
        int idx;
        if (Objects.equals("退款成功", r.getStatus()) && (idx = order.indexOf("_")) > 0) {
            return new StringReverseKey(order.substring(0, idx));
        }
        return null;
    }

    @Override
    protected void reverse(AlipayRecord r, Transaction txn, Map<ReverseKey, ReverseContext> reverseMap) {
        reverseMap.put(new StringReverseKey(r.getOrder()), new TxnReverseContext(txn));
    }
}
