package com.gempukku.stccg.hall;

import java.util.Map;

public interface HallChannelVisitor {

    void addTable(String tableId, Map<String, String> props);

}