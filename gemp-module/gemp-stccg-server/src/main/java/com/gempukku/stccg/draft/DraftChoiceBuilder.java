package com.gempukku.stccg.draft;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.TextUtils;
import com.gempukku.stccg.async.handler.SortAndFilterCards;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.GenericCardItem;
import com.gempukku.stccg.collection.CardCollection;
import com.gempukku.stccg.collection.CollectionsManager;
import com.gempukku.stccg.collection.DefaultCardCollection;
import com.gempukku.stccg.common.JsonUtils;
import com.gempukku.stccg.formats.FormatLibrary;

import java.util.*;

public class DraftChoiceBuilder {
    public static final int HIGH_ENOUGH_PRIME_NUMBER = 9497;
    private final CollectionsManager _collectionsManager;
    private final CardBlueprintLibrary _cardLibrary;
    private final FormatLibrary _formatLibrary;

    public DraftChoiceBuilder(CollectionsManager collectionsManager, CardBlueprintLibrary cardLibrary,
                              FormatLibrary formatLibrary) {
        _collectionsManager = collectionsManager;
        _cardLibrary = cardLibrary;
        _formatLibrary = formatLibrary;
    }

    public DraftChoiceDefinition buildDraftChoiceDefinition(JsonNode choiceDefinition) {
        String choiceDefinitionType = choiceDefinition.get("type").textValue();
        JsonNode data = choiceDefinition.get("data");
        return switch (choiceDefinitionType) {
            case "singleCollectionPick" -> buildSingleCollectionPickDraftChoiceDefinition(data);
            case "weightedSwitch" -> buildWeightedSwitchDraftChoiceDefinition(data);
            case "multipleCardPick" -> buildMultipleCardPickDraftChoiceDefinition(data);
            case "randomSwitch" -> buildRandomSwitchDraftChoiceDefinition(data);
            case "filterPick" -> buildFilterPickDraftChoiceDefinition(data);
            case "draftPoolFilterPick" -> buildDraftPoolFilterPickDraftChoiceDefinition(data);
            case "draftPoolFilterPluck" -> buildDraftPoolFilterPluckDraftChoiceDefinition(data);
            default -> throw new RuntimeException("Unknown choiceDefinitionType: " + choiceDefinitionType);
        };
    }


    private DraftChoiceDefinition buildFilterPickDraftChoiceDefinition(JsonNode data) {
        final int optionCount = data.get("optionCount").asInt();
        String filter = data.get("filter").textValue().replace(" ","|");

        final List<GenericCardItem> possibleCards = SortAndFilterCards.process(
                filter, _collectionsManager.getCompleteCardCollection().getAll(), _cardLibrary, _formatLibrary);

        return new DraftChoiceDefinition() {
            @Override
            public Iterable<SoloDraft.DraftChoice> getDraftChoice(long seed, int stage,
                                                                  DefaultCardCollection draftPool) {
                final List<GenericCardItem> cards = getCards(seed, stage);

                List<SoloDraft.DraftChoice> draftChoices = new ArrayList<>(optionCount);
                for (int i = 0; i < Math.min(optionCount, possibleCards.size()); i++) {
                    final int finalI = i;
                    draftChoices.add(
                            new SoloDraft.DraftChoice() {
                                @Override
                                public String getChoiceId() {
                                    return cards.get(finalI).getBlueprintId();
                                }

                                @Override
                                public String getBlueprintId() {
                                    return cards.get(finalI).getBlueprintId();
                                }

                                @Override
                                public String getChoiceUrl() {
                                    return null;
                                }
                            });
                }
                return draftChoices;
            }

            @Override
            public CardCollection getCardsForChoiceId(String choiceId, long seed, int stage) {
                DefaultCardCollection cardCollection = new DefaultCardCollection();
                cardCollection.addItem(choiceId, 1);
                return cardCollection;
            }

            private List<GenericCardItem> getCards(long seed, int stage) {
                return TextUtils.getRandomizedList(possibleCards, getRandom(seed, stage));
            }
        };
    }

    private DraftChoiceDefinition buildSingleCollectionPickDraftChoiceDefinition(JsonNode data) {
        List<JsonNode> switchResult = JsonUtils.toArray(data.get("possiblePicks"));
        final Map<String, List<String>> cardsMap = new HashMap<>();
        final List<SoloDraft.DraftChoice> draftChoices = new ArrayList<>();

        for (JsonNode pickDefinition : switchResult) {
            final String choiceId = pickDefinition.get("choiceId").textValue();
            final String url = pickDefinition.get("url").textValue();
            List<String> cardIds = JsonUtils.toStringArray(pickDefinition.get("cards"));

            draftChoices.add(
                    new SoloDraft.DraftChoice() {
                        @Override
                        public String getChoiceId() {
                            return choiceId;
                        }

                        @Override
                        public String getBlueprintId() {
                            return null;
                        }

                        @Override
                        public String getChoiceUrl() {
                            return url;
                        }
                    });
            cardsMap.put(choiceId, cardIds);
        }

        return new DraftChoiceDefinition() {
            @Override
            public Iterable<SoloDraft.DraftChoice> getDraftChoice(long seed, int stage,
                                                                  DefaultCardCollection draftPool) {
                return draftChoices;
            }

            @Override
            public CardCollection getCardsForChoiceId(String choiceId, long seed, int stage) {
                List<String> cardIds = cardsMap.get(choiceId);
                DefaultCardCollection cardCollection = new DefaultCardCollection();
                if (cardIds != null)
                    for (String cardId : cardIds)
                        cardCollection.addItem(cardId, 1);

                return cardCollection;
            }
        };
    }


    private DraftChoiceDefinition buildMultipleCardPickDraftChoiceDefinition(JsonNode data) {
        final int count = data.get("count").asInt();
        List<String> cards = JsonUtils.toStringArray(data.get("availableCards"));

        return new DraftChoiceDefinition() {
            @Override
            public Iterable<SoloDraft.DraftChoice> getDraftChoice(long seed, int stage,
                                                                  DefaultCardCollection draftPool) {
                final List<String> shuffledCards = getShuffledCards(seed, stage);

                List<SoloDraft.DraftChoice> eligibleCards = new ArrayList<>(count);
                for (int i = 0; i < count; i++) {
                    final int finalI = i;
                    eligibleCards.add(
                            new SoloDraft.DraftChoice() {
                                @Override
                                public String getChoiceId() {
                                    return shuffledCards.get(finalI);
                                }

                                @Override
                                public String getBlueprintId() {
                                    return shuffledCards.get(finalI);
                                }

                                @Override
                                public String getChoiceUrl() {
                                    return null;
                                }
                            });
                }
                return eligibleCards;
            }

            @Override
            public CardCollection getCardsForChoiceId(String choiceId, long seed, int stage) {
                List<String> shuffledCards = getShuffledCards(seed, stage);

                for (int i = 0; i < count; i++) {
                    if (shuffledCards.get(i).equals(choiceId)) {
                        DefaultCardCollection result = new DefaultCardCollection();
                        result.addItem(choiceId, 1);
                        return result;
                    }
                }

                return new DefaultCardCollection();
            }

            private List<String> getShuffledCards(long seed, int stage) {
                return TextUtils.getRandomizedList(cards, getRandom(seed, stage));
            }
        };
    }


    private DraftChoiceDefinition buildDraftPoolFilterPickDraftChoiceDefinition(JsonNode data) {
        final int optionCount = data.get("optionCount").asInt();
        String filter = data.get("filter").textValue();

        return new DraftChoiceDefinition() {
            @Override
            public Iterable<SoloDraft.DraftChoice> getDraftChoice(long seed, int stage,
                                                                  DefaultCardCollection draftPool) {

                List<GenericCardItem> possibleCards = SortAndFilterCards.process(
                        filter, draftPool.getAll(), _cardLibrary, _formatLibrary);

                final List<GenericCardItem> cards = getCards(seed, stage, possibleCards);

                List<SoloDraft.DraftChoice> draftChoices = new ArrayList<>(optionCount);
                for (int i = 0; i < Math.min(optionCount, possibleCards.size()); i++) {
                    final int finalI = i;
                    draftChoices.add(
                            new SoloDraft.DraftChoice() {
                                @Override
                                public String getChoiceId() {
                                    return cards.get(finalI).getBlueprintId();
                                }

                                @Override
                                public String getBlueprintId() {
                                    return cards.get(finalI).getBlueprintId();
                                }

                                @Override
                                public String getChoiceUrl() {
                                    return null;
                                }
                            });
                }
                return draftChoices;
            }

            @Override
            public CardCollection getCardsForChoiceId(String choiceId, long seed, int stage) {
                DefaultCardCollection cardCollection = new DefaultCardCollection();
                cardCollection.addItem(choiceId, 1);
                return cardCollection;
            }

            private List<GenericCardItem> getCards(long seed, int stage, List<GenericCardItem> possibleCards) {
                Random rnd = getRandom(seed, stage);
                // Fixing some weird issue with Random
                rnd.nextInt();
                Collections.shuffle(possibleCards, rnd);
                return possibleCards;
            }
        };
    }

    private DraftChoiceDefinition buildDraftPoolFilterPluckDraftChoiceDefinition(JsonNode data) {
        final int optionCount = data.get("optionCount").asInt();
        String filter = data.get("filter").textValue();

        return new DraftChoiceDefinition() {
            @Override
            public Iterable<SoloDraft.DraftChoice> getDraftChoice(long seed, int stage,
                                                                  DefaultCardCollection draftPool) {
                List<GenericCardItem> fullDraftPool = new ArrayList<>();
                for (GenericCardItem item : draftPool.getAll())
                    for (int i = 0; i < draftPool.getItemCount(item.getBlueprintId()); i++)
                        fullDraftPool.add(item);

                List<GenericCardItem> possibleCards =
                        SortAndFilterCards.process(filter, fullDraftPool, _cardLibrary, _formatLibrary);

                final List<GenericCardItem> cards = getCards(seed, stage, possibleCards);

                List<SoloDraft.DraftChoice> draftChoices = new ArrayList<>(optionCount);
                for (int i = 0; i < Math.min(optionCount, possibleCards.size()); i++) {
                    draftPool.removeItem(cards.get(i).getBlueprintId(),1);
                    final int finalI = i;
                    draftChoices.add(
                            new SoloDraft.DraftChoice() {
                                @Override
                                public String getChoiceId() {
                                    return cards.get(finalI).getBlueprintId();
                                }

                                @Override
                                public String getBlueprintId() {
                                    return cards.get(finalI).getBlueprintId();
                                }

                                @Override
                                public String getChoiceUrl() {
                                    return null;
                                }
                            });
                }
                return draftChoices;
            }

            @Override
            public CardCollection getCardsForChoiceId(String choiceId, long seed, int stage) {
                DefaultCardCollection cardCollection = new DefaultCardCollection();
                cardCollection.addItem(choiceId, 1);
                return cardCollection;
            }

            private List<GenericCardItem> getCards(long seed, int stage, List<GenericCardItem> possibleCards) {
                Random rnd = getRandom(seed, stage);
                // Fixing some weird issue with Random
                rnd.nextInt();
                Collections.shuffle(possibleCards, rnd);
                return possibleCards;
            }
        };
    }


    private DraftChoiceDefinition buildRandomSwitchDraftChoiceDefinition(JsonNode data) {
        List<JsonNode> switchResult = JsonUtils.toArray(data.get("switchResult"));

        final List<DraftChoiceDefinition> draftChoiceDefinitionList = new ArrayList<>();
        for (JsonNode switchResultObject : switchResult)
            draftChoiceDefinitionList.add(buildDraftChoiceDefinition(switchResultObject));

        return new DraftChoiceDefinition() {
            @Override
            public Iterable<SoloDraft.DraftChoice> getDraftChoice(long seed, int stage,
                                                                  DefaultCardCollection draftPool) {
                return TextUtils.getRandomFromList(draftChoiceDefinitionList, getRandom(seed, stage))
                        .getDraftChoice(seed, stage, draftPool);
            }

            @Override
            public CardCollection getCardsForChoiceId(String choiceId, long seed, int stage) {
                return TextUtils.getRandomFromList(draftChoiceDefinitionList, getRandom(seed, stage))
                        .getCardsForChoiceId(choiceId, seed, stage);
            }
        };
    }

    private DraftChoiceDefinition buildWeightedSwitchDraftChoiceDefinition(JsonNode data) {
        List<JsonNode> switchResult = JsonUtils.toArray(data.get("switchResult"));

        final Map<Float, DraftChoiceDefinition> draftChoiceDefinitionMap = new LinkedHashMap<>();
        float weightTotal = 0;
        for (JsonNode switchResultObject : switchResult) {
            float weight = switchResultObject.get("weight").floatValue();
            weightTotal += weight;
            draftChoiceDefinitionMap.put(weightTotal, buildDraftChoiceDefinition(switchResultObject));
        }

        return new DraftChoiceDefinition() {
            @Override
            public Iterable<SoloDraft.DraftChoice> getDraftChoice(long seed, int stage,
                                                                  DefaultCardCollection draftPool) {
                Random rnd = getRandom(seed, stage);
                float result = rnd.nextFloat();
                for (Map.Entry<Float, DraftChoiceDefinition> weightEntry : draftChoiceDefinitionMap.entrySet()) {
                    if (result < weightEntry.getKey())
                        return weightEntry.getValue().getDraftChoice(seed, stage, draftPool);
                }

                return null;
            }

            @Override
            public CardCollection getCardsForChoiceId(String choiceId, long seed, int stage) {
                Random rnd = getRandom(seed, stage);
                float result = rnd.nextFloat();
                for (Map.Entry<Float, DraftChoiceDefinition> weightEntry : draftChoiceDefinitionMap.entrySet()) {
                    if (result < weightEntry.getKey())
                        return weightEntry.getValue().getCardsForChoiceId(choiceId, seed, stage);
                }

                return null;
            }
        };
    }


    private Random getRandom(long seed, int stage) {
        return new Random(seed + (long) stage * HIGH_ENOUGH_PRIME_NUMBER);
    }
}