package com.gempukku.stccg.rules;

import com.gempukku.stccg.cards.PhysicalCard;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

public class GameUtils {

    public static <T> void addAllNotNull(List<T> list, List<? extends T> possiblyNullList) {
        if (possiblyNullList != null)
            list.addAll(possiblyNullList);
    }

    public static <T> List<T> getRandomFromList(List<? extends T> list, int count) {
        List<T> randomizedList = new ArrayList<>(list);
        Collections.shuffle(randomizedList, ThreadLocalRandom.current());
        return new LinkedList<>(randomizedList.subList(0, Math.min(count, randomizedList.size())));
    }

    public static String plural(int count, String noun) {
        StringBuilder sb = new StringBuilder();
        sb.append(count).append(" ").append(noun);
        if (count != 1)
            sb.append("s");
        return sb.toString();
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

}