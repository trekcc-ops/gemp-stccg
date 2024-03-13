package com.gempukku.stccg.common.filterable.lotr;

import com.gempukku.stccg.common.filterable.Filterable;

public enum Culture implements Filterable {
    DWARVEN(true),
    ELVEN(true),
    GANDALF(true),
    GOLLUM(true), GONDOR(true), ROHAN(true), SHIRE(true),
    DUNLAND(false), ISENGARD(false), MEN(false), MORIA(false), ORC(false), RAIDER(false),
    SAURON(false), URUK_HAI(false), WRAITH(false),
    FALLEN_REALMS(false, false),
	
	//Additional Hobbit Draft cultures
	ESGAROTH(true), GUNDABAD(false), MIRKWOOD(false), SMAUG(false), SPIDER(false), TROLL(false);

    Culture(boolean fp) {
        this(fp, true);
    }

    Culture(boolean fp, boolean official) {
    }

}
