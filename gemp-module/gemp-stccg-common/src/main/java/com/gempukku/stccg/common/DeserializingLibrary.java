package com.gempukku.stccg.common;

import org.apache.commons.io.FilenameUtils;

import java.io.File;

public interface DeserializingLibrary<T> {

    T get(String objectId);

    default boolean isNotValidJsonFile(File file) {
        String ext = FilenameUtils.getExtension(file.getName());
        return !ext.equalsIgnoreCase("json") && !ext.equalsIgnoreCase("hjson");
    }

}