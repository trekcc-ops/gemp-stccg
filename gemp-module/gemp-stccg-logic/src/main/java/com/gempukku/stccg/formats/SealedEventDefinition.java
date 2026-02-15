package com.gempukku.stccg.formats;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.GenericCardItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SealedEventDefinition {
    private final String _name;
    private final String _id;
    private GameFormat _format;
    private final String _formatId;
    private final List<List<GenericCardItem>> _seriesProduct = new ArrayList<>();

    public SealedEventDefinition(
            @JsonProperty(value = "name", required = true)
            String name,
            @JsonProperty(value = "id", required = true)
                    String id,
            @JsonProperty(value = "format", required = true)
                    String format,
            @JsonProperty(value = "seriesProduct", required = true)
                    List<List<String>> product) {
        _name = name;
        _id = id;
        _formatId = format;

        for(List<String> series : product) {
            List<GenericCardItem> items = new ArrayList<>();
            for(String def : series) {
                var item = GenericCardItem.createItem(def);
                items.add(item);
            }

            _seriesProduct.add(items);
        }
    }

    public int GetSeriesCount() { return _seriesProduct.size(); }

    @JsonProperty("name")
    public String getName() { return _name; }
    @JsonProperty("id")
    public String getId() { return _id; }
    public GameFormat getFormat() { return _format; }
    @JsonProperty("format")
    public String getFormatId() { return _formatId; }
    public List<GenericCardItem> GetProductForSeries(int serie) {
        return Collections.unmodifiableList(_seriesProduct.get(serie));
    }

    @JsonProperty("seriesProduct")
    public List<List<String>> getSeriesProductForSerialization() {
        List<List<String>> result = new ArrayList<>();
        for (List<GenericCardItem> stringList : _seriesProduct) {
            List<String> newList = new ArrayList<>();
            result.add(newList);
            for (GenericCardItem item : stringList) {
                newList.add(item.toString());
            }
        }
        return result;
    }

    public void assignFormat(GameFormat format) {
        _format = format;
    }
}