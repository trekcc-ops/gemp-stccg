package com.gempukku.stccg.chat;

import com.gempukku.stccg.database.UserNotFoundException;
import com.gempukku.stccg.service.AdminService;

public class BanIpCommandCallback implements ChatCommandCallback {

    private final AdminService _adminService;

    BanIpCommandCallback(AdminService adminService) {
        _adminService = adminService;
    }
    @Override
    public void commandReceived(String from, String parameters, boolean admin)
            throws ChatCommandErrorException {
        if (admin) {
            String userId = parameters.strip();
            try {
                _adminService.banIp(userId);
            } catch(UserNotFoundException exp) {
                throw new ChatCommandErrorException("Unable to find user '" + userId + "' in database");
            }
        } else {
            throw new ChatCommandErrorException("Only administrator can ban users");
        }
    }
}