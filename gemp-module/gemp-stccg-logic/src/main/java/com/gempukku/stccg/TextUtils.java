package com.gempukku.stccg;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

public class TextUtils {

    public static <T> T getRandomItemFromList(Collection<? extends T> list) {
        return getRandomItemsFromList(list, 1).getFirst();
    }


    public static <T> List<T> getRandomItemsFromList(Collection<? extends T> list, int count) {
        List<T> randomizedList = getRandomizedList(list, ThreadLocalRandom.current());
        return new LinkedList<>(randomizedList.subList(0, Math.min(count, randomizedList.size())));
    }

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

    public static <T> List<T> getRandomizedList(Collection<? extends T> list, ThreadLocalRandom random) {
        List<T> randomizedList = new ArrayList<>(list);
        Collections.shuffle(randomizedList, random);
        return randomizedList;
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

    public static String plural(int count, String noun) {
        return count + " " + ((count == 1) ? noun : getPluralNoun(noun));
    }

    public static String getPluralNoun(String noun) {
        return switch(noun) {
            case "series", "personnel" -> noun;
            default -> noun + "s";
        };
    }

    public static <T> String be(Collection<T> collection) { return collection.size() > 1 ? "are" : "is"; }

    public static String concatenateStrings(Stream<String> strings) {
        return concatenateStrings(strings.toList());
    }

    public static String concatenateStrings(Collection<String> strings) {
        if (strings.isEmpty())
            return "none";
        else return String.join(", ", strings);
    }


    public static String getConcatenatedCardLinks(Collection<? extends PhysicalCard> cards) {
        StringJoiner sj = new StringJoiner(", ");
        for (PhysicalCard card : cards)
            sj.add(card.getCardLink());
        if (sj.length() == 0)
            return "none";
        else
            return sj.toString();
    }

}