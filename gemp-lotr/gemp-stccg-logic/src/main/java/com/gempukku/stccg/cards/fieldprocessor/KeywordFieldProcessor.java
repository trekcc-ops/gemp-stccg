package com.gempukku.stccg.cards.fieldprocessor;

import com.gempukku.stccg.cards.CardBlueprint;
import com.gempukku.stccg.cards.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.common.filterable.lotr.Keyword;

import java.util.HashMap;
import java.util.Map;

public class KeywordFieldProcessor implements FieldProcessor {
    @Override
    public void processField(String key, Object value, CardBlueprint blueprint,
                             CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        final String[] keywords = environment.getStringArray(value, key);

        Map<Keyword, Integer> result = new HashMap<>();
        for (String keywordString : keywords) {
            final String[] keywordSplit = keywordString.split("\\+");
            Keyword keyword = environment.getEnum(Keyword.class, keywordSplit[0], key);
            result.put(keyword, keywordSplit.length == 2 ?
                    Integer.parseInt(keywordSplit[1]) : 1);
        }
        blueprint.setKeywords(result);
    }

}
