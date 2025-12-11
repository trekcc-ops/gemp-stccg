package com.gempukku.stccg;

import com.gempukku.stccg.common.SetDefinition;
import com.gempukku.stccg.collection.CollectionsManager;
import com.gempukku.stccg.draft.DraftChoice;
import com.gempukku.stccg.draft.SoloDraft;
import com.gempukku.stccg.draft.DraftFormatLibrary;
import com.gempukku.stccg.formats.FormatLibrary;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

public class DraftTests extends AbstractServerTest {
    @Test
    public void DraftTest() {
        CollectionsManager collectionsManager =
                new CollectionsManager(null, null, null, _cardLibrary);
        FormatLibrary formatLibrary = new FormatLibrary(_cardLibrary);

        DraftFormatLibrary draftFormatLibrary =
                new DraftFormatLibrary(_cardLibrary, formatLibrary);

        final SoloDraft soloDraft = draftFormatLibrary.getSoloDraft("test_draft");

        long collectionType = 1568486003481L;

        Map<String, Integer> availableCards = new TreeMap<>(
                (o1, o2) -> {
                    int set1 = Integer.parseInt(o1.substring(0, o1.indexOf('_')));
                    int set2 = Integer.parseInt(o2.substring(0, o2.indexOf('_')));
                    if (set1 != set2)
                        return set1 - set2;
                    int card1 = Integer.parseInt(o1.substring(o1.indexOf('_') + 1));
                    int card2 = Integer.parseInt(o2.substring(o2.indexOf('_') + 1));
                    return card1 - card2;
                });

        for (int i = 0; i < 100; i++) {
            // Take an example seed
            long seed = getSeed(String.valueOf(collectionType + i));

            int stage = 0;

            while (soloDraft.hasNextStage(stage)) {
                final Iterable<DraftChoice> availableChoices =
                        soloDraft.getAvailableChoices(seed, stage, null);
                for (DraftChoice availableChoice : availableChoices) {
                    final String blueprintId = availableChoice.getBlueprintId();
                    availableCards.merge(blueprintId, 1,
                            Integer::sum);
                }

                stage++;
            }
        }

        for (Map.Entry<String, Integer> entry : availableCards.entrySet()) {
            final String blueprint = entry.getKey();
            String set = blueprint.substring(0, blueprint.indexOf('_'));
            final SetDefinition setDefinition = _cardLibrary.getSetDefinitions().get(set);
            final String cardRarity = setDefinition.getCardRarity(blueprint);
//            System.out.println(blueprint + " (" + cardRarity + "): " + entry.getValue());
        }
    }

    @Test
    public void testRandomness() {
        doRandomTest(4, false);
        doRandomTest(3, false);
        doRandomTest(4, true);
        doRandomTest(3, true);
    }

    private static void doRandomTest(int nextIntValue, boolean getFloatBeforeInt) {
/*        System.out.println("Get float before int: " + getFloatBeforeInt);
        System.out.println("Next int value: " + nextIntValue); */
        int[] values = new int[nextIntValue];
        for (int i = 0; i < 1000; i++) {
            Random rnd = new Random(i);
            if (getFloatBeforeInt)
                rnd.nextFloat();
            values[rnd.nextInt(nextIntValue)]++;
        }
/*        for (int i = 0; i < values.length; i++) {
            System.out.println(i + ": " + values[i]);
        } */
    }

    private static long getSeed(String collectionType) {
        return collectionType.hashCode() + 1000 * 8963L;
    }
}