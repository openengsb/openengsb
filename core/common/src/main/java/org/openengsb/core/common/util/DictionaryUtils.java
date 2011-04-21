package org.openengsb.core.common.util;

import java.util.Dictionary;
import java.util.Hashtable;

public final class DictionaryUtils {

    public static <K, V> Dictionary<K, V> copy(Dictionary<K, V> original) {
        return new Hashtable<K, V>(DictionaryAsMap.wrap(original));
    }

    private DictionaryUtils() {
    }

}
