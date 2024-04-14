package org.reploop.beancount;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

public class AlipayImporter extends BillImporter<BillRecord> {

    public void importCsv(Path path) throws Exception {
        var headers = EnumSet.allOf(Header.class);
        var records = importCsv(headers, path);
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
            Posting peer = Posting.builder()
                    .amount(amount)
                    .currency(Currency.CNY)
                    .account("2")
                    .build();
            Posting payer = Posting.builder()
                    .amount(amount.negate())
                    .currency(Currency.CNY)
                    .account("8099")
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
    BiConsumer<BillRecord, String> setter(Header name) {
        return switch (name) {
            case CREATED_AT -> (record, s) -> {
                try {
                    record.setCreatedAt(LocalDateTime.parse(s, formatter));
                } catch (DateTimeParseException ignored) {
                }
            };
            case CATEGORY -> BillRecord::setCategory;
            case PEER -> BillRecord::setPeer;
            case PEER_ACCOUNT -> BillRecord::setPeerAccount;
            case GOODS -> BillRecord::setGoods;
            case TYPE -> (record, text) -> record.setType(parse(text));
            case AMOUNT -> (record, text) -> record.setAmount(new BigDecimal(text));
            case METHOD -> BillRecord::setMethod;
            case STATUS -> BillRecord::setStatus;
            case ORDER -> BillRecord::setOrder;
            case MERCHANT_ORDER -> BillRecord::setMerchantOrder;
            case COMMENT -> BillRecord::setComment;
        };
    }

    @Override
    BillHandler<BillRecord> billHandler(List<BillRecord> records, Set<Header> headers, Map<Header, BiConsumer<BillRecord, String>> setters) {
        return new AlipayBillHandler(records, headers, setters);
    }
}
