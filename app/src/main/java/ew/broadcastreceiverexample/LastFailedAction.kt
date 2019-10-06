package ew.broadcastreceiverexample

sealed class LastFailedAction {
    object CHOOSE_FILE: LastFailedAction()
    object DOWNLOAD: LastFailedAction()
}