package com.sujitbhoir.campusdiary.dataclasses

import com.google.firebase.Timestamp
import java.sql.Time

data class MessageData(
    var id :String = "",
    var msg : String = "",
    var time: Timestamp = Timestamp.now(),
    var sender : String = "",
    var img : String = ""
)
