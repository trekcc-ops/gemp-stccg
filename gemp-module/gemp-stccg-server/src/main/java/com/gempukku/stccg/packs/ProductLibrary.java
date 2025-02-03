package com.gempukku.stccg.packs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.common.AppConfig;
import com.gempukku.stccg.common.DeserializingLibrary;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Semaphore;


public class ProductLibrary implements DeserializingLibrary<PackBox> {
    private final Map<String, PackBox> _products = new HashMap<>();
    private final File _packDirectory = AppConfig.getProductPath();

    private final Semaphore collectionReady = new Semaphore(1);

    public ProductLibrary() {
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
        if (isNotValidJsonFile(file))
            return;
        try {
            ObjectMapper mapper = new ObjectMapper();
            PackBox[] packs = mapper.readValue(file, PackBox[].class);
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

    public PackBox get(String name) {
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