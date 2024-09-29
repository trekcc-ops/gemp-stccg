package com.gempukku.stccg.draft;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.collection.CollectionsManager;
import com.gempukku.stccg.common.AppConfig;
import com.gempukku.stccg.common.JsonUtils;
import com.gempukku.stccg.draft.builder.*;
import com.gempukku.stccg.formats.FormatLibrary;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Semaphore;

public class SoloDraftDefinitions {
    private final Map<String, SoloDraft> _draftTypes = new HashMap<>();
    private final DraftChoiceBuilder _draftChoiceBuilder;
    private final File _draftDefinitionPath;
    private final Semaphore collectionReady = new Semaphore(1);

    public SoloDraftDefinitions(CollectionsManager collectionsManager, CardBlueprintLibrary cardLibrary,
                                FormatLibrary formatLibrary) {
        this(collectionsManager, cardLibrary, formatLibrary, AppConfig.getDraftPath());
    }

    public SoloDraftDefinitions(CollectionsManager collectionsManager, CardBlueprintLibrary cardLibrary,
                                FormatLibrary formatLibrary, File draftDefinitionPath) {
        _draftChoiceBuilder = new DraftChoiceBuilder(collectionsManager, cardLibrary, formatLibrary);
        _draftDefinitionPath = draftDefinitionPath;
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
                    DraftChoiceDefinition draftChoiceDefinition =
                            _draftChoiceBuilder.buildDraftChoiceDefinition(choice);
                    int repeatCount = choice.get("repeat").asInt();
                    for (int i = 0; i < repeatCount; i++)
                        draftChoiceDefinitions.add(draftChoiceDefinition);
                }
            } else {
                DraftChoiceDefinition draftChoiceDefinition =
                        _draftChoiceBuilder.buildDraftChoiceDefinition(choices);
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

    public Map<String, SoloDraft> getAllSoloDrafts() {
        try {
            collectionReady.acquire();
            var data = Collections.unmodifiableMap(_draftTypes);
            collectionReady.release();
            return data;
        } catch (InterruptedException exp) {
            throw new RuntimeException("SoloDraftDefinitions.getAllSoloDrafts() interrupted: ", exp);
        }
    }
}
