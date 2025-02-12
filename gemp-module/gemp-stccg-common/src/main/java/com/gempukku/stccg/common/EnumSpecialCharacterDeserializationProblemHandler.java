package com.gempukku.stccg.common;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.gempukku.stccg.common.filterable.ShipClass;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Locale;

public class EnumSpecialCharacterDeserializationProblemHandler extends DeserializationProblemHandler {

        @Override
        public Object handleWeirdStringValue(DeserializationContext ctxt, Class<?> targetType, String valueToConvert,
                                           String failureMsg) throws IOException {
            Object result = null;
            if (targetType.isEnum()) {
                result = findCorrectEnum(targetType, valueToConvert);
            }
            if (result != null)
                return result;
            else
                return super.handleWeirdStringValue(ctxt, targetType, valueToConvert, failureMsg);
        }

        @Override
        public Object handleWeirdKey(DeserializationContext ctxt, Class<?> rawKeyType, String keyValue,
                                     String failureMsg) throws IOException {
            Object result = null;
            if (rawKeyType.isEnum()) {
                result = findCorrectEnum(rawKeyType, keyValue);
            }
            if (result != null)
                return result;
            else
                return super.handleWeirdStringValue(ctxt, rawKeyType, keyValue, failureMsg);
        }


        private Enum findCorrectEnum(Class<?> enumClass, String valueToConvert) {
            if (enumClass == ShipClass.class) {
                valueToConvert = valueToConvert.toUpperCase(Locale.ROOT).replace(" CLASS","");
            }
            String constantString = valueToConvert.trim().toUpperCase().replaceAll("[ '\\-.]", "_");
            for (Object constant : enumClass.getEnumConstants()) {
                if (constant instanceof Enum enumValue) {
                    if (enumValue.name().equals(constantString)) {
                        return enumValue;
                    }
                }
            }
            return null;
        }

}