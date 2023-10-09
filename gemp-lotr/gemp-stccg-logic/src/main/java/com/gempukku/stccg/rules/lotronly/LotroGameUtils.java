package com.gempukku.stccg.rules.lotronly;

import com.gempukku.stccg.cards.DefaultActionContext;
import com.gempukku.stccg.cards.LotroCardBlueprint;
import com.gempukku.stccg.common.filterable.Culture;
import com.gempukku.stccg.rules.GameUtils;

public class LotroGameUtils extends GameUtils {

    public static String getDeluxeCardLink(String blueprintId, LotroCardBlueprint blueprint) {
        var culture = blueprint.getCulture();
        var cultureString = getCultureImage(culture, null);
        return "<div class='cardHint' value='" + blueprintId + "'>" + cultureString
                + (blueprint.isUnique() ? "·" : "") + " " + LotroGameUtils.getFullName(blueprint) + "</div>";
    }

    public static String getCultureImage(String cultureName) {
        Culture culture = Culture.findCulture(cultureName);
        if(culture == null)
            return null;

        return getCultureImage(culture);
    }

    public static String getCultureImage(Culture culture, String override) {
        if(override == null || override.isEmpty()) {
            override = culture.toString().toLowerCase();
        }
        return "<span class='cultureHint' ><img src='images/cultures/" + override + ".png'></span>";
    }

    public static String getCultureImage(Culture culture) {
        return "<span class='cultureHint' value='" + culture.toString() + "'><img src='images/cultures/" + culture.toString().toLowerCase() + ".png'> "
                + culture.getHumanReadable() + "</span>";
    }

    public static String SubstituteText(String text, DefaultActionContext context)
    {
        String result = text;
        while (result.contains("{")) {
            int startIndex = result.indexOf("{");
            int endIndex = result.indexOf("}");
            String memory = result.substring(startIndex + 1, endIndex);
            String culture = getCultureImage(memory);
            if(culture != null) {
                result = result.replace("{" + memory + "}", culture);
            }
            else if(context != null){
                String cardNames = LotroGameUtils.getAppendedNames(context.getCardsFromMemory(memory));
                if(cardNames.equalsIgnoreCase("none")) {
                    try {
                        cardNames = context.getValueFromMemory(memory);
                    }
                    catch(IllegalArgumentException ex) {
                        cardNames = "none";
                    }
                }
                result = result.replace("{" + memory + "}", cardNames);
            }
        }

        return result;
    }

    public static int getRegion(int siteNumber) {
        return 1 + ((siteNumber - 1) / 3);
    }

}
