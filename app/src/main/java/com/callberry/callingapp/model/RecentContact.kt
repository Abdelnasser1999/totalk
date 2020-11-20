package com.callberry.callingapp.model

import androidx.room.Embedded

data class RecentContact(
    @Embedded
    var contact: Contact? = null,
    @Embedded
    var recent: Recent? = null
)
