package org.reploop.beancount;

import org.reploop.beancount.account.AccountMapping;
import org.reploop.beancount.account.AccountType;
import org.reploop.beancount.entity.BillRecord;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;

public class WechatImporter extends BillImporter<BillRecord> {

    private Map<String, String> cate;

    public static void main(String... args) throws Exception {
        WechatImporter importer = new WechatImporter();
    }

    public void importCsv(Path path) throws Exception {
        var records = super.importCsv(EnumSet.allOf(Header.class), path);
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
            case GOODS -> BillRecord::setGoods;
            case TYPE -> (record, text) -> record.setType(parse(text));
            case AMOUNT -> (record, text) -> record.setAmount(new BigDecimal(text.substring(1)));
            case METHOD -> BillRecord::setMethod;
            case STATUS -> (record, text) -> {
                if ("已存入零钱".equals(text) && Type.INGRESS == record.getType() && "/".equals(record.getMethod())) {
                    record.setMethod("零钱");
                }
                record.setStatus(text);
            };
            case ORDER -> BillRecord::setOrder;
            case MERCHANT_ORDER -> BillRecord::setMerchantOrder;
            case COMMENT -> BillRecord::setComment;
            default -> throw new IllegalStateException();
        };
    }

    @Override
    BillHandler<BillRecord> billHandler(List<BillRecord> records, Set<Header> headers, Map<Header, BiConsumer<BillRecord, String>> setters) {
        return new WechatBillHandler(records, headers, setters);
    }
}
