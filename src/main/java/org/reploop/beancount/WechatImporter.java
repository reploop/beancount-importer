package org.reploop.beancount;

import org.reploop.beancount.account.AccountMapping;
import org.reploop.beancount.account.AccountType;
import org.reploop.beancount.entity.BillRecord;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class WechatImporter extends BillImporter {

    @Override
    public void process(Path path) throws Exception {
        var records = importCsv(path);
        var transactions = new ArrayList<Transaction>();
        for (var record : records) {
            var type = record.getType();
            var dateTime = record.getCreatedAt();
            var builder = Transaction.builder();
            builder.payee(record.getPeer())
                    .flag(Flag.CLOSED)
                    .dateTime(record.getCreatedAt())
                    .narration(record.getGoods())
                    .meta(Map.of("date", dateTime.toLocalDate(), "time", dateTime.toLocalTime()));
            var method = record.getMethod();
            var elements = method.split("&");
            BigDecimal amount = switch (type) {
                case EGRESS -> record.getAmount().negate();
                case INGRESS -> record.getAmount();
            };
            var category = record.getCategory();
            record.getPeer();
            record.getGoods();
            record.getType();
            var myAccount = Arrays.stream(AccountType.values())
                    .map(t -> AccountMapping.account(t, method))
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(method);
            Posting payer = Posting.builder()
                    .account(myAccount)
                    .amount(amount)
                    .build();
            var peerAccount = ";";
            Posting payee = Posting.builder()
                    .amount(amount.negate())
                    .account(peerAccount).build();
            if (amount.compareTo(BigDecimal.ZERO) > 0) {
                builder.postings(List.of(payee, payer));
            } else {
                builder.postings(List.of(payer, payee));
            }
            var txn = builder.build();
            transactions.add(txn);
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
