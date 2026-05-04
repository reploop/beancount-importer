package org.reploop.beancount;

import java.util.function.BiConsumer;
import java.util.function.Function;

public record Point<T, E, V>(Integer index, String name, Function<T, V> getter, BiConsumer<E, V> setter) {

    public void process(T t, E e) {
        V v = getter.apply(t);
        setter.accept(e, v);
    }
}
