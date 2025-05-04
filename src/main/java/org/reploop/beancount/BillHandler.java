package org.reploop.beancount;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public abstract class BillHandler<R> extends DefaultHandler {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String TD = "td";
    private static final String TR = "tr";

    private final List<R> records;
    private final Map<Integer, Header> indexedHeader = new HashMap<>();
    private R record;
    private int columnIndex = 0;
    private int rowIndex = 0;
    /**
     * Matched header row index.
     */
    private int matches = Integer.MAX_VALUE;

    public BillHandler(List<R> records) {
        this.records = records;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (TR.equals(qName)) {
            record = newInstance();
            // reset column index
            columnIndex = 0;
        }
    }

    protected abstract R newInstance();

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        var text = new String(ch, start, length).trim();
        Header header;
        if (rowIndex <= matches && nonNull(header = Header.findByText(text))) {
            matches = rowIndex;
            indexedHeader.put(columnIndex, header);
        }
        if (rowIndex > matches && nonNull(record)) {
            header = indexedHeader.get(columnIndex);
            if (isNull(header)) {
                throw new IllegalStateException(text);
            }
            BiConsumer<R, String> consumer = getSetter(header);
            if (nonNull(consumer)) {
                consumer.accept(record, text);
            }
        }
    }

    Type parse(String text) {
        return switch (text) {
            case "不计收支", "收入" -> Type.INGRESS;
            case "支出" -> Type.EGRESS;
            default -> throw new IllegalStateException(text);
        };
    }

    protected abstract BiConsumer<R, String> getSetter(Header header);

    protected boolean validate(R r) {
        return nonNull(r);
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        switch (qName) {
            case TR -> {
                if (validate(record)) {
                    records.add(record);
                }
                // increment row index
                rowIndex++;
            }
            case TD -> columnIndex++;
        }
    }
}
