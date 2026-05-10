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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

public class AlipayRecordConverter extends AbstractRecordConverter<AlipayRecord> {

    @Override
    public void start(AlipayRecord r) {
        if (Objects.equals("还款成功", r.getStatus()) && r.getGoods().startsWith("自动还款")) {
            r.setType(Type.EXPENSE);
        }
    }

    @Override
    protected boolean test(AlipayRecord r) {
        // not paid
        return nonNull(r.getMethod()) && (Type.NOT_APPLICABLE != r.getType() || !Objects.equals("交易关闭", r.getStatus()));
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

    private static final List<String> ORDER_SEP = List.of("*", "_");

    @Override
    protected ReverseContext reverseContext(AlipayRecord r, Map<ReverseKey, ReverseContext> contexts) {
        if (Objects.equals("退款成功", r.getStatus())) {
            var order = r.getOrder();
            String orderNo = null;
            for (var sep : ORDER_SEP) {
                var idx = order.indexOf(sep);
                if (idx > 0) {
                    orderNo = order.substring(0, idx);
                    break;
                }
            }
            if (nonNull(orderNo)) {
                var key = new StringReverseKey(orderNo);
                return contexts.remove(key);
            }
        }
        return null;
    }

    @Override
    protected void reverse(AlipayRecord r, Transaction txn, Map<ReverseKey, ReverseContext> reverseMap) {
        reverseMap.put(new StringReverseKey(r.getOrder()), new TxnReverseContext(txn));
    }
}
