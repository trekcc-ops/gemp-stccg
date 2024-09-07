package com.gempukku.stccg;

import com.gempukku.stccg.cards.CardCollection;
import com.gempukku.stccg.cards.RarityPackBox;
import org.junit.Test;

import java.util.List;

public class RarityPackBoxTest extends AbstractLogicTest {
    @Test
    public void openingPacks() {
        RarityPackBox fellowshipBox = new RarityPackBox(_cardLibrary.getSetDefinitions().get("1"));
        for (int i=0; i<10; i++) {
            System.out.println("Pack: "+(i+1));
            final List<CardCollection.Item> items = fellowshipBox.openPack();
            for (CardCollection.Item item : items) {
                System.out.println(item.getBlueprintId());
            }
        }
    }
}
