package com.gempukku.stccg;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.collection.CollectionsManager;
import com.gempukku.stccg.common.JsonUtils;
import com.gempukku.stccg.formats.GameFormat;
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

        Map<String, GameFormat> formats = _formatLibrary.getHallFormats();

            Object[] output = formats.entrySet().stream()
                    .map(x -> new JSONData.ItemStub(x.getKey(), x.getValue().getName()))
                    .toArray();

            String json2new = new ObjectMapper().writeValueAsString(output);
            System.out.println(json2new);
    }

    @Test
    public void testFormat3() {


    }
}