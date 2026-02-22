package com.gempukku.stccg.cards;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.gempukku.stccg.common.CommaSeparatedValuesDeserializationProblemHandler;
import com.gempukku.stccg.common.EnumSpecialCharacterDeserializationProblemHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hjson.JsonValue;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class BlueprintLibraryMigration {
    private static final Logger LOGGER = LogManager.getLogger(BlueprintLibraryMigration.class);

    private final static ObjectMapper _jsonMapper = JsonMapper.builder()
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
            .addHandler(new CommaSeparatedValuesDeserializationProblemHandler())
            .addHandler(new EnumSpecialCharacterDeserializationProblemHandler())
            .build();

    private final static File _cardPath = new File("D:/GEMP/gemp-stccg/gemp-stccg/gemp-module/gemp-stccg-cards/src/main/resources/cards/");

    public static void main(String[] args) {
        loadSetsWithCards(_cardPath);
    }


    private static void loadSetsWithCards(File path) {
        if (path.isFile()) {
            loadSetWithCardsFromFileNew(path);
        }
        else if (path.isDirectory()) {
            for (File file : Objects.requireNonNull(path.listFiles())) {
                loadSetsWithCards(file);
            }
        }
    }

    private static void loadSetWithCardsFromFileNew(File file) {
        try {
            String name = file.getName();
            if (name.endsWith("hjson")) {
                Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
                JsonNode jsonNode = _jsonMapper.readTree(JsonValue.readHjson(reader).toString());

                DefaultPrettyPrinter.Indenter indenter = new DefaultIndenter("    ", DefaultIndenter.SYS_LF); // 4 spaces
                DefaultPrettyPrinter printer = new DefaultPrettyPrinter();
                printer.indentObjectsWith(indenter);
                printer.indentArraysWith(indenter);
                ObjectWriter writer = _jsonMapper.writer(printer);

                String newName = "D:/Gemp/ST Files/Jsontest/output/" + name.replace("hjson", "json");
                writer.writeValue(new File(newName), jsonNode);
            }

        } catch (Exception ignored) {
        }
    }


}