package com.gempukku.stccg.formats;

import com.gempukku.stccg.cards.GenericCardItem;
import com.gempukku.stccg.common.GameFormat;
import com.gempukku.stccg.common.JSONData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SealedEventDefinition {
    private final String _name;
    private final String _id;
    private final GameFormat _format;
    private final List<List<GenericCardItem>> _seriesProduct = new ArrayList<>();

    public SealedEventDefinition(String name, String id, GameFormat format, List<List<String>> product) {
        _name = name;
        _id = id;
        _format = format;

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

    public String GetID() { return _id; }
    public GameFormat GetFormat() { return _format; }
    public List<GenericCardItem> GetProductForSeries(int serie) { return Collections.unmodifiableList(_seriesProduct.get(serie)); }

    public JSONData.SealedTemplate Serialize() {
        return new JSONData.SealedTemplate() {{
           name = _name;
           id = _id;
           format = _format.getCode();
           seriesProduct = _seriesProduct.stream()
                   .map(x->x.stream().map(GenericCardItem::toString).collect(Collectors.toList()))
                   .collect(Collectors.toList());
        }};
    }
}