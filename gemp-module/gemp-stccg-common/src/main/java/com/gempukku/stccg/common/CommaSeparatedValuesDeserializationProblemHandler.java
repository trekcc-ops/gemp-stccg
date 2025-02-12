package com.gempukku.stccg.common;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CommaSeparatedValuesDeserializationProblemHandler extends DeserializationProblemHandler {

        @Override
        public Object handleUnexpectedToken(DeserializationContext ctxt, JavaType targetType, JsonToken token,
                                            JsonParser parser, String failureMsg) throws IOException {
            if (token == JsonToken.VALUE_STRING && targetType.isCollectionLikeType()) {
                return deserializeAsList(targetType, parser);
            }
            return super.handleUnexpectedToken(ctxt, targetType, token, parser, failureMsg);
        }

        private Object deserializeAsList(JavaType listType, JsonParser parser) throws IOException {
            String[] values = readValues(parser);

            ObjectMapper mapper = (ObjectMapper) parser.getCodec();
            JavaType itemType = listType.getContentType();

            List<Object> result = new ArrayList<>();
            for (String value : values) {
                result.add(convertToItemType(mapper, itemType, value));
            }

            return result;
        }

        private Object convertToItemType(ObjectMapper mapper, JavaType contentType, String value) throws IOException {
            final String json = "\"" + value.trim() + "\"";

            return mapper.readValue(json, contentType);
        }

        private String[] readValues(JsonParser p) throws IOException {
            final String text = p.getText();

            return text.split(",");
        }
}