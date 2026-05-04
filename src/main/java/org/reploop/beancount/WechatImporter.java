package org.reploop.beancount;

import org.reploop.beancount.account.AccountMapping;
import org.reploop.beancount.account.AccountType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public abstract class WechatImporter extends BillImporter<WechatRecord> {

    private static final Pattern PART_REVERSE_PATTERN = Pattern.compile("已退款\\((¥[\\d.]+)\\)");

    public List<Transaction> convert(List<WechatRecord> records, Map<ReverseKey, Transaction> reverseTransactions) throws Exception {
        var transactions = new ArrayList<Transaction>();
        var list = records.stream().sorted(Comparator.comparing(WechatRecord::getCreatedAt)).toList();
        for (var r : list) {
            var type = r.getType();

            var dateTime = r.createdAt;
            var builder = Transaction.builder();
            if (r.getGoods().startsWith("订单编号")) {
                r.setGoods(r.getPeer());
            }
            builder.payee(r.getPeer())
                    .flag(Flag.CLOSED)
                    .dateTime(r.createdAt)
                    .narration(r.getGoods())
                    .meta(Map.of("date", dateTime.toLocalDate(), "time", dateTime.toLocalTime()));
            //method=/ and status=已存入零钱
            if (Objects.equals(r.getMethod(), "/") && Objects.equals(r.getStatus(), "已存入零钱")) {
                r.setMethod("零钱");
            }

            BigDecimal amount;
            switch (type) {
                case EXPENSE -> amount = r.getAmount().negate();
                case INCOME -> amount = r.getAmount();
                default -> throw new IllegalStateException(type.text);
            }

            // It's always your assets or liabilities
            String method = r.getMethod();
            String myAccount = null;
            String peerAccount = null;
            // If it's a reverse, just reuse the accounts
            if (Type.INCOME == type) {
                ReverseKey reverseKey = new ReverseKey(r.getStatus(), method);
                Transaction prev = reverseTransactions.remove(reverseKey);
                if (nonNull(prev)) {
                    // my, peer
                    var postings = prev.getPostings();
                    for (var posting : postings) {
                        // reuse account
                        if (isNull(myAccount)) {
                            myAccount = posting.getAccount();
                        } else {
                            peerAccount = posting.getAccount();
                        }
                    }
                    Objects.requireNonNull(peerAccount);
                    Objects.requireNonNull(myAccount);
                }
            }
            // Identify my account
            if (isNull(myAccount)) {
                EnumSet<AccountType> accountTypes = EnumSet.of(AccountType.ASSETS, AccountType.LIABILITIES);
                myAccount = accountTypes.stream()
                        .map(at -> AccountMapping.account(at, method))
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElseThrow((Supplier<RuntimeException>) () -> new IllegalStateException(method + " not found"));
            }
            // Then my posting
            Posting myPosting = Posting.builder()
                    .account(myAccount)
                    .amount(amount)
                    .build();
            // Identify peer's account
            if (isNull(peerAccount)) {
                if (Type.INCOME == type) {
                    var first = segment(r.getCategory()).stream().findFirst().orElse("");
                    if (Objects.equals("微信红包", first)) {
                        peerAccount = AccountMapping.account(AccountType.INCOME, first);
                    }
                }
                if (isNull(peerAccount)) {
                    var category = r.getCategory();
                    Set<String> searchList = new LinkedHashSet<>();
                    if (Objects.equals("扫二维码付款", category)) {
                        searchList.add("扫二维码付款");
                    }
                    searchList.add(r.getPeer());
                    searchList.add(r.getGoods());
                    searchList.add(r.getCategory());
                    searchList.addAll(segment(r.getPeer()));
                    searchList.addAll(segment(r.getGoods()));
                    searchList.addAll(segment(r.getCategory()));
                    Set<AccountType> accountTypes = Set.of(AccountType.EXPENSES, AccountType.LIABILITIES, AccountType.ASSETS);
                    for (String search : searchList) {
                        var op = accountTypes.stream().map(at -> AccountMapping.account(at, search))
                                .filter(Objects::nonNull)
                                .findFirst();
                        if (op.isPresent()) {
                            peerAccount = op.get();
                            break;
                        }
                    }
                    if (isNull(peerAccount)) {
                        throw new IllegalStateException(searchList + " not found");
                    }
                }
            }
            // Then peer's posting
            Posting peerPosting = Posting.builder()
                    .amount(amount.negate())
                    .account(peerAccount)
                    .build();
            builder.postings(List.of(myPosting, peerPosting));
            var txn = builder.build();
            transactions.add(txn);

            // 部分退款
            //收入:已退款¥136.80
            //支出:已退款(¥136.80)
            // 已全额退款
            //收入:已全额退款
            //支出:已全额退款
            if (type == Type.EXPENSE) {
                var status = r.getStatus();
                if (Objects.equals("已全额退款", status)) {
                    reverseTransactions.put(new ReverseKey(status, method), txn);
                } else if (status.startsWith("已退款")) {
                    var m = PART_REVERSE_PATTERN.matcher(status);
                    if (m.find()) {
                        status = "已退款" + m.group(1);
                        reverseTransactions.put(new ReverseKey(status, method), txn);
                    }
                }
            }
        }
        System.out.println();
        for (var txn : transactions) {
            System.out.println(txn);
        }
        return transactions;
    }

    private List<String> segment(String val) {
        var values = val.split("[\\s-_&（）·:，|]+");
        return Arrays.stream(values).map(String::trim).sorted((o1, o2) -> Integer.compare(o2.length(), o1.length())).toList();
    }
}
