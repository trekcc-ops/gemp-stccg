package com.gempukku.stccg.league;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.draft.DraftFormatLibrary;
import com.gempukku.stccg.formats.FormatLibrary;

public class LeagueMapper extends ObjectMapper {

    public LeagueMapper(CardBlueprintLibrary cardLibrary, FormatLibrary formatLibrary,
                        DraftFormatLibrary draftFormatLibrary) {
        InjectableValues.Std injectables = new InjectableValues.Std()
                .addValue(CardBlueprintLibrary.class, cardLibrary)
                .addValue(FormatLibrary.class, formatLibrary)
                .addValue(DraftFormatLibrary.class, draftFormatLibrary);
        setInjectableValues(injectables);
        registerModule(new JavaTimeModule());
        disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
        enable(SerializationFeature.WRITE_DATES_WITH_ZONE_ID);
        disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public String writeLeagueAsJsonString(League league) throws JsonProcessingException {
        JsonNode leagueNode = valueToTree(league);
        if (league.getLeagueId() < 0) {
            ((ObjectNode) leagueNode).remove("leagueId");
        }
        return writeValueAsString(leagueNode);
    }

}