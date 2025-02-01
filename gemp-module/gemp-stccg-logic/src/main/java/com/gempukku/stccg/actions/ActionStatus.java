package com.gempukku.stccg.actions;

// ActionStatus is intended to be used by serialization
enum ActionStatus {
    virtual(false, false), // Selectable actions that haven't been selected, or unperformed subactions of other actions
    initiation_started(true, false), // Actions in progress that haven't been fully initiated
    initiation_failed(false, true), // Actions that have ended because they couldn't be fully initiated
    initiation_complete(true, false), // Actions that have been fully initiated and are being processed
    cancelled(false, true), // Actions that were cancelled after being initiated
    completed_success(false, false), // Actions that were successfully completed
    completed_failure(false, true); // Actions that were completed but failed

    private boolean _isInProgress;
    private boolean _wasFailed;

    ActionStatus(boolean isInProgress, boolean wasFailed) {
        _isInProgress = isInProgress;
        _wasFailed = wasFailed;
    }

    public boolean wasFailed() {
        return _wasFailed;
    }

    public boolean isInProgress() {
        return _isInProgress;
    }
}