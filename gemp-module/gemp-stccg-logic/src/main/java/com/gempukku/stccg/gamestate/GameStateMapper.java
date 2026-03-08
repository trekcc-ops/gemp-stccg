package com.gempukku.stccg.gamestate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.json.JsonMapper;

public class GameStateMapper extends JsonMapper {

    public ObjectWriter writer(boolean showComplete) {
        return (showComplete) ?
                new ObjectMapper().writerWithView(GameStateViews.AdminView.class) :
                new ObjectMapper().writerWithView(GameStateViews.UserView.class);
    }

}