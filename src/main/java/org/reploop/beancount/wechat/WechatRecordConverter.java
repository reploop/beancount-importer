package org.reploop.beancount.wechat;

import org.reploop.beancount.AbstractRecordConverter;
import org.reploop.beancount.ReverseContext;
import org.reploop.beancount.ReverseKey;
import org.reploop.beancount.Transaction;
import org.reploop.beancount.Type;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

public class WechatRecordConverter extends AbstractRecordConverter<WechatRecord> {
    private static final Pattern PART_REVERSE_PATTERN = Pattern.compile("已退款\\((¥[\\d.]+)\\)");

    @Override
    public void start(WechatRecord r) {
        if (r.getGoods().startsWith("订单编号")) {
            r.setGoods(r.getPeer());
        }
        //method=/ and status=已存入零钱
        if (Objects.equals(r.getMethod(), "/") && Objects.equals(r.getStatus(), "已存入零钱")) {
            r.setMethod("零钱");
        }
    }

    @Override
    public ReverseKey reverseKey(WechatRecord r) {
        return new WechatReverseKey(r.getStatus(), r.getMethod());
    }

    @Override
    protected Set<String> incomeSearchList(WechatRecord r) {
        var first = segment(r.getCategory()).stream().findFirst().orElse("");
        if (Objects.equals("微信红包", first)) {
            return Set.of(first);
        }
        return Collections.emptySet();
    }

    @Override
    protected Set<String> peerSearchList(WechatRecord r) {
        Set<String> searchList = new LinkedHashSet<>();
        var category = r.getCategory();
        if (Objects.equals("扫二维码付款", category)) {
            searchList.add("扫二维码付款");
        }
        searchList.add(r.getPeer());
        searchList.add(r.getGoods());
        searchList.add(r.getCategory());
        searchList.addAll(segment(r.getPeer()));
        searchList.addAll(segment(r.getGoods()));
        searchList.addAll(segment(r.getCategory()));
        return searchList;
    }

    @Override
    protected void reverse(WechatRecord r, Transaction txn, Map<ReverseKey, ReverseContext> reverses) {
        // 部分退款
        //收入:已退款¥136.80
        //支出:已退款(¥136.80)
        // 已全额退款
        //收入:已全额退款
        //支出:已全额退款
        if (Type.EXPENSE == r.getType()) {
            var status = r.getStatus();
            var method = r.getMethod();
            if (Objects.equals("已全额退款", status)) {
                reverses.put(new WechatReverseKey(status, method), new WechatReverseContext(txn));
            } else if (status.startsWith("已退款")) {
                var m = PART_REVERSE_PATTERN.matcher(status);
                if (m.find()) {
                    status = "已退款" + m.group(1);
                    reverses.put(new WechatReverseKey(status, method), new WechatReverseContext(txn));
                }
            }
        }
    }
}
