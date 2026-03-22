package com.gempukku.stccg.game;

import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.common.AppConfig;
import com.gempukku.stccg.formats.FormatLibrary;
import com.gempukku.stccg.hall.HallServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SampleGameLibrary {

    private final List<GameTestBuilder> games;
    private boolean _errorEncountered = false;

    private static final Logger LOGGER = LogManager.getLogger(SampleGameLibrary.class);

    public SampleGameLibrary(CardBlueprintLibrary cardLibrary, FormatLibrary formatLibrary) {
        games = new ArrayList<>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setInjectableValues(new InjectableValues.Std()
                    .addValue(CardBlueprintLibrary.class, cardLibrary)
                    .addValue(FormatLibrary.class, formatLibrary)
            );
            File path = AppConfig.getSampleGameLibraryPath();
            GameTestBuilder[] gameData = mapper.readValue(path, GameTestBuilder[].class);
            games.addAll(Arrays.asList(gameData));
        } catch (Exception exp) {
            _errorEncountered = true;
            LOGGER.error(exp);
        }
    }

    public void addGamesToHall(HallServer hallServer) {
        for (GameTestBuilder builder : games) {
            try {
                builder.initializeGame();
                hallServer.createTableForTestingExistingGame(builder.getGame(), builder.getName());
            } catch(Exception exp) {
                LOGGER.error(exp);
            }
        }
    }

    public boolean loadedSuccessfully() {
        return !_errorEncountered;
    }

}