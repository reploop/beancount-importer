package org.reploop.beancount;

import org.reploop.beancount.account.AccountMapping;
import org.reploop.beancount.account.AccountType;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

public class WechatImporter extends BillImporter<WechatRecord> {

    public static void main(String... args) throws Exception {
        WechatImporter importer = new WechatImporter();
    }

    private Map<String, String> cate;

    public void importCsv(Path path) throws Exception {
        var headers = Arrays.stream("交易时间\t交易类型\t交易对方\t商品\t收/支\t金额(元)\t支付方式\t当前状态\t交易单号\t商户单号\t备注".split("\\s+")).toList();
        var records = super.importCsv(headers, path);
        var transactions = new ArrayList<Transaction>();
        for (var record : records) {
            var type = record.getType();

            var dateTime = record.createdAt;
            var builder = Transaction.builder();
            builder.payee(record.getPeer())
                    .flag(Flag.CLOSED)
                    .dateTime(record.createdAt)
                    .narration(record.getGoods())
                    .meta(Map.of("date", dateTime.toLocalDate(), "time", dateTime.toLocalTime()));
            var method = record.getMethod();
            var elements = method.split("&");
            BigDecimal amount;
            switch (type) {
                case "支出" -> {
                    amount = record.getAmount().negate();
                }
                case "收入" -> amount = record.getAmount();
                default -> throw new IllegalStateException(type);
            }
            ;
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
    BiConsumer<WechatRecord, String> setter(int idx, String name) {
        return switch (idx) {
            case 0 -> (record, s) -> {
                try {
                    record.setCreatedAt(LocalDateTime.parse(s, formatter));
                } catch (DateTimeParseException ignored) {
                }
            };
            case 1 -> WechatRecord::setCategory;
            case 2 -> WechatRecord::setPeer;
            case 3 -> WechatRecord::setGoods;
            case 4 -> WechatRecord::setType;
            case 5 -> (record, text) -> record.setAmount(new BigDecimal(text.substring(1)));
            case 6 -> WechatRecord::setMethod;
            case 7 -> (record, text) -> {
                if ("已存入零钱".equals(text) && "收入".equals(record.getType()) && "/".equals(record.getMethod())) {
                    record.setMethod("零钱");
                }
                record.setStatus(text);
            };
            case 8 -> WechatRecord::setOrder;
            case 9 -> WechatRecord::setMerchantOrder;
            case 10 -> WechatRecord::setComment;
            default -> throw new IllegalStateException();
        };
    }

    @Override
    BillHandler<WechatRecord> billHandler(List<WechatRecord> records, List<String> headers, Map<Integer, BiConsumer<WechatRecord, String>> setters) {
        return new WechatBillHandler(records, headers, setters);
    }
}
