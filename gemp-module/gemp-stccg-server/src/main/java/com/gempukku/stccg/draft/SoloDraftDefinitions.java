package com.gempukku.stccg.draft;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.collection.CollectionsManager;
import com.gempukku.stccg.common.AppConfig;
import com.gempukku.stccg.common.JsonUtils;
import com.gempukku.stccg.formats.FormatLibrary;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Semaphore;

public class SoloDraftDefinitions {
    private final Map<String, SoloDraft> _draftTypes = new HashMap<>();
    private final File _draftDefinitionPath = AppConfig.getDraftPath();
    private final Semaphore collectionReady = new Semaphore(1);
    private final CardBlueprintLibrary _cardLibrary;
    private final FormatLibrary _formatLibrary;
    private final CollectionsManager _collectionsManager;

    public SoloDraftDefinitions(CollectionsManager collectionsManager, CardBlueprintLibrary cardLibrary,
                                FormatLibrary formatLibrary) {
        _cardLibrary = cardLibrary;
        _formatLibrary = formatLibrary;
        _collectionsManager = collectionsManager;
        ReloadDraftsFromFile();
    }

    public void ReloadDraftsFromFile() {
        try {
            collectionReady.acquire();
            loadDrafts(_draftDefinitionPath);
            collectionReady.release();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadDrafts(File path) {
        if (path.isFile()) {
            loadDraft(path);
        }
        else if (path.isDirectory()) {
            for (File file : Objects.requireNonNull(path.listFiles())) {
                loadDrafts(file);
            }
        }
    }

    private void loadDraft(File file) {
        try {
            JsonNode node = JsonUtils.readJsonFromFile(file);
            String code = node.get("code").textValue();

            CardCollectionProducer cardCollectionProducer =
                    StartingPoolBuilder.buildCardCollectionProducer(node.get("startingPool"));
            DraftPoolProducer draftPoolProducer =
                    DraftPoolBuilder.buildDraftPoolProducer(node.get("draftPool"));

            List<DraftChoiceDefinition> draftChoiceDefinitions = new ArrayList<>();
            JsonNode choices = node.get("choices");
            if (choices.isArray()) {
                for (JsonNode choice : choices) {
                    DraftChoiceDefinition draftChoiceDefinition = buildDraftChoiceDefinition(choice);
                    int repeatCount = choice.get("repeat").asInt();
                    for (int i = 0; i < repeatCount; i++)
                        draftChoiceDefinitions.add(draftChoiceDefinition);
                }
            } else {
                DraftChoiceDefinition draftChoiceDefinition = buildDraftChoiceDefinition(choices);
                int repeatCount = choices.get("repeat").asInt();
                for (int i = 0; i < repeatCount; i++)
                    draftChoiceDefinitions.add(draftChoiceDefinition);
            }

            if(_draftTypes.containsKey(code))
                System.out.println("Duplicate draft loaded: " + code);

            _draftTypes.put(code, new DefaultSoloDraft(code, node.get("format").textValue(),
                    cardCollectionProducer, draftChoiceDefinitions, draftPoolProducer));

        } catch (IOException exp) {
            throw new RuntimeException("Problem loading solo draft " + file, exp);
        }
    }

    public SoloDraft getSoloDraft(String draftType) {
        try {
            collectionReady.acquire();
            var data = _draftTypes.get(draftType);
            collectionReady.release();
            return data;
        } catch (InterruptedException exp) {
            throw new RuntimeException("SoloDraftDefinitions.getSoloDraft() interrupted: ", exp);
        }
    }

    public DraftChoiceDefinition buildDraftChoiceDefinition(JsonNode choiceDefinition) {
        String choiceDefinitionType = choiceDefinition.get("type").textValue();
        JsonNode data = choiceDefinition.get("data");
        return switch (choiceDefinitionType) {
            case "singleCollectionPick" -> new SingleCollectionPickDraftChoiceDefinition(data);
            case "weightedSwitch" -> new WeightedSwitchDraftChoiceDefinition(data, this);
            case "multipleCardPick" -> new MultipleCardPickDraftChoiceDefinition(data);
            case "randomSwitch" -> new RandomSwitchDraftChoiceDefinition(data, this);
            case "filterPick" -> {
                FilterPickDraftChoiceDefinition definition = new FilterPickDraftChoiceDefinition(data);
                definition.assignPossibleCards(_collectionsManager, _cardLibrary, _formatLibrary);
                yield definition;
            }
            case "draftPoolFilterPick" ->
                    new DraftPoolFilterPickDraftChoiceDefinition(data, _cardLibrary, _formatLibrary);
            case "draftPoolFilterPluck" ->
                    new DraftPoolFilterPluckDraftChoiceDefinition(data, _cardLibrary, _formatLibrary);
            default -> throw new RuntimeException("Unknown choiceDefinitionType: " + choiceDefinitionType);
        };
    }

}