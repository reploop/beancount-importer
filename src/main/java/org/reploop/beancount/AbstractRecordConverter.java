package org.reploop.beancount;

import org.reploop.beancount.account.AccountMapping;
import org.reploop.beancount.account.AccountType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public abstract class AbstractRecordConverter<R extends BillRecord> implements RecordConverter<R> {
    public void start(R r) {
    }

    protected ReverseContext reverseContext(R r, Map<ReverseKey, ReverseContext> contexts) {
        return null;
    }

    protected boolean test(R r) {
        return true;
    }

    @Override
    public List<Transaction> convert(List<R> records, Map<ReverseKey, ReverseContext> reverses) {
        var transactions = new ArrayList<Transaction>();
        var list = records.stream().sorted(Comparator.comparing(R::getCreatedAt)).toList();
        for (var r : list) {
            if (!test(r)) {
                continue;
            }
            start(r);
            var dateTime = r.getCreatedAt();
            var builder = Transaction.builder();
            builder.payee(r.getPeer())
                    .flag(Flag.CLOSED)
                    .dateTime(r.getCreatedAt())
                    .narration(r.getGoods())
                    .meta(Map.of("time", dateTime.toLocalTime()));
            BigDecimal amount = r.getAmount();
            var type = r.getType();
            if (Type.EXPENSE == type) {
                amount = amount.negate();
            }
            // It's always your assets or liabilities
            String myAccount = null;
            String peerAccount = null;
            // If it's a reverse, just reuse the accounts
            String method = r.getMethod();
            // If there is refund
            ReverseContext reverseContext;
            if (Type.EXPENSE != type && nonNull(reverseContext = reverseContext(r, reverses))) {
                Transaction prev;
                if (nonNull(prev = reverseContext.getTxn())) {
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
                myAccount = search(accountTypes, mySearchList(r));
                if (isNull(myAccount)) {
                    throw new IllegalStateException(method);
                }
            }
            // Then my posting
            Posting myPosting = Posting.builder()
                    .account(myAccount)
                    .amount(amount)
                    .build();
            // Identify peer's account
            if (isNull(peerAccount)) {
                if (Type.INCOME == type) {
                    peerAccount = search(List.of(AccountType.INCOME), incomeSearchList(r));
                }
                if (isNull(peerAccount)) {
                    Set<String> searchList = peerSearchList(r);
                    List<AccountType> accountTypes = List.of(AccountType.EXPENSES, AccountType.LIABILITIES, AccountType.INCOME, AccountType.ASSETS);
                    peerAccount = search(accountTypes, searchList);
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
            reverse(r, txn, reverses);
        }
        return transactions;
    }

    String search(Collection<AccountType> accountTypes, Collection<String> searchList) {
        for (String search : searchList) {
            var op = accountTypes.stream().map(at -> AccountMapping.account(at, search))
                    .filter(Objects::nonNull)
                    .findFirst();
            if (op.isPresent()) {
                return op.get();
            }
        }
        return null;
    }

    protected void reverse(R r, Transaction txn, Map<ReverseKey, ReverseContext> reverseMap) {
    }

    protected Set<String> incomeSearchList(R r) {
        return Collections.emptySet();
    }

    protected abstract Set<String> peerSearchList(R r);

    protected Set<String> mySearchList(R r) {
        return Set.of(r.getMethod());
    }
}
