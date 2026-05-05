package org.reploop.beancount.jd;

import org.reploop.beancount.BillHandler;
import org.reploop.beancount.CsvBillImporter;
import org.reploop.beancount.Flag;
import org.reploop.beancount.Posting;
import org.reploop.beancount.Transaction;
import org.reploop.beancount.Type;
import org.reploop.beancount.account.AccountMapping;
import org.reploop.beancount.account.AccountType;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class JdCsvImporter implements CsvBillImporter<JdRecord> {
    static final Pattern REVERSE_PATTERN = Pattern.compile("([\\d.]+)\\(已退款([\\d.]+)\\)");

    @Override
    public BiConsumer<JdRecord, String> setter(int idx, String name) {
        return switch (idx) {
            case 0 -> (jdRecord, s) -> jdRecord.setTransactionTime(LocalDateTime.parse(s, formatter));
            case 1 -> JdRecord::setMerchantName;
            case 2 -> JdRecord::setDescription;
            case 3 -> (jdRecord, s) -> {
                var m = REVERSE_PATTERN.matcher(s);
                String value = s;
                if (m.find()) {
                    jdRecord.setAmountText(s);
                    value = m.group(1);
                }
                jdRecord.setAmount(new BigDecimal(value));
            };
            case 4 -> JdRecord::setMethod;
            case 5 -> JdRecord::setStatus;
            case 6 -> (jdRecord, s) -> jdRecord.setType(Type.textOf(s));
            case 7 -> JdRecord::setCategory;
            case 8 -> JdRecord::setOrderNo;
            case 9 -> JdRecord::setMerchantOrderNo;
            case 10 -> JdRecord::setNotes;
            default -> throw new IllegalStateException("Unexpected value: " + idx);
        };
    }

    @Override
    public BillHandler<JdRecord> billHandler(List<JdRecord> records, List<String> headers, Map<Integer, BiConsumer<JdRecord, String>> setters) {
        return new JdBillHandler(records, headers, setters);
    }

    @Override
    public void doImportFile(Path path) throws Exception {
        var headers = Arrays.stream("交易时间,商户名称,交易说明,金额,收/付款方式,交易状态,收/支,交易分类,交易订单号,商家订单号,备注".split(",")).toList();
        if (support(path)) {
            Map<String, ReverseContext> reverseTxn = new HashMap<>();
            var records = importCsv(headers, path);
            var list = records.stream().sorted(Comparator.comparing(JdRecord::getTransactionTime)).toList();
            for (JdRecord r : list) {
                // WeChat payment filter out
                if (Objects.equals("微信支付", r.getMethod())) {
                    continue;
                }
                var dateTime = r.getTransactionTime();
                var tb = Transaction.builder()
                        .flag(Flag.CLOSED)
                        .dateTime(dateTime)
                        .payee(r.getMerchantName())
                        .meta(Map.of("date", dateTime.toLocalDate(), "time", dateTime.toLocalTime()))
                        .narration(r.getDescription());
                BigDecimal amount = r.getAmount();
                if (Type.EXPENSE == r.getType()) {
                    amount = amount.negate();
                }
                String myAccount = null;
                String peerAccount = null;

                if (r.getDescription().startsWith("退款-") && Type.NOT_APPLICABLE == r.getType()) {
                    var context = reverseTxn.get(r.getOrderNo());
                    if (nonNull(context)) {
                        var txn = context.txn();
                        var postings = txn.getPostings();
                        for (Posting p : postings) {
                            if (isNull(myAccount)) {
                                myAccount = p.getAccount();
                            } else {
                                peerAccount = p.getAccount();
                            }
                        }
                    }
                }
                if (isNull(myAccount)) {
                    List<AccountType> accountTypes = List.of(AccountType.ASSETS, AccountType.LIABILITIES);
                    for (AccountType accountType : accountTypes) {
                        myAccount = AccountMapping.account(accountType, r.getMethod());
                        if (nonNull(myAccount)) {
                            break;
                        }
                    }
                }
                if (isNull(myAccount)) {
                    throw new IllegalStateException(r.getMethod());
                }
                Posting myPosting = Posting.builder().account(myAccount).amount(amount).build();

                if (isNull(peerAccount)) {

                    List<AccountType> accountTypes = List.of(AccountType.EXPENSES, AccountType.ASSETS, AccountType.LIABILITIES);
                    List<String> searchList = new ArrayList<>();
                    searchList.addAll(segment(r.getCategory()));
                    searchList.addAll(segment(r.getDescription()));
                    for (String search : searchList) {
                        var op = accountTypes.stream().map(at -> AccountMapping.account(at, search)).filter(Objects::nonNull).findFirst();
                        if (op.isPresent()) {
                            peerAccount = op.get();
                            break;
                        }
                    }
                }
                if (isNull(peerAccount)) {
                    throw new IllegalStateException(r.getCategory());
                }
                Posting peerPosting = Posting.builder().account(peerAccount).amount(amount.negate()).build();
                tb.postings(List.of(myPosting, peerPosting));

                Transaction txn = tb.build();

                // Reverse Txn
                var amountText = r.getAmountText();
                if (nonNull(amountText) && Type.EXPENSE == r.getType()) {
                    var m = REVERSE_PATTERN.matcher(amountText);
                    if (m.find()) {
                        var reverseAmount = new BigDecimal(m.group(2));
                        reverseTxn.put(r.getOrderNo(), new ReverseContext(txn, reverseAmount));
                    }
                }

                System.out.println(txn.toString());
            }
        }
    }

    private record ReverseContext(Transaction txn, BigDecimal amount) {
    }

    @Override
    public boolean support(Path path) {
        var filename = path.getFileName().toString();
        return filename.startsWith("京东交易流水") && filename.endsWith(getFileExtension());
    }
}
