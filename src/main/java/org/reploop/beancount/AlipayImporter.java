package org.reploop.beancount;

import org.reploop.beancount.account.AccountMapping;
import org.reploop.beancount.account.AccountType;
import org.reploop.beancount.entity.BillRecord;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class AlipayImporter extends BillImporter {

    @Override
    public void process(Path path) throws Exception {
        var records = importCsv(path);
        Map<String, Transaction> transactionMap = new HashMap<>();
        for (var record : records) {
            var dateTime = record.getCreatedAt();
            var builder = Transaction.builder();
            builder.dateTime(dateTime)
                    .flag(Flag.CLOSED)
                    .narration(record.getGoods())
                    .payee(record.getPeer())
                    .meta(Map.of("date", dateTime.toLocalDate(), "time", dateTime.toLocalTime()));
            var amount = switch (record.getType()) {
                case INGRESS -> record.getAmount().negate();
                case EGRESS -> record.getAmount();
            };
            var account = AccountMapping.account(AccountType.EXPENSES, record.getCategory());
            Posting peer = Posting.builder()
                    .amount(amount)
                    .currency(Currency.CNY)
                    .account(account)
                    .build();
            System.out.println(record.getMethod());
            var v = AccountMapping.account(AccountType.LIABILITIES, record.getMethod());
            Posting payer = Posting.builder()
                    .amount(amount.negate())
                    .currency(Currency.CNY)
                    .account(v)
                    .build();
            builder.postings(List.of(peer, payer));
            var tnx = builder.build();

            System.out.println(tnx);
        }

        var types = records.stream().map(BillRecord::getType).sorted().distinct().toList();
        System.out.println(types);
        var methods = records.stream().map(BillRecord::getMethod).sorted().distinct().toList();
        System.out.println(methods);
        var goods = records.stream().map(BillRecord::getGoods)
                .map(s -> s.replaceAll("\\d+", ""))
                .sorted().distinct().toList();
        System.out.println(goods);
        var payee = records.stream().map(BillRecord::getPeer)
                .sorted()
                .distinct().toList();
        System.out.println(payee);
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
