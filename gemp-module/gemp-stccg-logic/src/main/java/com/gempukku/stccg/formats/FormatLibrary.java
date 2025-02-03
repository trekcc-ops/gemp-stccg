package com.gempukku.stccg.formats;

import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.common.AppConfig;
import com.gempukku.stccg.common.DeserializingLibrary;
import com.gempukku.stccg.common.JSONData;
import com.gempukku.stccg.common.JsonUtils;
import org.hjson.JsonValue;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Semaphore;

@JsonIncludeProperties({ "Formats", "SealedTemplates" })
@JsonPropertyOrder({ "Formats", "SealedTemplates" })
public class FormatLibrary implements DeserializingLibrary {
    private final Map<String, GameFormat> _allFormats = new HashMap<>();
    private final Map<String, SealedEventDefinition> _sealedTemplates = new LinkedHashMap<>();
    private final Semaphore collectionReady = new Semaphore(1);

    public FormatLibrary(CardBlueprintLibrary bpLibrary) {
        reloadFormats(bpLibrary);
        reloadSealedTemplates();
    }

    public void reloadSealedTemplates() {
        try {
            collectionReady.acquire();
            _sealedTemplates.clear();
            loadSealedTemplates(AppConfig.getSealedPath());
            collectionReady.release();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadSealedTemplates(File path) {
        if (path.isDirectory()) {
            for (File file : Objects.requireNonNull(path.listFiles())) {
                loadSealedTemplates(file);
            }
        }
        else if (path.isFile()) {
            if (isNotValidJsonFile(path))
                return;
            try (Reader reader = new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8)) {
                ObjectMapper mapper = new ObjectMapper();
                SealedEventDefinition[] sealedDefinitions = mapper.readValue(path, SealedEventDefinition[].class);
                for (SealedEventDefinition definition : sealedDefinitions) {
                    String definitionId = definition.getId();
                    if(_sealedTemplates.containsKey(definitionId)) {
                        System.out.println("Overwriting existing sealed definition '" + definitionId + "'!");
                    }
                    GameFormat format = _allFormats.get(definition.getFormatId());
                    if (format != null) {
                        definition.assignFormat(format);
                    } else {
                        throw new IOException("Unable to create sealed event template " + definition.getName());
                    }
                    _sealedTemplates.put(definitionId, definition);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void reloadFormats(CardBlueprintLibrary blueprintLibrary) {
        try (InputStreamReader reader =
                     new InputStreamReader(new FileInputStream(AppConfig.getFormatDefinitionsPath()),
                             StandardCharsets.UTF_8)) {
            collectionReady.acquire();
            _allFormats.clear();

            for (JSONData.Format def : JsonUtils.readListOfClassFromReader(reader, JSONData.Format.class)) {
                if (def == null)
                    continue;
                GameFormat format = new DefaultGameFormat(blueprintLibrary, def);
                _allFormats.put(format.getCode(), format);
            }
            collectionReady.release();
        }
        catch (Exception exp) {
            throw new RuntimeException("Problem loading game formats", exp);
        }
    }


    public Map<String, GameFormat> getHallFormats() {
        try {
            collectionReady.acquire();
            Map<String, GameFormat> result = new HashMap<>();
            for (Map.Entry<String, GameFormat> entry : _allFormats.entrySet()) {
                if (entry.getValue().hallVisible()) {
                    result.put(entry.getKey(), entry.getValue());
                }
            }
            var data = Collections.unmodifiableMap(result);
            collectionReady.release();
            return data;
        }
        catch (InterruptedException exp) {
            throw new RuntimeException("FormatLibrary.getHallFormats() interrupted: ", exp);
        }
    }

    @JsonProperty("Formats")
    public Map<String, GameFormat> getAllFormats() {
        try {
            collectionReady.acquire();
            var data = Collections.unmodifiableMap(_allFormats);
            collectionReady.release();
            return data;
        }
        catch (InterruptedException exp) {
            throw new RuntimeException("FormatLibrary.getAllFormats() interrupted: ", exp);
        }
    }

    public GameFormat getFormat(String formatCode) {
        try {
            collectionReady.acquire();
            var data = _allFormats.get(formatCode);
            collectionReady.release();
            return data;
        }
        catch (InterruptedException exp) {
            throw new RuntimeException("FormatLibrary.getFormat() interrupted: ", exp);
        }
    }

    public GameFormat getFormatByName(String formatName) {
        try {
            collectionReady.acquire();
            var data = _allFormats.values().stream()
                    .filter(format -> format.getName().equals(formatName))
                    .findFirst()
                    .orElse(null);
            collectionReady.release();
            return data;
        }
        catch (InterruptedException exp) {
            throw new RuntimeException("FormatLibrary.getFormatByName() interrupted: ", exp);
        }
    }

    public SealedEventDefinition GetSealedTemplate(String leagueName) {
        try {
            collectionReady.acquire();
            var data = _sealedTemplates.get(leagueName);
            if(data == null) {
                collectionReady.release();
                throw new RuntimeException("Could not find league definition for '" + leagueName + "'.");
            }
            collectionReady.release();
            return data;
        }
        catch (InterruptedException exp) {
            throw new RuntimeException("FormatLibrary.GetSealedTemplate() interrupted: ", exp);
        }
    }

    @JsonProperty("SealedTemplates")
    private Map<String, SealedEventDefinition> GetAllSealedTemplatesNew() {
        try {
            collectionReady.acquire();
            Map<String, SealedEventDefinition> result = new HashMap<>();
            for (SealedEventDefinition eventDef : _sealedTemplates.values()) {
                result.put(eventDef.getName(), eventDef);
            }
            collectionReady.release();
            return result;
        }
        catch (InterruptedException exp) {
            throw new RuntimeException("FormatLibrary.GetSealedTemplate() interrupted: ", exp);
        }
    }


}