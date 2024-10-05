package com.gempukku.stccg.collection;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.DBDefs;
import com.gempukku.stccg.common.JsonUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CollectionSerializer {
    private final List<String> _doubleByteCountItems = new ArrayList<>();
    private final List<String> _singleByteCountItems = new ArrayList<>();

    private final ObjectMapper _mapper = new ObjectMapper();


    public MutableCardCollection deserializeCollection(InputStream inputStream) throws IOException {
        int version = inputStream.read();
        if (version == 0) {
            return deserializeCollectionVer0(new BufferedInputStream(inputStream));
        } else if (version == 1) {
            return deserializeCollectionVer1(new BufferedInputStream(inputStream));
        } else if (version == 2) {
            return deserializeCollectionVer2(new BufferedInputStream(inputStream));
        } else if (version == 3) {
            return deserializeCollectionVer3(new BufferedInputStream(inputStream));
        } else if (version == 4) {
            return deserializeCollectionVer4(new BufferedInputStream(inputStream));
        } else {
            throw new IllegalStateException("Unknown version of serialized collection: " + version);
        }
    }

    public MutableCardCollection deserializeCollection(DBDefs.Collection coll, List<DBDefs.CollectionEntry> entries)
            throws IOException {
        DefaultCardCollection newColl = new DefaultCardCollection();

        if(coll.extra_info != null) {
            newColl.setExtraInformation(_mapper.convertValue(
                    _mapper.readTree(coll.extra_info), new TypeReference<>() {}));
        }

        for(var entry : entries) {
            newColl.addItem(entry.product, entry.quantity);
        }

        return newColl;
    }

    private MutableCardCollection deserializeCollectionVer0(BufferedInputStream inputStream) throws IOException {
        int packTypes = inputStream.read();

        DefaultCardCollection collection = new DefaultCardCollection();

        byte[] packs = new byte[packTypes];
        int read = readWholeArray(inputStream, packs);
        if (read != packTypes)
            throw new IllegalStateException("Under-read the packs information");
        for (int i = 0; i < packs.length; i++)
            if (packs[i] > 0)
                collection.addItem(_doubleByteCountItems.get(i), packs[i]);

        int cardBytes = convertToInt(inputStream.read(), inputStream.read());
        byte[] cards = new byte[cardBytes];
        read = readWholeArray(inputStream, cards);
        if (read != cardBytes)
            throw new IllegalArgumentException("Under-read the cards information");
        for (int i = 0; i < cards.length; i++)
            if (cards[i] > 0) {
                final String blueprintId = _singleByteCountItems.get(i);
                collection.addItem(blueprintId, cards[i]);
            }

        return collection;
    }

    private MutableCardCollection deserializeCollectionVer1(BufferedInputStream inputStream) throws IOException {
        int byte1 = inputStream.read();
        int byte2 = inputStream.read();
        int byte3 = inputStream.read();

        int currency = convertToInt(byte1, byte2, byte3);

        int packTypes = inputStream.read();

        DefaultCardCollection collection = new DefaultCardCollection();
        collection.addCurrency(currency);

        byte[] packs = new byte[packTypes];
        int read = readWholeArray(inputStream, packs);
        if (read != packTypes)
            throw new IllegalStateException("Under-read the packs information");
        for (int i = 0; i < packs.length; i++)
            if (packs[i] > 0)
                collection.addItem(_doubleByteCountItems.get(i), packs[i]);

        int cardBytes = convertToInt(inputStream.read(), inputStream.read());
        byte[] cards = new byte[cardBytes];
        read = readWholeArray(inputStream, cards);
        if (read != cardBytes)
            throw new IllegalArgumentException("Under-read the cards information");
        for (int i = 0; i < cards.length; i++)
            if (cards[i] > 0) {
                final String blueprintId = _singleByteCountItems.get(i);
                collection.addItem(blueprintId, cards[i]);
            }

        return collection;
    }

    private MutableCardCollection deserializeCollectionVer2(BufferedInputStream inputStream) throws IOException {
        int byte1 = inputStream.read();
        int byte2 = inputStream.read();
        int byte3 = inputStream.read();

        int currency = convertToInt(byte1, byte2, byte3);

        int packTypes = inputStream.read();

        DefaultCardCollection collection = new DefaultCardCollection();
        collection.addCurrency(currency);

        byte[] packs = new byte[packTypes * 2];

        int read = readWholeArray(inputStream, packs);
        if (read != packTypes * 2)
            throw new IllegalStateException("Under-read the packs information");
        for (int i = 0; i < packTypes; i++) {
            int count = convertToInt(packs[i * 2], packs[i * 2 + 1]);
            if (count > 0)
                collection.addItem(_doubleByteCountItems.get(i), count);
        }

        int cardBytes = convertToInt(inputStream.read(), inputStream.read());
        byte[] cards = new byte[cardBytes];
        read = readWholeArray(inputStream, cards);
        if (read != cardBytes)
            throw new IllegalArgumentException("Under-read the cards information");
        for (int i = 0; i < cards.length; i++)
            if (cards[i] > 0) {
                final String blueprintId = _singleByteCountItems.get(i);
                collection.addItem(blueprintId, cards[i]);
            }

        return collection;
    }

    private MutableCardCollection deserializeCollectionVer3(BufferedInputStream inputStream) throws IOException {
        DefaultCardCollection collection = new DefaultCardCollection();

        int byte1 = inputStream.read();
        int byte2 = inputStream.read();
        int byte3 = inputStream.read();
        collection.addCurrency(convertToInt(byte1, byte2, byte3));

        int packTypes = convertToInt(inputStream.read());

        byte[] packs = new byte[packTypes * 2];

        int read = readWholeArray(inputStream, packs);
        if (read != packTypes * 2)
            throw new IllegalStateException("Under-read the packs information");
        for (int i = 0; i < packTypes; i++) {
            int count = convertToInt(packs[i * 2], packs[i * 2 + 1]);
            if (count > 0)
                collection.addItem(_doubleByteCountItems.get(i), count);
        }

        int cardBytes = convertToInt(inputStream.read(), inputStream.read());
        byte[] cards = new byte[cardBytes];
        read = readWholeArray(inputStream, cards);
        if (read != cardBytes)
            throw new IllegalArgumentException("Under-read the cards information");
        for (int i = 0; i < cards.length; i++) {
            int count = convertToInt(cards[i]);
            if (count > 0) {
                final String blueprintId = _singleByteCountItems.get(i);
                collection.addItem(blueprintId, count);
            }
        }

        return collection;
    }

    private MutableCardCollection deserializeCollectionVer4(BufferedInputStream inputStream) throws IOException {
        DefaultCardCollection collection = new DefaultCardCollection();

        int byte1 = inputStream.read();
        int byte2 = inputStream.read();
        int byte3 = inputStream.read();
        int currency = convertToInt(byte1, byte2, byte3);
        collection.addCurrency(currency);

        int packTypes = convertToInt(inputStream.read());

        byte[] packs = new byte[packTypes * 2];

        int read = readWholeArray(inputStream, packs);
        if (read != packTypes * 2)
            throw new IllegalStateException("Under-read the packs information");
        for (int i = 0; i < packTypes; i++) {
            int count = convertToInt(packs[i * 2], packs[i * 2 + 1]);
            if (count > 0)
                collection.addItem(_doubleByteCountItems.get(i), count);
        }

        int cardBytes = convertToInt(inputStream.read(), inputStream.read());
        byte[] cards = new byte[cardBytes];
        read = readWholeArray(inputStream, cards);
        if (read != cardBytes)
            throw new IllegalArgumentException("Under-read the cards information");
        for (int i = 0; i < cards.length; i++) {
            int count = convertToInt(cards[i]);
            if (count > 0) {
                final String blueprintId = _singleByteCountItems.get(i);
                collection.addItem(blueprintId, count);
            }
        }

        Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        collection.setExtraInformation(_mapper.convertValue(
                _mapper.readTree(JsonUtils.readJson(reader)), new TypeReference<>() {
                }));

        return collection;
    }

    private int convertToInt(int... bytes) {
        int result = 0;
        for (int i = 0; i < bytes.length; i++) {
            int value = bytes[i] << ((bytes.length - i - 1) * 8);
            if (value < 0)
                value += 256;
            result += value;
        }
        return result;
    }

    private static int readWholeArray(InputStream stream, byte[] array) throws IOException {
        int readCount = 0;
        while (true) {
            int readAmount = stream.read(array, readCount, array.length - readCount);
            if (readAmount < 0)
                return readCount;
            readCount += readAmount;
            if (readCount == array.length)
                return readCount;
        }
    }
}
