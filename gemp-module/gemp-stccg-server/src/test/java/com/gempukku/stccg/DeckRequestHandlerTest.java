package com.gempukku.stccg;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.collection.CollectionsManager;
import com.gempukku.stccg.common.GameFormat;
import com.gempukku.stccg.common.JSONData;
import com.gempukku.stccg.draft.SoloDraftDefinitions;
import com.gempukku.stccg.formats.SealedEventDefinition;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.stream.Collectors;

public class DeckRequestHandlerTest extends AbstractServerTest {

    @SuppressWarnings("WriteOnlyObject")
    @Test
    public void formatTest2() throws JsonProcessingException {
        CollectionsManager collectionsManager = new CollectionsManager(null, null, null, _cardLibrary);

        SoloDraftDefinitions soloDraftDefinitions = new SoloDraftDefinitions(collectionsManager, _cardLibrary, _formatLibrary);

            JSONData.FullFormatReadout data = new JSONData.FullFormatReadout();
            data.Formats = _formatLibrary.getAllFormats().values().stream()
                    .map(GameFormat::Serialize)
                    .collect(Collectors.toMap(x-> x.code, x-> x));
            data.SealedTemplates = _formatLibrary.GetAllSealedTemplates().values().stream()
                    .map(SealedEventDefinition::Serialize)
                    .collect(Collectors.toMap(x-> x.name, x-> x));
            data.DraftTemplates = soloDraftDefinitions.getAllSoloDrafts().values().stream()
                    .map(soloDraft -> new JSONData.ItemStub(soloDraft.getCode(), soloDraft.getFormat()))
                    .collect(Collectors.toMap(x-> x.code, x-> x));

        Map<String, GameFormat> formats = _formatLibrary.getHallFormats();

            Object[] output = formats.entrySet().stream()
                    .map(x -> new JSONData.ItemStub(x.getKey(), x.getValue().getName()))
                    .toArray();

            String json2new = new ObjectMapper().writeValueAsString(output);
            System.out.println(json2new);
        }
}