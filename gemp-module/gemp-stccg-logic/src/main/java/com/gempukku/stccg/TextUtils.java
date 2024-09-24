package com.gempukku.stccg;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

public class TextUtils {

    public static <T> List<T> getRandomFromList(List<? extends T> list, int count) {
        List<T> randomizedList = new ArrayList<>(list);
        Collections.shuffle(randomizedList, ThreadLocalRandom.current());
        return new LinkedList<>(randomizedList.subList(0, Math.min(count, randomizedList.size())));
    }

    public static String signed(int value) {
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
        StringJoiner sj = new StringJoiner(", ");
        for (String string : strings)
            sj.add(string);
        if (sj.length() == 0)
            return "none";
        else return sj.toString();
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

    public static String listToCommaSeparated(List<String> strings) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String participantId : strings) {
            if (!first) sb.append(",");
            sb.append(participantId);
            first = false;
        }
        return sb.toString();
    }

    public static String arrayToCommaSeparated(int[] integers) {
        int iMax = integers.length - 1;
        if (iMax == -1)
            return "";

        StringBuilder b = new StringBuilder();
        for (int i = 0; ; i++) {
            b.append(integers[i]);
            if (i == iMax)
                return b.toString();
            b.append(",");
        }
    }
}