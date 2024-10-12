package com.gempukku.stccg.draft;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.TextUtils;

import java.util.*;

public class DraftPoolBuilder {

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