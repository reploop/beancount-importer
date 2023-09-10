package org.reploop.beancount;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

import static java.util.Objects.nonNull;

public abstract class BillHandler<R> extends DefaultHandler {
    private static final String TD = "td";
    private static final String TR = "tr";
    private static final String TABLE = "table";

    private final List<R> records;
    private final List<String> headers;
    private final Map<Integer, BiConsumer<R, String>> setters;

    public BillHandler(List<R> records, List<String> headers, Map<Integer, BiConsumer<R, String>> setters) {
        this.records = records;
        this.setters = setters;
        this.headers = headers;
    }

    private R record;
    private int index = 0;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (TR.equals(qName)) {
            record = newInstance();
            index = 0;
        }
    }

    protected abstract R newInstance();

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        var text = new String(ch, start, length).trim();
        var header = headers.get(index);
        if (!Objects.equals(header, text) && nonNull(record)) {
            BiConsumer<R, String> consumer = setters.get(index);
            if (nonNull(consumer)) {
                consumer.accept(record, text);
            }
        }
    }

    protected boolean validate(R r) {
        return nonNull(r);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        switch (qName) {
            case TR -> {
                if (index >= setters.size() && validate(record)) {
                    records.add(record);
                }
            }
            case TD -> index++;
        }
    }
}
