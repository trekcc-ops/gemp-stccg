package com.gempukku.stccg.draft;

import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.common.AppConfig;
import com.gempukku.stccg.formats.FormatLibrary;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Semaphore;

public class DraftFormatLibrary {
    private final Map<String, SoloDraft> _draftTypes = new HashMap<>();
    private final Semaphore collectionReady = new Semaphore(1);
    private final ObjectMapper _draftMapper;

    public DraftFormatLibrary(CardBlueprintLibrary cardLibrary, FormatLibrary formatLibrary) {
        InjectableValues values = new InjectableValues.Std()
                .addValue("cardLibrary", cardLibrary)
                .addValue("formatLibrary", formatLibrary);
        _draftMapper = new ObjectMapper().setInjectableValues(values);
        try {
            collectionReady.acquire();
            loadDrafts(AppConfig.getDraftPath());
            collectionReady.release();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadDrafts(File path) {
        if (path.isFile()) {
            try {
                SoloDraft soloDraft = _draftMapper.readValue(path, DefaultSoloDraft.class);
                String code = soloDraft.getCode();
                if (_draftTypes.containsKey(code))
                    System.out.println("Duplicate draft loaded: " + code);
                _draftTypes.put(code, soloDraft);
            } catch (IOException exp) {
                throw new RuntimeException("Problem loading solo draft " + path, exp);
            }
        }
        else if (path.isDirectory()) {
            for (File file : Objects.requireNonNull(path.listFiles())) {
                loadDrafts(file);
            }
        }
    }

    public SoloDraft getSoloDraft(String draftType) {
        try {
            collectionReady.acquire();
            var data = _draftTypes.get(draftType);
            collectionReady.release();
            return data;
        } catch (InterruptedException exp) {
            throw new RuntimeException("SoloDraftDefinitions.getSoloDraft() interrupted: ", exp);
        }
    }


}