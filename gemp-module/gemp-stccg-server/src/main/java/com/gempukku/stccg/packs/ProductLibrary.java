package com.gempukku.stccg.packs;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.common.AppConfig;
import com.gempukku.stccg.common.JSONData;
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

    private final Semaphore collectionReady = new Semaphore(1);

    public ProductLibrary(CardBlueprintLibrary cardLibrary) {
        this(cardLibrary, AppConfig.getProductPath());
    }
    private ProductLibrary(CardBlueprintLibrary cardLibrary, File packDefinitionPath) {
        _cardLibrary = cardLibrary;
        _packDirectory = packDefinitionPath;

        collectionReady.acquireUninterruptibly();
        loadPacks(_packDirectory);
        collectionReady.release();
    }

    public final void ReloadPacks() {
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
            File[] files = path.listFiles();
            for (File file : Objects.requireNonNull(files)) {
                loadPacks(file);
            }
        }
    }

    private void loadPackFromFile(File file) {
        if (JsonUtils.isNotAValidHJSONFile(file))
            return;
        try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            List<PackBox> packs = JsonUtils.readListOfClassFromReader(reader, PackBox.class);
            for (PackBox pack : packs) {
                String packName = pack.getName();
                if(_products.containsKey(packName)) {
                    System.out.println("Overwriting existing pack '" + packName + "'!");
                }
                _products.put(pack.getName(), pack);
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

    public CardBlueprintLibrary getCardBlueprintLibrary() {
        return _cardLibrary;
    }
}