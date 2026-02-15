package com.gempukku.stccg.gamestate;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;

public class MissionLocationSerializerFilter extends SimpleBeanPropertyFilter {

    @Override
    public void serializeAsField(Object pojo, JsonGenerator jgen, SerializerProvider provider, PropertyWriter writer)
            throws Exception {
        Boolean showComplete = (Boolean) provider.getAttribute("showComplete");

        if (include(writer)) {
            String propertyName = writer.getName();
            if (!propertyName.equals("seedCardIds") || showComplete) {
                writer.serializeAsField(pojo, jgen, provider);
            }
        } else if (!jgen.canOmitFields()) { // since 2.3
            writer.serializeAsOmittedField(pojo, jgen, provider);
        }
    }
    @Override
    protected boolean include(BeanPropertyWriter writer) {
        return true;
    }
    @Override
    protected boolean include(PropertyWriter writer) {
        return true;
    }
}