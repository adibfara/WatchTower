package com.snakydesign.watchtower.android

import android.app.Application
import android.app.Notification
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow


object WatchTowerAndroid {
    fun notificationFlow(application: Application, serverPort: Int): Flow<Notification> {
        return emptyFlow()
        // no-op
    }
}