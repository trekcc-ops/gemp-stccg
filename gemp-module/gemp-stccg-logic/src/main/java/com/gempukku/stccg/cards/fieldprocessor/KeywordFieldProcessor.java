package com.gempukku.stccg.cards.fieldprocessor;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.common.filterable.Keyword;

import java.util.HashMap;
import java.util.Map;

public class KeywordFieldProcessor implements FieldProcessor {
    @Override
    public void processField(String key, JsonNode value, CardBlueprint blueprint,
                             CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        Map<Keyword, Integer> result = new HashMap<>();
        final String[] keywordSplit = value.textValue().split("\\+");
        Keyword keyword = environment.getEnum(Keyword.class, keywordSplit[0], key);
        result.put(keyword, keywordSplit.length == 2 ?
                Integer.parseInt(keywordSplit[1]) : 1);
        blueprint.setKeywords(result);
    }

}
