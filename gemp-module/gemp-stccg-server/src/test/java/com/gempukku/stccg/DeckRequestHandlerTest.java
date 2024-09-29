package com.gempukku.stccg;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.collection.CollectionsManager;
import com.gempukku.stccg.common.GameFormat;
import com.gempukku.stccg.common.JSONDefs;
import com.gempukku.stccg.draft.SoloDraftDefinitions;
import com.gempukku.stccg.formats.SealedLeagueDefinition;
import org.junit.Test;

import java.util.Map;
import java.util.stream.Collectors;

public class DeckRequestHandlerTest extends AbstractServerTest {

    @Test
    public void formatTest() throws JsonProcessingException {
        GameFormat currentFormat = _formatLibrary.getFormat("st1emoderncomplete");

        String json;
        Map<String, String> sets = currentFormat.getValidSets();
        Object[] output = sets.entrySet().stream()
                .map(x -> new JSONDefs.ItemStub(x.getKey(), x.getValue()))
                .toArray();

        String newJson = new ObjectMapper().writeValueAsString(output);
    }

    @Test
    public void formatTest2() throws JsonProcessingException {
        CollectionsManager collectionsManager = new CollectionsManager(null, null, null, _cardLibrary);

        SoloDraftDefinitions soloDraftDefinitions = new SoloDraftDefinitions(collectionsManager, _cardLibrary, _formatLibrary);

            JSONDefs.FullFormatReadout data = new JSONDefs.FullFormatReadout();
            data.Formats = _formatLibrary.getAllFormats().values().stream()
                    .map(GameFormat::Serialize)
                    .collect(Collectors.toMap(x-> x.code, x-> x));
            data.SealedTemplates = _formatLibrary.GetAllSealedTemplates().values().stream()
                    .map(SealedLeagueDefinition::Serialize)
                    .collect(Collectors.toMap(x-> x.name, x-> x));
            data.DraftTemplates = soloDraftDefinitions.getAllSoloDrafts().values().stream()
                    .map(soloDraft -> new JSONDefs.ItemStub(soloDraft.getCode(), soloDraft.getFormat()))
                    .collect(Collectors.toMap(x-> x.code, x-> x));

            String json1new = new ObjectMapper().writeValueAsString(data);
            JsonNode json1newnode = new ObjectMapper().readTree(json1new);


            Map<String, GameFormat> formats = _formatLibrary.getHallFormats();

            Object[] output = formats.entrySet().stream()
                    .map(x -> new JSONDefs.ItemStub(x.getKey(), x.getValue().getName()))
                    .toArray();

            String json2new = new ObjectMapper().writeValueAsString(output);
            System.out.println(json2new);
        }
}
