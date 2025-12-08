package com.gempukku.stccg;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.player.PlayerNotFoundException;
import org.junit.jupiter.api.Test;

public class SerializingIdNumberTest extends AbstractAtTest {

    @Test
    public void thing() throws JsonProcessingException, PlayerNotFoundException {
        initializeSimple1EGame(40);
        SerializingIdNumberGame game = new SerializingIdNumberGame(_game);

        ObjectMapper mapper = new ObjectMapper();
        String jsonString = mapper.writeValueAsString(game);
        System.out.println(jsonString);

        mapper.setInjectableValues(new InjectableValues.Std().addValue(CardBlueprintLibrary.class, _cardLibrary));
        SerializingIdNumberGame gameCopy = mapper.readValue(jsonString, SerializingIdNumberGame.class);
    }

}