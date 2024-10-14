package com.gempukku.stccg.formats;

import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.common.AppConfig;
import com.gempukku.stccg.common.JSONData;
import com.gempukku.stccg.common.JsonUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Semaphore;

public class FormatLibrary {
    private final Map<String, GameFormat> _allFormats = new HashMap<>();
    private final Map<String, GameFormat> _hallFormats = new LinkedHashMap<>();
    private final Map<String, SealedEventDefinition> _sealedTemplates = new LinkedHashMap<>();
    private final CardBlueprintLibrary _cardLibrary;
    private final File _formatPath;
    private final File _sealedPath;


    private final Semaphore collectionReady = new Semaphore(1);

    public FormatLibrary(CardBlueprintLibrary bpLibrary) {
        this(bpLibrary, AppConfig.getFormatDefinitionsPath(), AppConfig.getSealedPath());
    }

    public FormatLibrary(CardBlueprintLibrary bpLibrary, File formatPath, File sealedPath) {
        _cardLibrary = bpLibrary;
        _formatPath = formatPath;
        _sealedPath = sealedPath;

        ReloadFormats();
        ReloadSealedTemplates();
    }

    public void ReloadSealedTemplates() {
        try {
            collectionReady.acquire();
            _sealedTemplates.clear();
            loadSealedTemplates(_sealedPath);
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
            if (JsonUtils.isNotAValidHJSONFile(path))
                return;
            try (Reader reader = new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8)) {
                List<JSONData.SealedTemplate> defs =
                        JsonUtils.readListOfClassFromReader(reader, JSONData.SealedTemplate.class);

                for (var def : defs) {
                    if(def == null)
                        continue;
                    var sealed = new SealedEventDefinition(def.name, def.id, _allFormats.get(def.format), def.seriesProduct);

                    if(_sealedTemplates.containsKey(def.id)) {
                        System.out.println("Overwriting existing sealed definition '" + def.id + "'!");
                    }
                    _sealedTemplates.put(def.id, sealed);
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public void ReloadFormats() {
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(_formatPath), StandardCharsets.UTF_8)) {
            collectionReady.acquire();
            _allFormats.clear();
            _hallFormats.clear();

            for (JSONData.Format def : JsonUtils.readListOfClassFromReader(reader, JSONData.Format.class)) {
                if (def == null)
                    continue;

                DefaultGameFormat format = new DefaultGameFormat(_cardLibrary, def);

                _allFormats.put(format.getCode(), format);
                if (format.hallVisible()) {
                    _hallFormats.put(format.getCode(), format);
                }
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
            var data = Collections.unmodifiableMap(_hallFormats);
            collectionReady.release();
            return data;
        }
        catch (InterruptedException exp) {
            throw new RuntimeException("FormatLibrary.getHallFormats() interrupted: ", exp);
        }
    }

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

    public Map<String, SealedEventDefinition> GetAllSealedTemplates() {
        try {
            collectionReady.acquire();
            var data = Collections.unmodifiableMap(_sealedTemplates);
            collectionReady.release();
            return data;
        }
        catch (InterruptedException exp) {
            throw new RuntimeException("FormatLibrary.GetSealedTemplate() interrupted: ", exp);
        }
    }

}