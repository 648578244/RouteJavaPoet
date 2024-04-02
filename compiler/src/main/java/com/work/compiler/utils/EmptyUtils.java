package com.work.compiler.utils;

import java.util.Collection;
import java.util.Map;

import javax.lang.model.type.TypeKind;

public class EmptyUtils {
    public static boolean isEmpty(CharSequence cs){
        return cs == null || cs.length() == 0;
    }
    public static boolean isEmpty(Collection<?> coll){
        return coll == null || coll.isEmpty();
    }
    public static boolean isEmpty(final Map<?,?> map){
        return map == null || map.isEmpty();
    }
}
