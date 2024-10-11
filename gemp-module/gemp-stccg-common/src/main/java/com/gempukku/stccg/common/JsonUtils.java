package com.gempukku.stccg.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FilenameUtils;
import org.hjson.JsonValue;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class JsonUtils {

    private static final ObjectMapper _mapper = new ObjectMapper();

    public static boolean isNotAValidHJSONFile(File file) {
        String ext = FilenameUtils.getExtension(file.getName());
        return !ext.equalsIgnoreCase("json") && !ext.equalsIgnoreCase("hjson");
    }

    //Reads both json and hjson files, converting both to json (for compatibility with other libraries)
    public static String readJson(Reader reader) throws IOException {
        return JsonValue.readHjson(reader).toString();
    }

    public static JsonNode readJsonFromFile(File file) throws IOException {
        Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
        return readJsonFromReader(reader);
    }

    public static JsonNode readJsonFromReader(Reader reader) throws IOException {
        return _mapper.readTree(JsonUtils.readJson(reader));
    }

    public static <T> List<T> readListOfClassFromReader(Reader reader, Class<T> clazz) throws IOException {
        JsonNode node = readJsonFromReader(reader);
        if (node.isNull())
            return new ArrayList<>();
        else {
            List<T> result = new ArrayList<>();
            List<JsonNode> nodeArray = toArray(node);
            for (JsonNode elem : nodeArray) {
                T object = _mapper.readValue(toJsonString(elem), clazz);
                result.add(object);
            }
            return result;
        }
    }


    public static List<JsonNode> toArray(JsonNode parentNode) {
        List<JsonNode> result = new ArrayList<>();
        if (parentNode.isArray()) {
            for (JsonNode node : parentNode)
                result.add(node);
        } else {
            result.add(parentNode);
        }
        return result;
    }

    public static List<String> toStringArray(JsonNode parentNode) {
        List<String> result = new ArrayList<>();
        if (parentNode.isArray()) {
            for (JsonNode node : parentNode)
                result.add(node.textValue());
        } else {
            result.add(parentNode.textValue());
        }
        return result;
    }

    public static String toJsonString(Object object) throws JsonProcessingException {
        return _mapper.writeValueAsString(object);
    }
}