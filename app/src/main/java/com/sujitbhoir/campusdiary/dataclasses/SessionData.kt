package com.sujitbhoir.campusdiary.dataclasses

import com.google.firebase.Timestamp

data class SessionData(
    var id : String = "",
    var members : List<String> = listOf<String>(),
    var lastmsg : String = "",
    var sender : String = "",
    var lasttime : Timestamp = Timestamp.now()
)
