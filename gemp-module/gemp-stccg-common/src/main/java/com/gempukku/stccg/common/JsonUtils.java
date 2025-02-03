package com.gempukku.stccg.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hjson.JsonValue;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class JsonUtils {

    public static <T> List<T> readListOfClassFromReader(Reader reader, Class<T> clazz) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(JsonValue.readHjson(reader).toString());
        if (node.isNull())
            return new ArrayList<>();
        else {
            List<T> result = new ArrayList<>();
            List<JsonNode> nodeArray = new ArrayList<>();
            if (node.isArray()) {
                for (JsonNode childNode : node)
                    nodeArray.add(childNode);
            } else {
                nodeArray.add(node);
            }
            for (JsonNode elem : nodeArray) {
                T object = mapper.readValue(mapper.writeValueAsString(elem), clazz);
                result.add(object);
            }
            return result;
        }
    }


    public static String toJsonString(Object object) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(object);
    }


}