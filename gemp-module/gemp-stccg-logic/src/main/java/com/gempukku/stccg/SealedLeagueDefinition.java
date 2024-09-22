package com.gempukku.stccg;

import com.gempukku.stccg.common.JSONDefs;
import com.gempukku.stccg.cards.CardCollection;
import com.gempukku.stccg.formats.GameFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SealedLeagueDefinition {
    private final String _name;
    private final String _id;
    private final GameFormat _format;
    private final List<List<CardCollection.Item>> _seriesProduct = new ArrayList<>();

    public SealedLeagueDefinition(String name, String id, GameFormat format, List<List<String>> product) {
        _name = name;
        _id = id;
        _format = format;

        for(var serie : product) {
            List<CardCollection.Item> items = new ArrayList<>();
            for(String def : serie) {
                var item = CardCollection.Item.createItem(def);
                items.add(item);
            }

            _seriesProduct.add(items);
        }
    }

    public int GetSeriesCount() { return _seriesProduct.size(); }

    public String GetName() { return _name; }
    public String GetID() { return _id; }
    public GameFormat GetFormat() { return _format; }
    public List<List<CardCollection.Item>> GetAllSeriesProducts() { return Collections.unmodifiableList(_seriesProduct); }
    public List<CardCollection.Item> GetProductForSeries(int serie) { return Collections.unmodifiableList(_seriesProduct.get(serie)); }

    public JSONDefs.SealedTemplate Serialize() {
        return new JSONDefs.SealedTemplate() {{
           Name = _name;
           ID = _id;
           Format = _format.getCode();
           SeriesProduct = _seriesProduct.stream()
                   .map(x->x.stream().map(CardCollection.Item::toString).collect(Collectors.toList()))
                   .collect(Collectors.toList());
        }};
    }
}
