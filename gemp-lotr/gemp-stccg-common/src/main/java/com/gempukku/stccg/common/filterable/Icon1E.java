package com.gempukku.stccg.common.filterable;

public enum Icon1E implements Filterable {
    AU_ICON("Alternate Universe", "https://www.trekcc.org/forum/images/smilies/icon_1e_au.jpg"),
    COMMAND("Command", "https://www.trekcc.org/forum/images/smilies/icon_command.gif"),
    COMMUNICATIONS_SUBCOMMAND("Communications", "https://www.trekcc.org/forum/images/smilies/icon_sc_comm.png"),
    DEFENSE_SUBCOMMAND("Defense", "https://www.trekcc.org/forum/images/smilies/icon_sc_def.png"),
    DS9_ICON("Deep Space Nine", "https://www.trekcc.org/forum/images/smilies/icon_ds91e.png"),
    ENT_ERA("22nd Century", "https://www.trekcc.org/forum/images/smilies/icon_22nd_century.png"),
    FIRST_CONTACT_ICON("First Contact", "no img"), // TODO - No image for this icon
    MAQUIS("Maquis", "https://www.trekcc.org/forum/images/smilies/icon_maquis.png"),
    REACTOR_CORE("Reactor Core", "https://www.trekcc.org/forum/images/smilies/icon_reactorcore.jpg"),
    SKILL_DOT("Skill dot", "https://www.trekcc.org/forum/images/smilies/icon_skilldot.png"),
    STAFF("Staff", "https://www.trekcc.org/forum/images/smilies/icon_staff.gif"),
    TNG_ICON("The Next Generation", "https://www.trekcc.org/forum/images/smilies/icon_tng1e.png"),
    WARP_CORE("Warp Core", "https://www.trekcc.org/forum/images/smilies/icon_warpcore.jpg");

    private final String _iconURL;
    private final String _humanReadable;

    Icon1E(String humanReadable, String iconURL) {
        _humanReadable = humanReadable;
        _iconURL = iconURL;
    }

    public String getIconURL() { return _iconURL; }

    public String getHumanReadable() { return _humanReadable; }
    public String toHTML() { return "<img src='" + _iconURL + "' class='inline-icon' title='" + _humanReadable + "'>"; }
}