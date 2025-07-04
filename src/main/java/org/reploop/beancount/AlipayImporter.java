package org.reploop.beancount;

import org.reploop.beancount.account.AccountType;
import org.reploop.beancount.entity.BillRecord;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;

@Component
public class AlipayImporter extends BillImporter {

    private static final String ORDER_NO_KEY = "order";

    protected AlipayImporter() {
        super(Source.ALIPAY);
    }

    @Override
    public void process(List<Path> paths) throws Exception {
        var records = importCsv(paths);
        Map<String, Transaction> transactions = new LinkedHashMap<>();
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
            var account = searchAccount(record.getCategory(), first(record.getPeer()), record.getPeer(), first(record.getGoods()), record.getGoods()).orElse("------");
            Posting peer = Posting.builder()
                    .amount(amount)
                    .currency(Currency.CNY)
                    .account(account)
                    .build();
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
                var elements = fullOrderNo.split("[_*]+");
                if (elements.length > 1) {
                    var paymentOrder = elements[0];
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
        output(transactions.values());
    }

    @Override
    BillHandler<BillRecord> billHandler(List<BillRecord> records) {
        return new AlipayBillHandler(records);
    }
}
