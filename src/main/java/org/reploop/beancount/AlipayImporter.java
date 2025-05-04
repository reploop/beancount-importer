package org.reploop.beancount;

import org.reploop.beancount.account.AccountType;
import org.reploop.beancount.entity.BillRecord;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;

@Component
public class AlipayImporter extends BillImporter {

    private static final String ORDER_NO_KEY = "order";

    @Override
    public void process(Path path) throws Exception {
        var records = importCsv(path);
        Map<String, Transaction> transactions = new HashMap<>();
        for (var record : records) {
            var dateTime = record.getCreatedAt();
            var builder = Transaction.builder();
            builder.dateTime(dateTime)
                    .flag(Flag.CLOSED)
                    .narration(record.getGoods())
                    .payee(record.getPeer())
                    .meta(Map.of(DATE_KEY, dateTime.toLocalDate(), TIME_KEY, dateTime.toLocalTime(), ORDER_NO_KEY, record.getOrder()));
            var amount = switch (record.getType()) {
                case INGRESS -> record.getAmount().negate();
                case EGRESS -> record.getAmount();
            };
            var account = searchAccount(record.getCategory(), record.getPeer()).orElse("------");
            Posting peer = Posting.builder()
                    .amount(amount)
                    .currency(Currency.CNY)
                    .account(account)
                    .build();
            System.out.println(record.getMethod());
            var paymentMethod = record.getMethod().split("&")[0];
            var v = searchAccount(EnumSet.of(AccountType.LIABILITIES, AccountType.ASSETS), paymentMethod).orElse("------");
            Posting payer = Posting.builder()
                    .amount(amount.negate())
                    .currency(Currency.CNY)
                    .account(v)
                    .build();
            builder.postings(Stream.of(peer, payer).sorted(Comparator.comparing(Posting::getAmount)).toList());
            var tnx = builder.build();
            var category = record.getCategory();
            if (Objects.equals(REFUND, category)) {
                var fullOrderNo = record.getOrder();
                var index = fullOrderNo.indexOf("_");
                if (index > 0) {
                    var paymentOrder = fullOrderNo.substring(0, index);
                    var transaction = transactions.get(paymentOrder);
                    if (nonNull(transaction)) {
                        var ps = transaction.getPostings().stream()
                                .map(p -> Posting.builder()
                                        .amount(reverse(p.getAmount(), record.getAmount()))
                                        .account(p.getAccount())
                                        .currency(p.getCurrency())
                                        .build())
                                .sorted(Comparator.comparing(Posting::getAmount))
                                .toList();
                        tnx = builder.postings(ps).build();
                    }
                }
            }
            transactions.put(record.getOrder(), tnx.addIgnoreKeys(ORDER_NO_KEY));
        }
        transactions.values().forEach(System.out::println);
    }

    @Override
    boolean support(Source source) {
        return source == Source.ALIPAY;
    }

    @Override
    BillHandler<BillRecord> billHandler(List<BillRecord> records) {
        return new AlipayBillHandler(records);
    }
}
