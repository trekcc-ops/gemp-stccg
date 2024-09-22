package com.gempukku.stccg;

import com.gempukku.stccg.cards.GenericCardItem;
import com.gempukku.stccg.collection.RarityPackBox;
import org.junit.Test;

import java.util.List;

public class RarityPackBoxTest extends AbstractLogicTest {

    @Test
    public void openingPacks() {
        RarityPackBox fellowshipBox = new RarityPackBox(_cardLibrary.getSetDefinitions().get("101"));
        for (int i=0; i<10; i++) {
            System.out.println("Pack: "+(i+1));
            final List<GenericCardItem> items = fellowshipBox.openPack();
            for (GenericCardItem item : items) {
                System.out.println(item.getBlueprintId());
            }
        }
    }
}
