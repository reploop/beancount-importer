package org.reploop.beancount.meituan;

import org.reploop.beancount.AbstractRecordConverter;

import java.util.Set;

public class MeiTuanRecordConverter extends AbstractRecordConverter<MeiTuanRecord> {
    @Override
    protected Set<String> peerSearchList(MeiTuanRecord r) {
        return Set.of();
    }
}
