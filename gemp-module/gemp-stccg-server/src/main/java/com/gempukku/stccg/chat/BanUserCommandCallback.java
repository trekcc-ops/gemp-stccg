package com.gempukku.stccg.chat;

import com.gempukku.stccg.service.AdminService;

public class BanUserCommandCallback implements ChatCommandCallback {

    private final AdminService _adminService;

    BanUserCommandCallback(AdminService adminService) {
        _adminService = adminService;
    }
    @Override
    public void commandReceived(String from, String parameters, boolean admin) throws ChatCommandErrorException {
        if (admin) {
            String userId = parameters.strip();
            _adminService.banUser(userId);
        } else {
            throw new ChatCommandErrorException("Only administrator can ban users");
        }
    }
}