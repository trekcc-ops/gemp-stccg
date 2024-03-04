package com.gempukku.stccg.formats;

import com.alibaba.fastjson.JSON;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.common.AppConfig;
import com.gempukku.stccg.common.JSONDefs;
import com.gempukku.stccg.adventure.AdventureLibrary;
import com.gempukku.stccg.SealedLeagueDefinition;
import com.gempukku.stccg.common.JsonUtils;
import org.hjson.JsonValue;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Semaphore;

public class FormatLibrary {
    private final Map<String, GameFormat> _allFormats = new HashMap<>();
    private final Map<String, GameFormat> _hallFormats = new LinkedHashMap<>();

    private final Map<String, SealedLeagueDefinition> _sealedTemplates = new LinkedHashMap<>();

    private final CardBlueprintLibrary _cardLibrary;
    private final File _formatPath;
    private final File _sealedPath;


    private final Semaphore collectionReady = new Semaphore(1);

    public FormatLibrary(AdventureLibrary adventureLibrary, CardBlueprintLibrary bpLibrary) {
        this(bpLibrary, AppConfig.getFormatDefinitionsPath(), AppConfig.getSealedPath());
    }

    public FormatLibrary(CardBlueprintLibrary bpLibrary, File formatPath,
                         File sealedPath) {
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
        if (path.isFile()) {
            loadTemplateFromFile(path);
        }
        else if (path.isDirectory()) {
            for (File file : Objects.requireNonNull(path.listFiles())) {
                loadSealedTemplates(file);
            }
        }
    }

    private void loadTemplateFromFile(File file) {
        if (JsonUtils.IsInvalidHjsonFile(file))
            return;
        JSONParser parser = new JSONParser();
        try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            var defs = JsonUtils.ConvertArray(reader, JSONDefs.SealedTemplate.class);

            if(defs == null)
            {
                var def= JsonUtils.Convert(reader, JSONDefs.SealedTemplate.class);
                if(def != null)
                {
                    defs = Collections.singletonList(def);
                }
                else {
                    System.out.println(file + " is not a SealedTemplate nor an array of SealedTemplate.  Could not load from file.");
                    return;
                }
            }

            for (var def : defs) {
                if(def == null)
                    continue;
                var sealed = new SealedLeagueDefinition(def.Name, def.ID, _allFormats.get(def.Format), def.SeriesProduct);

                if(_sealedTemplates.containsKey(def.ID)) {
                    System.out.println("Overwriting existing sealed definition '" + def.ID + "'!");
                }
                _sealedTemplates.put(def.ID, sealed);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void ReloadFormats() {
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(_formatPath), StandardCharsets.UTF_8)) {
            collectionReady.acquire();
            _allFormats.clear();
            _hallFormats.clear();

            for (JSONDefs.Format def : JSON.parseObject(JsonValue.readHjson(reader).toString(), JSONDefs.Format[].class)) {
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
            throw new RuntimeException("Problem loading LotR formats", exp);
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

    private final Map<String, String> legacyCodeMapping = new HashMap<>() {{
        put("fotr_block", "fotr_block_sealed");
        put("ttt_block", "ttt_block_sealed");
        put("movie", "rotk_block_sealed");
        put("war_block", "wotr_block_sealed");
        put("hunters_block", "th_block_sealed");
        put("movie_special", "movie_special_sealed");
        put("ts_special", "ts_special_sealed");
    }};

    public SealedLeagueDefinition GetSealedTemplate(String leagueName) {
        try {
            collectionReady.acquire();
            var data = _sealedTemplates.get(leagueName);
            if(data == null) {
                data = _sealedTemplates.get(legacyCodeMapping.get(leagueName));
            }
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

    public Map<String,SealedLeagueDefinition> GetAllSealedTemplates() {
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

    public SealedLeagueDefinition GetSealedTemplateByFormatCode(String formatCode) {
        try {
            collectionReady.acquire();
            var data = _sealedTemplates.values().stream()
                    .filter(x -> x.GetFormat().getCode().equals(formatCode))
                    .findFirst()
                    .orElse(null);
            collectionReady.release();
            return data;
        }
        catch (InterruptedException exp) {
            throw new RuntimeException("FormatLibrary.GetSealedTemplateByFormatCode() interrupted: ", exp);
        }
    }
}
