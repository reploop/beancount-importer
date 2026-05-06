package org.reploop.beancount.wechat;

import org.reploop.beancount.BillImporter;
import org.reploop.beancount.ReverseContext;
import org.reploop.beancount.ReverseKey;
import org.reploop.beancount.Transaction;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public abstract class WechatImporter implements BillImporter {

    private final WechatRecordConverter converter = new WechatRecordConverter();

    public List<Transaction> convert(List<WechatRecord> records, Map<ReverseKey, ReverseContext> reverseTransactions) throws Exception {
        return converter.convert(records, reverseTransactions);
    }

    @Override
    public boolean support(Path path) {
        var filename = path.getFileName().toString();
        return filename.startsWith("微信支付账单") && supportExtension(filename);
    }

    abstract boolean supportExtension(String filename);
}
