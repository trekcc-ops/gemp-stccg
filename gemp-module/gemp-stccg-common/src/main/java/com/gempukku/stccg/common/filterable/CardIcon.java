package com.gempukku.stccg.common.filterable;

@SuppressWarnings("unused")
public enum CardIcon implements Filterable {
    AU_ICON("Alternate Universe", "https://www.trekcc.org/forum/images/smilies/icon_1e_au.jpg"),
    AU_ICON_2E("Alternate Universe", "https://www.trekcc.org/images/icons/au.gif"),
    CLASSIC_FILMS_ICON("Classic Films", "https://www.trekcc.org/forum/images/smilies/icon_classicfilms.png"),
    COMMAND("Command", "https://www.trekcc.org/forum/images/smilies/icon_command.gif"),
    COMMUNICATIONS_SUBCOMMAND("Communications", "https://www.trekcc.org/forum/images/smilies/icon_sc_comm.png"),
    DEFENSE_SUBCOMMAND("Defense", "https://www.trekcc.org/forum/images/smilies/icon_sc_def.png"),
    DS9_ICON("Deep Space Nine", "https://www.trekcc.org/forum/images/smilies/icon_ds91e.png"),
    EARTH("Earth", "https://www.trekcc.org/forum/images/smilies/icon_earth.gif"),
    ENT_ERA("22nd Century", "https://www.trekcc.org/forum/images/smilies/icon_22nd_century.png"),
    FUTURE("Future","https://www.trekcc.org/images/icons/future.gif"),
    FIRST_CONTACT_ICON("First Contact", "https://www.trekcc.org/images/icons/1e/icon_first_contact.jpg"),
    MAQUIS("Maquis", "https://www.trekcc.org/forum/images/smilies/icon_maquis.png"),
    NAVIGATION_SUBCOMMAND("Navigation", "https://www.trekcc.org/forum/images/smilies/icon_sc_nav.png"),
    ORB("Orb", "https://www.trekcc.org/forum/images/smilies/icon_orb.jpg"),
    PAST("Past","https://www.trekcc.org/images/icons/past.gif"),
    REACTOR_CORE("Reactor Core", "https://www.trekcc.org/forum/images/smilies/icon_reactorcore.jpg"),
    SKILL_DOT("Skill dot", "https://www.trekcc.org/forum/images/smilies/icon_skilldot.png"),
    STAFF("Staff", "https://www.trekcc.org/forum/images/smilies/icon_staff.gif"),
    TNG_ICON("The Next Generation", "https://www.trekcc.org/forum/images/smilies/icon_tng1e.png"),
    WARP_CORE("Warp Core", "https://www.trekcc.org/forum/images/smilies/icon_warpcore.jpg");

    private final String _iconURL;
    private final String _humanReadable;

    CardIcon(String humanReadable, String iconURL) {
        _humanReadable = humanReadable;
        _iconURL = iconURL;
    }
    public String getIconURL() { return _iconURL; }
    public String toHTML() { return "<img src='" + _iconURL + "' class='inline-icon' title='" + _humanReadable + "'>"; }
}