package org.reploop.beancount;

import org.reploop.beancount.entity.BillRecord;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;

@Component
public class WechatImporter extends BillImporter {

    public static final String TOTAL_REFUND = "已全额退款";
    public static final String CATEGORY_KEY = "_category";
    private static final Pattern pattern = Pattern.compile("已退款\\(￥([\\d.]+)\\)");

    protected WechatImporter() {
        super(Source.WECHAT);
    }

    @Override
    public void process(List<Path> paths) throws Exception {
        var records = importCsv(paths);
        var transactions = new ArrayList<Transaction>();
        List<Transaction> partialRefunds = new ArrayList<>();
        List<Transaction> totalRefunds = new LinkedList<>();
        for (var record : records) {
            var type = record.getType();
            var dateTime = record.getCreatedAt();
            var builder = Transaction.builder();
            builder.payee(record.getPeer())
                    .flag(Flag.CLOSED)
                    .dateTime(record.getCreatedAt())
                    .narration(record.getGoods())
                    .meta(Map.of(DATE_KEY, dateTime.toLocalDate(), TIME_KEY, dateTime.toLocalTime(), STATUS_KEY, record.getStatus(), CATEGORY_KEY, record.getCategory()));
            String method = record.getMethod().split("&")[0];
            BigDecimal amount = switch (type) {
                case EGRESS -> record.getAmount().negate();
                case INGRESS -> record.getAmount();
            };

            var myAccount = searchAccount(method).orElse(method);
            Posting my = Posting.builder()
                    .account(myAccount)
                    .amount(amount)
                    .build();

            var goods = record.getGoods();
            var peer = record.getPeer();
            var peerAccount = searchAccount(first(peer), peer, first(goods), goods, record.getCategory()).orElse("------");
            Posting pp = Posting.builder()
                    .amount(amount.negate())
                    .account(peerAccount)
                    .build();

            // OUT -> IN order
            builder.postings(Stream.of(pp, my).sorted(Comparator.comparing(Posting::getAmount)).toList());
            var txn = builder.build().addIgnoreKeys(STATUS_KEY, CATEGORY_KEY);
            // Match refund transactions
            var status = record.getStatus();
            if (Objects.equals(status, TOTAL_REFUND)) {
                totalRefunds.add(txn);
                totalRefunds = match(totalRefunds);
            } else if (status.contains(REFUND)) {
                // It's the original transaction but its status contains refund keywords.
                if (status.contains("(") && status.contains(")")) {
                    partialRefunds.add(txn);
                } else {
                    var amt = record.getAmount();
                    var it = partialRefunds.iterator();
                    while (it.hasNext()) {
                        Transaction transaction = it.next();
                        var postings = transaction.getPostings();
                        var prev = transaction.getMetaValue(STATUS_KEY, String.class);
                        // Partial refund
                        var m = pattern.matcher(prev);
                        if (m.find() && (new BigDecimal(m.group(1).trim())).compareTo(amt) == 0) {
                            var ps = postings.stream()
                                    .map(p -> Posting.builder()
                                            .amount(reverse(p.getAmount(), amt))
                                            .account(p.getAccount())
                                            .currency(p.getCurrency())
                                            .build())
                                    .sorted(Comparator.comparing(Posting::getAmount))
                                    .toList();
                            txn.setPostings(ps);
                            it.remove();
                            break;
                        }
                    }
                }
            }
            transactions.add(txn);
        }
        output(transactions);
    }

    private List<Transaction> match(List<Transaction> totalRefunds) {
        Set<Integer> matched = new HashSet<>();
        // Refund Orders are not ordered.
        for (int m = 0; m < totalRefunds.size(); m++) {
            var payment = totalRefunds.get(m);
            var category = payment.getMetaValue(CATEGORY_KEY, String.class);
            if (nonNull(category) && category.contains(REFUND)) {
                continue;
            }
            BigDecimal total = totalAmount(payment);
            BigDecimal sum = BigDecimal.ZERO;
            int i = m + 1;
            for (; i < totalRefunds.size(); i++) {
                var t = totalRefunds.get(i);
                var c = t.getMetaValue(CATEGORY_KEY, String.class);
                if (nonNull(c) && !c.contains(REFUND)) {
                    continue;
                }
                matched.add(i);
                sum = sum.add(totalAmount(t));
                if (sum.compareTo(total) == 0) {
                    break;
                }
            }
            // Found a match
            if (i > m && i < totalRefunds.size()) {
                for (var n : matched) {
                    var t = totalRefunds.get(n);
                    var refund = totalAmount(t);
                    var ps = payment.getPostings().stream()
                            .map(p -> Posting.builder()
                                    .amount(reverse(p.getAmount(), refund))
                                    .currency(p.getCurrency())
                                    .account(p.getAccount())
                                    .build())
                            .sorted(Comparator.comparing(Posting::getAmount))
                            .toList();
                    t.setPostings(ps);
                }
                matched.add(m);
                break;
            } else {
                matched.clear();
            }
        }
        if (matched.isEmpty()) {
            return totalRefunds;
        } else {
            List<Transaction> list = new ArrayList<>();
            for (int i = 0; i < totalRefunds.size(); i++) {
                if (matched.contains(i)) {
                    continue;
                }
                list.add(totalRefunds.get(i));
            }
            return list;
        }
    }

    private BigDecimal totalAmount(Transaction txn) {
        return txn.getPostings().stream()
                .map(Posting::getAmount)
                .filter(amt -> amt.compareTo(BigDecimal.ZERO) > 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    BillHandler<BillRecord> billHandler(List<BillRecord> records) {
        return new WechatBillHandler(records);
    }
}
