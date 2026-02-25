package com.gempukku.stccg;

import java.util.*;

public class TextUtils {


    public static <T> T getRandomItemsFromList(List<? extends T> list, Random random) {
        random.nextFloat(); // This fixes random bug for some reason according to LotR Gemp comments
        return list.get(random.nextInt(list.size()));
    }

    public static <T> List<T> getRandomizedList(List<? extends T> list, Random random) {
        random.nextFloat(); // This fixes random bug for some reason according to LotR Gemp comments
        List<T> newList = new ArrayList<>(list);
        Collections.shuffle(newList, random);
        return newList;
    }

    public static String getAllCharacters(boolean includeUpper, boolean includeDashAndUnderscore){
        StringBuilder sb = new StringBuilder();
        sb.append("abcdefghijklmnopqrstuvwxyz0123456789");
        if (includeUpper) sb.append("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        if (includeDashAndUnderscore) sb.append("-_");
        return sb.toString();
    }

    public static String signed(float value) {
        return (value >= 0 ? "+" : "") + value;
    }

    public static String concatenateStrings(Collection<String> strings) {
        if (strings.isEmpty())
            return "none";
        else return String.join(", ", strings);
    }


}