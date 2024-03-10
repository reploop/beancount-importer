package org.reploop.beancount;

public enum Flag {
    OPEN("!"),
    CLOSED("*");
    private final String text;

    Flag(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
