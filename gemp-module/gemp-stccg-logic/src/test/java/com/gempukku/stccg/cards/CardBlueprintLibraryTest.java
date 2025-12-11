package com.gempukku.stccg.cards;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.gempukku.stccg.AbstractLogicTest;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.common.SetDefinition;
import com.gempukku.stccg.common.filterable.*;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("MagicNumber")
public class CardBlueprintLibraryTest extends AbstractLogicTest {

    // Commented out lackey validation so that it isn't run every single time the server is built

    // TODO - Add check that all card images are valid URLs
    @Test
    public void AllBlueprintsAreBuilt() throws CardNotFoundException {
        assertTrue(_cardLibrary.checkLoadSuccess());
        assertEquals("Admiral McCoy", _cardLibrary.getCardBlueprint("106_014").getTitle());
        int totalCardCount = 0;

        for (SetDefinition set : _cardLibrary.getSetDefinitions().values()) {
/*            StringBuilder sb = new StringBuilder();
            sb.append("Set ").append(set.getSetId()).append(" '").append(set.getSetName());
            sb.append("' - ").append(TextUtils.plural(set.getAllCards().size(), "card"));

            GameType gameType = set.getGameType();
            String gameTypeName = (gameType == null) ? "null" : gameType.name();

            sb.append(" [").append(gameTypeName).append("]");
            System.out.println(sb); */
            totalCardCount += set.getAllCards().size();
        }

        assertEquals(totalCardCount, _cardLibrary.getAllBlueprintIds().size());
    }

    @Test
    public void alternateBlueprintCheck() throws CardNotFoundException {
        /* The blueprint IDs below were arbitrarily selected. At time of writing of this test, no card blueprints are
            using base blueprints. If blueprints do start to use this property in the future, this test will need to
            be rewritten.
         */

        _cardLibrary.getCardBlueprint("101_218").setBaseBlueprintId("101_217");
        _cardLibrary.getCardBlueprint("113_003").setBaseBlueprintId("101_217");

        _cardLibrary.reloadMappings();

        assertEquals("101_217", _cardLibrary.getBaseBlueprintId("101_218"));
        assertEquals("101_217", _cardLibrary.getBaseBlueprintId("113_003"));

        assertTrue(_cardLibrary.hasAlternateInSet("101_218", "113"));
        assertEquals(2, _cardLibrary.getAllAlternates("113_003").size());
    }

//    @Test
    public void checkAgainstLackey() {
        createLackeyLibrary();
        int matchCount = 0;
        for (String blueprintId : _cardLibrary.getAllBlueprintIds()) {
            CardBlueprint blueprint = _cardLibrary.get(blueprintId);
            if (blueprint.getGameType() == GameType.FIRST_EDITION) {
                String setId = blueprintId.substring(0,3);
                SetDefinition setDefinition = _cardLibrary.getSetDefinition(setId);
                if (setDefinition == null) {
                    System.out.println("Could not find setDefinition for " + setId);
                }
                CardData lackeyData = _newLibraryMap.get(blueprintId);
                if (lackeyData == null) {
                    System.out.println("Could not find lackeyData for " + blueprintId + " " + blueprint.getTitle());
                } else {
/*                    if (getBlueprintMatch(blueprint, lackeyData)) {
                        matchCount++;
                    } else {
                        System.out.println("Card " + blueprintId + " (" + blueprint.getTitle() + ") did not validate successfully.");
                    } */
                }
            }
        }
        System.out.println(matchCount + " matches");
    }

    List<CardData> _newLibrary = new LinkedList<>();
    Map<String, CardData> _newLibraryMap = new HashMap<>();

    public void createLackeyLibrary() {
        File input;
        MappingIterator<Map<?, ?>> mappingIterator;
        input = new File("..\\gemp-stccg-logic\\src\\test\\resources\\Lackey_upload.csv");
        try {
            CsvSchema csv = CsvSchema.emptySchema().withHeader();
            CsvMapper csvMapper = new CsvMapper();
            ObjectMapper jsonMapper = _cardLibrary.getMapper();
            List<Object> list = csvMapper.reader().forType(Map.class).with(csv).readValues(input).readAll();
            JsonNode jsonMap = jsonMapper.valueToTree(list);
            List<JsonNode> lackeyCardData = jsonMapper.readerForListOf(CardData.class).readValue(jsonMap);
            int x = 5;
/*            for (Map<?, ?> card : list) {
                CardData cardInfo = new CardData();
                String blueprintId = cardInfo._blueprintId;
                _newLibraryMap.put(blueprintId, cardInfo);
                _newLibrary.add(cardInfo);
            }*/
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /*
    private boolean getBlueprintMatch(CardBlueprint blueprint, CardData lackeyData) {
        ObjectMapper jsonMapper = _cardLibrary.getMapper();
        String blueprintId = blueprint.getBlueprintId();
        if (lackeyData == null) {
            System.out.println("Could not find lackeyData for " + blueprint.getBlueprintId() + " " + blueprint.getTitle());
            return false;
        }
        String setId = blueprintId.substring(0,3);
        SetDefinition setDefinition = _cardLibrary.getSetDefinition(setId);
        if (setDefinition == null) {
            System.out.println("Could not find setDefinition for " + setId);
            return false;
        }
        String setName = setDefinition.getSetName();
        String lackeyCardName = lackeyData._lackeyTitle.replace("("+setName+")","").trim();

        if (!Objects.equals(blueprint.getTitle(), lackeyCardName)) {
            System.out.println("Gemp: " + blueprint.getTitle() + "; Lackey: " + lackeyCardName);
            return false;
        }

        if (!checkPropertyLogo(blueprint, lackeyData)) {
            return false;
        }

        if (!checkUniqueness(blueprint, lackeyData)) {
            return false;
        }

        try {
            CardType lackeyType = jsonMapper.readValue(lackeyData._type, CardType.class);
            assertEquals(blueprint.getCardType(), lackeyType);
        } catch(IOException exp) {
            System.out.println(exp.getMessage());
            return false;
        }

        // TODO: Mission type
        // TODO: Affiliation

        try {
            if (!checkClass(blueprint, lackeyData._class)) {
                return false;
            }
        } catch(IOException exp) {
            System.out.println(exp.getMessage());
            return false;
        }

        if (!checkAttributes(blueprint, lackeyData)) {
            return false;
        }

        // TODO: Points
        // TODO: Region
        // TODO: Quadrant
        // TODO: Span

        try {
            if (!checkIcons(blueprint, lackeyData)) {
                return false;
            }
        } catch(InvalidCardDefinitionException exp) {
            System.out.println(exp.getMessage());
            return false;
        }

        try {
            if (!checkStaffing(blueprint, lackeyData)) {
                return false;
            }
        } catch(InvalidCardDefinitionException exp) {
            System.out.println(exp.getMessage());
            return false;
        }

        // TODO: Characteristics/Keywords
        // TODO: Persona
        // TODO: Command
        // TODO: Lore
        // TODO: Text

        return true;
    }

    private boolean checkUniqueness(CardBlueprint blueprint, CardData lackeyData) {
        boolean result;
        List<CardType> types = new LinkedList<>();
        String lackeyUniqueness = lackeyData._uniqueness;
        types.add(CardType.MISSION);
        types.add(CardType.FACILITY);
        types.add(CardType.PERSONNEL);
        types.add(CardType.SHIP);
        types.add(CardType.SITE);
        if (!types.contains(blueprint.getCardType())) {
            result = Objects.equals(lackeyData._uniqueness, "");
        } else {
            result = switch (blueprint.getUniqueness()) {
                case UNIVERSAL -> Objects.equals(lackeyUniqueness, "univ");
                case ENIGMA -> Objects.equals(lackeyUniqueness, "enig");
                case UNIQUE -> Objects.equals(lackeyUniqueness, "");
            };
        }
        if (!result) {
            System.out.println(blueprint.getUniqueness() + ", " + lackeyData._uniqueness);
            System.out.println("Uniqueness doesn't match for blueprint " + blueprint.getBlueprintId() + " " +
                    blueprint.getTitle());
        }
        return result;
    }

    private boolean checkAttributes(CardBlueprint blueprint, CardData lackeyData) {
        List<String> gempAttr = new LinkedList<>();
        List<String> lackeyAttr = new LinkedList<>();

        if (blueprint.getCardType() == CardType.PERSONNEL) {
            gempAttr.add(String.valueOf(blueprint.getIntegrity()));
            gempAttr.add(String.valueOf(blueprint.getCunning()));
            gempAttr.add(String.valueOf(blueprint.getStrength()));
        } else {
            gempAttr.add(String.valueOf(blueprint.getRange()));
            gempAttr.add(String.valueOf(blueprint.getWeapons()));
            gempAttr.add(String.valueOf(blueprint.getShields()));
        }

        for (String attr : lackeyData._attributes) {
            String attribute;
            attribute = attr.replace("-X", "");
            if (attribute.isEmpty()) {
                attribute = "0";
            }
            lackeyAttr.add(attribute);
        }

        for (int i = 0; i < 3; i++) {
            if (!Objects.equals(gempAttr.get(i), lackeyAttr.get(i))) {
                System.out.println("Attributes don't match for blueprint" + blueprint.getBlueprintId() + " " + blueprint.getTitle());
                return false;
            }
        }

        return true;

    }

    private boolean checkClass(CardBlueprint blueprint, String lackeyClassText) throws IOException {
        ObjectMapper jsonMapper = _cardLibrary.getMapper();

        if (blueprint.getCardType() == CardType.PERSONNEL) {
            SkillName gempClass = blueprint.getClassification();
            SkillName lackeyClass = jsonMapper.readValue(lackeyClassText, SkillName.class);
            if (gempClass != lackeyClass) {
                throw new InvalidCardDefinitionException("Classification mismatch for blueprint" + blueprint.getBlueprintId() + " " + blueprint.getTitle());
            } else {
                return true;
            }
        }
        if (blueprint.getCardType() == CardType.FACILITY) {
            FacilityType gempType = blueprint.getFacilityType();
            FacilityType lackeyType = jsonMapper.readValue(lackeyClassText, FacilityType.class);
            if (gempType != lackeyType) {
                System.out.println("Facility type mismatch for blueprint" + blueprint.getBlueprintId() + " " + blueprint.getTitle());
                return false;
            } else {
                return true;
            }
        }
        if (blueprint.getCardType() == CardType.SHIP) {
            ShipClass gempClass = blueprint.getShipClass();
            lackeyClassText = lackeyClassText.replace(" Class","");
            if (lackeyClassText.startsWith("D'Kora-Class")) {
                lackeyClassText = "D'Kora";
            }
            ShipClass lackeyClass = jsonMapper.readValue(lackeyClassText, ShipClass.class);
            if (gempClass != lackeyClass) {
                System.out.println("Ship class mismatch for blueprint" + blueprint.getBlueprintId() + " " + blueprint.getTitle());
                return false;
            } else {
                return true;
            }
        }
        return Objects.equals(lackeyClassText, "");
    }

    private boolean checkIcons(CardBlueprint blueprint, CardData lackeyData) throws InvalidCardDefinitionException {
        StringBuilder sb = new StringBuilder();
        List<CardIcon> icons = blueprint.getIcons();
        for (CardIcon icon : icons) {
            String iconText = switch(icon) {
                case AU_ICON -> "[AU]";
                case COMMAND -> "[Cmd]";
                case COMMUNICATIONS_SUBCOMMAND -> "[Com]";
                case DEFENSE_SUBCOMMAND -> "[Def]";
                case ENT_ERA -> "[22]";
                case FIRST_CONTACT_ICON -> "[FC]";
                case MAQUIS -> "[Maq]";
                case NAVIGATION_SUBCOMMAND -> "[Nav]";
                case ORB -> "[Orb]";
                case STAFF -> "[Stf]";
                case WARP_CORE -> "[WC]";
                default -> throw new InvalidCardDefinitionException("Unable to map icon");
            };
            sb.append(iconText);
        }

        String lackeyIcons = lackeyData._icons;
        if (lackeyData._release.equals("fc")) {
            lackeyIcons = lackeyIcons + "[FC]";
        }


        String[] gempIcons = sb.toString().split("]");
        List<String> gempIconsList = new LinkedList<>();
        for (String icon : gempIcons) {
            String iconStripped = icon.replace("[","").replace("]","").trim();
            if (!iconStripped.isEmpty()) {
                gempIconsList.add(iconStripped);
            }
        }

        String[] lackeyIconsArr = lackeyIcons.split("]");
        List<String> lackeyIconsList = new LinkedList<>();
        for (String icon : lackeyIconsArr) {
            String iconStripped = icon.replace("[","").replace("]","").trim();
            if (!iconStripped.isEmpty()) {
                lackeyIconsList.add(iconStripped);
            }
        }

        Collections.sort(gempIconsList);
        Collections.sort(lackeyIconsList);

        String gempIconCheck = StringUtils.join(gempIconsList, ",");
        String lackeyIconCheck = StringUtils.join(lackeyIconsList, ",");
        return gempIconCheck.equals(lackeyIconCheck);
    }

    private boolean checkPropertyLogo(CardBlueprint blueprint, CardData lackeyData) {
        String lackeyLogo = lackeyData._property;
        PropertyLogo blueprintLogo = blueprint.getPropertyLogo();
        return switch(blueprintLogo) {
            case TOS_LOGO -> lackeyLogo.equals("TOS");
            case TNG_LOGO -> lackeyLogo.equals("TNG");
            case DS9_LOGO -> lackeyLogo.equals("DS9");
            case VOY_LOGO -> lackeyLogo.equals("VOY");
            case ENT_LOGO -> lackeyLogo.equals("ENT");
            case TAS_LOGO -> lackeyLogo.equals("TAS");
            case TMP_LOGO -> lackeyLogo.equals("TMP");
            case ST2_LOGO -> lackeyLogo.equals("TWOK");
            case ST3_LOGO -> lackeyLogo.equals("TSfS");
            case ST4_LOGO -> lackeyLogo.equals("TVH");
            case ST5_LOGO -> lackeyLogo.equals("TFF");
            case ST6_LOGO -> lackeyLogo.equals("TUC");
            case GENERATIONS_LOGO -> lackeyLogo.equals("Gen");
            case FIRST_CONTACT_LOGO -> lackeyLogo.equals("FC");
            case INSURRECTION_LOGO -> lackeyLogo.equals("Ins");
            case NEMESIS_LOGO -> lackeyLogo.equals("Nem");
            case ARMADA_LOGO -> lackeyLogo.equals("Armada");
            case KLINGON_CHALLENGE_LOGO -> lackeyLogo.equals("AKC");
            case CCG_LOGO -> lackeyLogo.equals("CCG");
            case null -> lackeyLogo.isEmpty();
        };
    }

    private boolean checkStaffing(CardBlueprint blueprint, CardData lackeyData) throws InvalidCardDefinitionException {
        StringBuilder sb = new StringBuilder();
        List<CardIcon> icons = blueprint.getStaffing();
        for (CardIcon icon : icons) {
            String iconText = switch(icon) {
                case AU_ICON -> "[AU]";
                case COMMAND -> "[Cmd]";
                case COMMUNICATIONS_SUBCOMMAND -> "[Com]";
                case DEFENSE_SUBCOMMAND -> "[Def]";
                case ENT_ERA -> "[22]";
                case FIRST_CONTACT_ICON -> "[FC]";
                case MAQUIS -> "[Maq]";
                case NAVIGATION_SUBCOMMAND -> "[Nav]";
                case ORB -> "[Orb]";
                case STAFF -> "[Stf]";
                case WARP_CORE -> "[WC]";
                default -> throw new InvalidCardDefinitionException("Unable to map icon");
            };
            sb.append(iconText);
        }

        String[] gempIcons = sb.toString().split("]");
        List<String> gempIconsList = new LinkedList<>();
        for (String icon : gempIcons) {
            String iconStripped = icon.replace("[","").replace("]","").trim();
            if (!iconStripped.isEmpty()) {
                gempIconsList.add(iconStripped);
            }
        }

        String[] lackeyIconsArr = lackeyData._staffing.split("]");
        List<String> lackeyIconsList = new LinkedList<>();
        for (String icon : lackeyIconsArr) {
            String iconStripped = icon.replace("[","").replace("]","").trim();
            if (!iconStripped.isEmpty()) {
                lackeyIconsList.add(iconStripped);
            }
        }

        Collections.sort(gempIconsList);
        Collections.sort(lackeyIconsList);

        String gempIconCheck = StringUtils.join(gempIconsList, ",");
        String lackeyIconCheck = StringUtils.join(lackeyIconsList, ",");
        return gempIconCheck.equals(lackeyIconCheck);
    }
*/

}