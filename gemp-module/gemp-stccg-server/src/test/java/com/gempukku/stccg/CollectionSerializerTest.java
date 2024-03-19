package com.gempukku.stccg;

import com.gempukku.stccg.collection.CollectionSerializer;
import com.gempukku.stccg.cards.CardCollection;
import com.gempukku.stccg.collection.DefaultCardCollection;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class CollectionSerializerTest {
    private final CollectionSerializer _serializer = new CollectionSerializer();

    private CardCollection serializeAndDeserialize(DefaultCardCollection collection) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        _serializer.serializeCollection(collection, baos);
        return _serializer.deserializeCollection(new ByteArrayInputStream(baos.toByteArray()));
    }

    @Test
    public void testLotsOfCurrency() throws IOException {
        DefaultCardCollection collection = new DefaultCardCollection();
        collection.addCurrency(127 * 255);

        assertEquals(127 * 255, serializeAndDeserialize(collection).getCurrency());
    }

}
