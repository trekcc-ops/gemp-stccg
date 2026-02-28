package com.gempukku.stccg.game;

import com.gempukku.stccg.cards.cardgroup.CardPile;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class GameRandomizer {

    public <T> List<T> getRandomItemsFromList(Collection<? extends T> list, int count) {
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

    public void shuffleCardPile(CardPile<? extends PhysicalCard> cardPile) {
        cardPile.shuffle();
    }
}