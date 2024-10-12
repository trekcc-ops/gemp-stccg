package com.gempukku.stccg.draft;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.collection.CardCollection;
import com.gempukku.stccg.collection.DefaultCardCollection;
import com.gempukku.stccg.collection.MutableCardCollection;
import com.google.common.collect.Iterables;

import java.util.*;
import java.util.random.RandomGenerator;

public class StartingPoolBuilder {
    public static CardCollectionProducer buildCardCollectionProducer(JsonNode startingPool) {
        if (startingPool == null)
            return null;
        String cardCollectionProducerType = startingPool.get("type").textValue();
        if ("randomCardPool".equals(cardCollectionProducerType)) {
            return buildRandomCardPool(startingPool.get("data"));
        } else if ("boosterDraftRun".equals(cardCollectionProducerType)) {
            return buildBoosterDraftRun(startingPool.get("data"));
        }
        throw new RuntimeException("Unknown cardCollectionProducer type: " + cardCollectionProducerType);
    }

    private static CardCollectionProducer buildRandomCardPool(JsonNode randomCardPool) {
        final List<CardCollection> cardCollections = new ArrayList<>();
        if (randomCardPool.get("randomResult").isArray()) {
            for (JsonNode cards : randomCardPool.get("randomResult")) {
                MutableCardCollection cardCollection = new DefaultCardCollection();
                for (JsonNode card : cards) {
                    cardCollection.addItem(card.textValue(), 1);
                }
                cardCollections.add(cardCollection);
            }
        } else {
            MutableCardCollection cardCollection = new DefaultCardCollection();
            for (JsonNode card :randomCardPool.get("randomResult")) {
                cardCollection.addItem(card.textValue(), 1);
            }
            cardCollections.add(cardCollection);
        }
        return seed -> {
            RandomGenerator rnd = new Random(seed);
            rnd.nextFloat();
            return cardCollections.get(rnd.nextInt(cardCollections.size()));
        };
    }

    private static CardCollectionProducer buildBoosterDraftRun(JsonNode boosterDraftRun) {
        final int runLength = boosterDraftRun.get("runLength").asInt();
        final Collection<String> coreCards = new LinkedList<>();
        final List<JsonNode> freePeoplesRuns = new LinkedList<>();
        final List<JsonNode> shadowRuns = new LinkedList<>();

        for (JsonNode card : boosterDraftRun.get("coreCards")) coreCards.add(card.textValue());
        for (JsonNode run : boosterDraftRun.get("freePeoplesRuns")) freePeoplesRuns.add(run);
        for (JsonNode run : boosterDraftRun.get("shadowRuns")) shadowRuns.add(run);

        return seed -> {
            RandomGenerator rnd = new Random(seed);
            JsonNode freePeoplesRunNode = freePeoplesRuns.get(rnd.nextInt(freePeoplesRuns.size()));
            JsonNode shadowRunNode = shadowRuns.get(rnd.nextInt(shadowRuns.size()));

            Collection<String> freePeoplesRun = new LinkedList<>();
            Collection<String> shadowRun = new LinkedList<>();

            for (JsonNode card : freePeoplesRunNode)
                freePeoplesRun.add(card.textValue());
            for (JsonNode card : shadowRunNode)
                shadowRun.add(card.textValue());

            Iterable<String> freePeopleIterable =
                    getCyclingIterable(freePeoplesRun, rnd.nextInt(freePeoplesRun.size()), runLength);
            Iterable<String> shadowIterable =
                    getCyclingIterable(shadowRun, rnd.nextInt(shadowRun.size()), runLength);

            final MutableCardCollection startingCollection = new DefaultCardCollection();

            for (String card : Iterables.concat(coreCards, freePeopleIterable, shadowIterable))
                startingCollection.addItem(card, 1);

            return startingCollection;
        };
    }


    private static Iterable<String> getCyclingIterable(Iterable<String> list, int start, int length) {
        return Iterables.limit(Iterables.skip(Iterables.cycle(list), start), length);
    }
}