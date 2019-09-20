package com.gempukku.lotro.draft;

import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.draft2.SoloDraft;
import com.gempukku.lotro.draft2.SoloDraftDefinitions;
import com.gempukku.lotro.game.CardSets;
import com.gempukku.lotro.game.DefaultAdventureLibrary;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.gempukku.lotro.game.packs.SetDefinition;

import java.io.File;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

public class HobbitDraftTest {
    public static void main(String[] args) {
        LotroCardBlueprintLibrary library = new LotroCardBlueprintLibrary();
        final String property = System.getProperty("user.dir");
        String projectRoot = new File(property).getAbsolutePath();

        library.init(new File(projectRoot + "/gemp-lotr-async/src/main/web/cards"), new CardSets());

        CollectionsManager collectionsManager = new CollectionsManager(null, null, null, library);
        DefaultAdventureLibrary defaultAdventureLibrary = new DefaultAdventureLibrary();
        LotroFormatLibrary lotroFormatLibrary = new LotroFormatLibrary(defaultAdventureLibrary, library);
        CardSets cardSets = new CardSets();

        SoloDraftDefinitions soloDraftDefinitions = new SoloDraftDefinitions(collectionsManager, library, lotroFormatLibrary, cardSets.getSetDefinitions());

        final SoloDraft hobbitDraft = soloDraftDefinitions.getSoloDraft("hobbit_draft");

        long collectionType = 1568486003481L;
        final int playerId = 1000;

        Map<String, Integer> availableCards = new TreeMap<>(
                new Comparator<String>() {
                    @Override
                    public int compare(String o1, String o2) {
                        int set1 = Integer.parseInt(o1.substring(0, o1.indexOf('_')));
                        int set2 = Integer.parseInt(o2.substring(0, o2.indexOf('_')));
                        if (set1 != set2)
                            return set1 - set2;
                        int card1 = Integer.parseInt(o1.substring(o1.indexOf('_') + 1));
                        int card2 = Integer.parseInt(o2.substring(o2.indexOf('_') + 1));
                        return card1 - card2;
                    }
                });

        for (int i = 0; i < 1000; i++) {
            // Take an example seed
            long seed = getSeed(String.valueOf(collectionType + i), playerId);

            int stage = 0;

            while (hobbitDraft.hasNextStage(seed, stage)) {
                final Iterable<SoloDraft.DraftChoice> availableChoices = hobbitDraft.getAvailableChoices(seed, stage);
                for (SoloDraft.DraftChoice availableChoice : availableChoices) {
                    final String blueprintId = availableChoice.getBlueprintId();
                    availableCards.merge(blueprintId, 1,
                            (integer, integer2) -> integer + integer2);
                }

                stage++;
            }
        }

        for (Map.Entry<String, Integer> entry : availableCards.entrySet()) {
            final String blueprint = entry.getKey();
            String set = blueprint.substring(0, blueprint.indexOf('_'));
            final SetDefinition setDefinition = cardSets.getSetDefinitions().get(set);
            final String cardRarity = setDefinition.getCardRarity(blueprint);
            System.out.println(blueprint + " (" + cardRarity + "): " + entry.getValue());
        }
    }

    private static long getSeed(String collectionType, int playerId) {
        return collectionType.hashCode() + playerId * 8963;
    }
}
