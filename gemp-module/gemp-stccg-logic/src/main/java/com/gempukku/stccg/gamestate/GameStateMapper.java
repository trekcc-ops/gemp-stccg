package com.gempukku.stccg.gamestate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

import java.util.HashMap;
import java.util.Map;

public class GameStateMapper extends ObjectMapper {

    public ObjectWriter writer(boolean showComplete) {
        FilterProvider filters = new SimpleFilterProvider()
                .addFilter("missionLocationSerializerFilter", new MissionLocationSerializerFilter());
        Map<Object, Object> attributes = new HashMap<>();
        attributes.put("showComplete", showComplete);
        return new ObjectMapper().writer(filters).withAttributes(attributes);
    }

}