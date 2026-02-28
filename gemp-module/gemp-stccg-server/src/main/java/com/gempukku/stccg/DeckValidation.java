package com.gempukku.stccg;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.GameType;
import com.gempukku.stccg.common.filterable.SubDeck;
import com.gempukku.stccg.formats.GameFormat;

import java.util.*;

public class DeckValidation {

    @JsonProperty("errors")
    private final List<String> _errors = new ArrayList<>();

    @JsonProperty("warnings")
    private final List<String> _warnings = new ArrayList<>();

    @JsonProperty("info")
    private final List<String> _info = new ArrayList<>();

    @JsonProperty("invalidBlueprintIds")
    private final Set<String> _invalidBlueprintIds = new HashSet<>();

    @JsonProperty("notAllowedCards")
    private final Set<String> _notAllowedCards = new HashSet<>();
    private final CardDeck _deck;
    private final CardBlueprintLibrary _library;
    private final GameFormat _format;

    public DeckValidation(CardDeck cardDeck, CardBlueprintLibrary library, GameFormat format) {
        _deck = cardDeck;
        _library = library;
        _format = format;
        validate();
    }

    @JsonIgnore
    public List<String> getAllErrors() {
        List<String> result = new ArrayList<>();
        if (!_invalidBlueprintIds.isEmpty()) {
            StringJoiner sj = new StringJoiner(",");
            for (String blueprintId : _invalidBlueprintIds) {
                sj.add(blueprintId);
            }
            String message = "Deck contains blueprint ids that cannot be identified : " + sj;
            result.add(message);
        }
        if (!_notAllowedCards.isEmpty()) {
            StringJoiner sj = new StringJoiner(",");
            for (String cardTitle : _notAllowedCards) {
                sj.add("'" + cardTitle + "'");
            }
            String message = "Deck contains cards not allowed in this format : " + sj;
            result.add(message);
        }
        result.addAll(_errors);
        return result;
    }

    private void validate() {
        validateDeckStructure();
        validateCardCopyLimits();

        for (String blueprintId : _deck.getAllCards()) {
            try {
                CardBlueprint blueprint = _library.getCardBlueprint(blueprintId);
                if (!_format.isCardAllowedInFormat(blueprint, _library)) {
                    _notAllowedCards.add(blueprint.getTitle());
                }
            } catch(CardNotFoundException exp) {
                _invalidBlueprintIds.add(blueprintId);
            }
        }

    }

    private void validateCardCopyLimits() {
        // Card count in deck
        Map<String, Integer> cardCountByName = new HashMap<>();
        Map<String, Integer> cardCountByBaseBlueprintId = new HashMap<>();

        for (String blueprintId : _deck.getAllCards()) {
            try {
                processCardCounts(_library, blueprintId, cardCountByName, cardCountByBaseBlueprintId);
            } catch (CardNotFoundException exp) {
                _invalidBlueprintIds.add(blueprintId);
            }
        }

        for (Map.Entry<String, Integer> count : cardCountByName.entrySet()) {
            if (count.getValue() > _format.getSameNameLimit()) {
                _errors.add("Deck contains more of the same card than allowed - " + count.getKey() + " (" +
                        count.getValue() + ">" + _format.getSameNameLimit() + "): " + count.getKey());
            }
        }
    }


    private void validateDeckStructure() {
        int _minimumDrawDeckSize = _format.getMinimumDrawDeckSize();
        int _maximumSeedDeckSize = _format.getMaximumSeedDeckSize();
        int drawDeckSize = _deck.getSubDeck(SubDeck.DRAW_DECK).size();
        if (drawDeckSize < _minimumDrawDeckSize) {
            String sb = "Draw deck contains below minimum number of cards: " +
                    drawDeckSize + "<" + _minimumDrawDeckSize + ".";
            _errors.add(sb);
        } else {
            _info.add("Draw deck has " + drawDeckSize + " cards.");
        }
        if (_format.getGameType() == GameType.FIRST_EDITION) {
            validateMissionsPile(_library, _deck);
            int seedDeckSize = _deck.getSubDeck(SubDeck.SEED_DECK).size();
            if (seedDeckSize > _maximumSeedDeckSize) {
                String result = "Seed deck contains more than maximum number of cards: " +
                        seedDeckSize + ">" + _maximumSeedDeckSize + ".";
                _errors.add(result);
            } else {
                _info.add("Seed deck has " + seedDeckSize + " cards.");
            }
        }
    }

    private void processCardCounts(CardBlueprintLibrary library, String blueprintId, Map<String, Integer> cardCountByName,
                                   Map<String, Integer> cardCountByBaseBlueprintId) throws CardNotFoundException {
        increaseCount(cardCountByName, library.getCardBlueprint(blueprintId).getTitle());
        increaseCount(cardCountByBaseBlueprintId, library.getBaseBlueprintId(blueprintId));
    }

    private void increaseCount(Map<String, Integer> counts, String name) {
        counts.merge(name, 1, Integer::sum);
    }

    private void validateMissionsPile(CardBlueprintLibrary library, CardDeck deck) {
        List<String> missionsPile = deck.getSubDeck(SubDeck.MISSIONS);
        if (_format.getMissions() > 0 && missionsPile.size() != _format.getMissions()) {
            _errors.add("Deck must contain exactly " + _format.getMissions() + " missions.");
        }
        List<String> uniqueLocations = new LinkedList<>();
        for (String blueprintId : missionsPile) {
            try {
                StringBuilder result = new StringBuilder();
                CardBlueprint blueprint = library.getCardBlueprint(blueprintId);
                if (blueprint.getCardType() != CardType.MISSION) {
                    result.append("Missions pile contains non-mission card: ").append(blueprint.getTitle()).append(".\n");
                } else if (!blueprint.isUniversal()) {
                    uniqueLocations.add(blueprint.getLocation());
                }
                _errors.add(result.toString());
            } catch(CardNotFoundException ignored) {
                _invalidBlueprintIds.add(blueprintId);
            }
        }
        List<String> distinctUniqueLocations = uniqueLocations.stream().distinct().toList();
        for (String location : distinctUniqueLocations) {
            StringBuilder result = new StringBuilder();
            int locationCount = Collections.frequency(uniqueLocations, location);
            if (locationCount > 1) {
                result.append("Deck has ").append(locationCount).append(" unique missions at location: ").append(location);
            }
            _errors.add(result.toString());
        }
    }

}