package org.reploop.beancount.jd;

import org.reploop.beancount.AbstractRecordConverter;
import org.reploop.beancount.ReverseContext;
import org.reploop.beancount.ReverseKey;
import org.reploop.beancount.StringReverseKey;
import org.reploop.beancount.Transaction;
import org.reploop.beancount.Type;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static java.util.Objects.nonNull;
import static org.reploop.beancount.jd.JdConstants.REVERSE_PATTERN;

public class JdRecordConverter extends AbstractRecordConverter<JdRecord> {


    @Override
    public ReverseContext reverseContext(JdRecord r, Map<ReverseKey, ReverseContext> contexts) {
        if (r.getGoods().startsWith("退款-") && Type.NOT_APPLICABLE == r.getType()) {
            var key = new StringReverseKey(r.getOrder());
            return contexts.get(key);
        }
        return null;
    }

    @Override
    protected Set<String> peerSearchList(JdRecord r) {
        Set<String> searchList = new LinkedHashSet<>();
        searchList.addAll(segment(r.getCategory()));
        searchList.addAll(segment(r.getGoods()));
        return searchList;
    }

    @Override
    protected boolean test(JdRecord r) {
        // WeChat payment filter out
        return !Objects.equals("微信支付", r.getMethod());
    }

    @Override
    protected void reverse(JdRecord r, Transaction txn, Map<ReverseKey, ReverseContext> reverseMap) {
        // Reverse Txn
        var amountText = r.getAmountText();
        if (nonNull(amountText) && Type.EXPENSE == r.getType()) {
            var m = REVERSE_PATTERN.matcher(amountText);
            if (m.find()) {
                var reverseAmount = new BigDecimal(m.group(2));
                reverseMap.put(new StringReverseKey(r.getOrder()), new JdReverseContext(txn, reverseAmount));
            }
        }
    }
}
