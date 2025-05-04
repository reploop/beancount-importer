package org.reploop.beancount;

import org.reploop.beancount.entity.BillRecord;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Component
public class WechatImporter extends BillImporter {

    private static final Pattern pattern = Pattern.compile("已退款\\(￥([\\d.]+)\\)");

    @Override
    public void process(Path path) throws Exception {
        var records = importCsv(path);
        var transactions = new ArrayList<Transaction>();
        var refunds = new ArrayList<Transaction>();
        for (var record : records) {
            var type = record.getType();
            var dateTime = record.getCreatedAt();
            var builder = Transaction.builder();
            builder.payee(record.getPeer())
                    .flag(Flag.CLOSED)
                    .dateTime(record.getCreatedAt())
                    .narration(record.getGoods())
                    .meta(Map.of(DATE_KEY, dateTime.toLocalDate(), TIME_KEY, dateTime.toLocalTime(), STATUS_KEY, record.getStatus()));
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
            var peerAccount = searchAccount(first(peer), first(goods)).orElse("------");
            Posting pp = Posting.builder()
                    .amount(amount.negate())
                    .account(peerAccount)
                    .build();

            // OUT -> IN order
            builder.postings(Stream.of(pp, my).sorted(Comparator.comparing(Posting::getAmount)).toList());
            var txn = builder.build();
            // Match refund transactions
            var status = record.getStatus();
            if (status.contains(REFUND)) {
                // It's the original transaction but its status contains refund keywords.
                if (refunds.isEmpty()) {
                    refunds.add(txn);
                } else {
                    var amt = record.getAmount();
                    var it = refunds.iterator();
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
                            txn = builder.postings(ps).build();
                            it.remove();
                            break;
                        } else {
                            // Full refund
                            boolean match = false;
                            for (var posting : postings) {
                                if (amt.add(posting.getAmount()).compareTo(BigDecimal.ZERO) == 0 && Objects.equals(myAccount, posting.getAccount())) {
                                    match = true;
                                    break;
                                }
                            }
                            if (match) {
                                List<Posting> ps = postings.stream()
                                        .map(p -> Posting.builder()
                                                .account(p.getAccount())
                                                .currency(p.getCurrency())
                                                .amount(p.getAmount().negate())
                                                .build())
                                        .sorted(Comparator.comparing(Posting::getAmount))
                                        .toList();
                                txn = builder.postings(ps).build();
                                it.remove();
                                break;
                            }
                        }
                    }
                }
            }
            transactions.add(txn.addIgnoreKeys(STATUS_KEY));
        }
        System.out.println();
        for (var txn : transactions) {
            System.out.println(txn);
        }
    }


    @Override
    boolean support(Source source) {
        return source == Source.WECHAT;
    }

    @Override
    BillHandler<BillRecord> billHandler(List<BillRecord> records) {
        return new WechatBillHandler(records);
    }
}
