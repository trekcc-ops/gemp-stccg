package com.gempukku.stccg.draft;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.TextUtils;
import com.gempukku.stccg.collection.CardCollection;
import com.gempukku.stccg.collection.DefaultCardCollection;

import java.util.*;

public class DefaultSoloDraft implements SoloDraft {
    private final String _code;
    private final String _format;
    private final CardCollectionProducer _newCollection;
    private final DraftPoolProducer _draftPool;
    private final List<? extends DraftChoiceDefinition> _draftChoiceDefinitions;
    public record RepeatedDefinition(int repeat, DraftChoiceDefinition choice) { }

    private DefaultSoloDraft(
            @JsonProperty("startingPool")
            CardCollectionProducer startingPoolProducer,
            @JsonProperty("draftPool")
            JsonNode draftPoolNode,
            @JsonProperty("choices")
            List<RepeatedDefinition> definitions,
            @JsonProperty("code")
            String code,
            @JsonProperty("format")
            String format
    ) {
        _newCollection = startingPoolProducer;
        _draftPool = buildDraftPoolProducer(draftPoolNode);
        _code = code;
        _format = format;
        List<DraftChoiceDefinition> draftChoiceDefinitions = new ArrayList<>();
        for (RepeatedDefinition repeated : definitions) {
            for (int i = 0; i < repeated.repeat; i++)
                draftChoiceDefinitions.add(repeated.choice);
        }
        _draftChoiceDefinitions = draftChoiceDefinitions;
    }

    @Override
    public CardCollection initializeNewCollection(long seed) {
        return (_newCollection != null) ? _newCollection.getCardCollection(seed) : null;
    }

    @Override
    public List<String> initializeDraftPool(long seed, long code) {
        return (_draftPool != null) ? _draftPool.getDraftPool(seed, code) : null;
    }

    @Override
    public Iterable<DraftChoice> getAvailableChoices(long seed, int stage, DefaultCardCollection draftPool) {
        return _draftChoiceDefinitions.get(stage).getDraftChoice(seed, stage, draftPool);
    }


    @Override
    public CardCollection getCardsForChoiceId(String choiceId, long seed, int stage)
            throws InvalidDraftResultException {
        return _draftChoiceDefinitions.get(stage).getCardsForChoiceId(choiceId, seed, stage);
    }

    @Override
    public boolean hasNextStage(int stage) {
        return stage + 1 < _draftChoiceDefinitions.size();
    }

    @Override
    public String getCode() {
        return _code;
    }

    @Override
    public String getFormat() {
        return _format;
    }

    public static class CardCollectionProducer {

        public enum ProducerType {
            randomCardPool, boosterDraftRun
        }

        private final ProducerType _producerType;
        private final List<CardCollection> cardCollections = new ArrayList<>();

        public CardCollectionProducer(@JsonProperty("type")
                               ProducerType producerType,
                               @JsonProperty("randomResult")
                               @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                               List<List<String>> randomResult
                               ) {
            _producerType = producerType;
            if (_producerType == ProducerType.randomCardPool && randomResult != null) {
                for (List<String> cardList : randomResult) {
                    DefaultCardCollection cardCollection = new DefaultCardCollection();
                    for (String blueprintId : cardList) {
                        cardCollection.addItem(blueprintId, 1);
                    }
                    cardCollections.add(cardCollection);
                }
            }
        }

        CardCollection getCardCollection(long seed) {
            if (_producerType == ProducerType.randomCardPool) {
                Random rnd = new Random(seed);
                rnd.nextFloat();
                return cardCollections.get(rnd.nextInt(cardCollections.size()));
            } else {
                return new DefaultCardCollection();
            }
        }
    }

    public static DraftPoolProducer buildDraftPoolProducer(JsonNode draftPoolComponents) {
        if (draftPoolComponents == null)
            return null;
        Collection<DraftPoolElement> fullDraftPool = new ArrayList<>();
        if (draftPoolComponents.isArray()) {
            for (JsonNode component : draftPoolComponents)
                fullDraftPool.add(buildDraftPool(component));
        } else {
            fullDraftPool.add(buildDraftPool(draftPoolComponents));
        }

        return (seed, code) -> {
            List<String> completedDraftPool = new ArrayList<>();
            Random randomSource = new Random();
            int mod = 0;

            for (DraftPoolElement element : fullDraftPool) {
                List<List<String>> draftPacks;
                draftPacks = element.getDraftPackList();
                if (Objects.equals(element.getDraftPoolType(), "singleDraft"))
                    randomSource = new Random(seed+mod);
                else if (Objects.equals(element.getDraftPoolType(), "sharedDraft"))
                    randomSource = new Random(code);
                mod++;

                draftPacks = TextUtils.getRandomizedList(draftPacks, randomSource);
                for (int i = 0; i < element.getPacksToDraft(); i++) {
                    completedDraftPool.addAll(draftPacks.get(i));
                }
            }
            return completedDraftPool;
        };
    }

    public static DraftPoolElement buildDraftPool(JsonNode draftPool) {
        String draftPoolProducerType = draftPool.get("type").textValue();
        if ("singleDraft".equals(draftPoolProducerType)) {
            return buildSingleOrSharedDraftPool(draftPool.get("data"));
        } else if ("sharedDraft".equals(draftPoolProducerType)) {
            return buildSingleOrSharedDraftPool(draftPool.get("data"));
        }
        throw new RuntimeException("Unknown draftPoolProducer type: " + draftPoolProducerType);
    }


    private static DefaultDraftPoolElement buildSingleOrSharedDraftPool(JsonNode data) {
        int choose = data.get("choose").asInt();
        JsonNode draftPackPool = data.get("packs");

        List<List<String>> draftPacks = new ArrayList<>();
        if (draftPackPool.isArray()) {
            for (JsonNode cards : draftPackPool) {
                List<String> draftPack = new ArrayList<>();
                for (JsonNode card : cards) {
                    draftPack.add(card.textValue());
                }
                draftPacks.add(draftPack);
            }
        } else {
            List<String> draftPack = new ArrayList<>();
            for (JsonNode card : draftPackPool) {
                draftPack.add(card.textValue());
            }
            draftPacks.add(draftPack);
        }

        return new DefaultDraftPoolElement("singleDraft", draftPacks, choose);
    }


}