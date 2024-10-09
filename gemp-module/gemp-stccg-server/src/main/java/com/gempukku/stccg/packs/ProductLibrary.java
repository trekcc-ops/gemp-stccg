package com.gempukku.stccg.packs;

import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.common.AppConfig;
import com.gempukku.stccg.common.JSONDefs;
import com.gempukku.stccg.common.JsonUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Semaphore;


public class ProductLibrary {
    private final Map<String, PackBox> _products = new HashMap<>();
    private final CardBlueprintLibrary _cardLibrary;
    private final File _packDirectory;
    private static final Logger LOGGER = LogManager.getLogger(ProductLibrary.class);

    private final Semaphore collectionReady = new Semaphore(1);

    public ProductLibrary(CardBlueprintLibrary cardLibrary) {
        this(cardLibrary, AppConfig.getProductPath());
    }
    public ProductLibrary(CardBlueprintLibrary cardLibrary, File packDefinitionDirectory) {
        _cardLibrary = cardLibrary;
        _packDirectory = packDefinitionDirectory;

        collectionReady.acquireUninterruptibly();
        loadPacks(_packDirectory);
        collectionReady.release();
    }

    public void ReloadPacks() {
        try {
            collectionReady.acquire();
            loadPacks(_packDirectory);
            collectionReady.release();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadPacks(File path) {
        if (path.isFile()) {
            loadPackFromFile(path);
        }
        else if (path.isDirectory()) {
            for (File file : Objects.requireNonNull(path.listFiles())) {
                loadPacks(file);
            }
        }
    }

    private void loadPackFromFile(File file) {
        if (JsonUtils.IsInvalidHjsonFile(file))
            return;
        try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            List<JSONDefs.Pack> packs = JsonUtils.readListOfClassFromReader(reader, JSONDefs.Pack.class);

            for (JSONDefs.Pack def : packs) {
                LOGGER.debug("Loading pack definitions for " + def.name);

                PackBox result = null;
                String[] rarities;
                String[] sets;
                switch (def.type) {
                    case random -> {
                        if (def.items == null || def.items.isEmpty())
                            continue;
                        if (def.items.stream().anyMatch(x -> x.contains("%"))) {
                            result = WeightedRandomPack.LoadFromArray(def.items);
                        } else {
                            result = UnweightedRandomPack.LoadFromArray(def.items);
                        }
                    }
                    case random_foil -> {
                        if (def.data == null || !def.data.has("rarities") || !def.data.has("sets")) {
                            System.out.println(def.name + " RANDOM_FOIL pack type must contain a definition for 'rarities' and 'sets' within data.");
                            continue;
                        }
                        rarities = def.data.get("rarities").textValue().toUpperCase().split("\\s*,\\s*");
                        sets = def.data.get("sets").textValue().split("\\s*,\\s*");
                        result = new RandomFoilPack(rarities, sets, _cardLibrary);
                    }
                    case booster -> {
                        if (def.data == null || !def.data.has("set")) {
                            System.out.println(def.name + " BOOSTER pack type must contain a definition for 'set' within data.");
                            continue;
                        }
                        if (def.data.get("set").textValue().contains(",")) {
                            System.out.println(def.name + " BOOSTER pack type must define exactly one set.");
                            continue;
                        }
                        String set = def.data.get("set").textValue().trim();
                        result = new BoosterPack(_cardLibrary.getSetDefinitions().get(set), def.items);
                    }
                    case pack, selection -> {
                        if (def.items == null || def.items.isEmpty())
                            continue;
                        result = FixedPackBox.LoadFromArray(def.items);
                    }
                }
                if(result == null)
                {
                    System.out.println("Unrecognized pack type: " + def.type);
                    continue;
                }

                if(_products.containsKey(def.name)) {
                    System.out.println("Overwriting existing pack '" + def.name + "'!");
                }
                _products.put(def.name, result);
            }


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, PackBox> getAllProducts() {
        try {
            collectionReady.acquire();
            var data = Collections.unmodifiableMap(_products);
            collectionReady.release();
            return data;
        }
        catch (InterruptedException exp) {
            throw new RuntimeException("ProductLibrary.GetAllProducts() interrupted: ", exp);
        }
    }

    public PackBox GetProduct(String name) {
        try {
            collectionReady.acquire();
            var data = _products.get(name);
            collectionReady.release();
            return data;
        }
        catch (InterruptedException exp) {
            throw new RuntimeException("ProductLibrary.GetProduct() interrupted: ", exp);
        }
    }
}