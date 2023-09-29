package com.sujitbhoir.campusdiary.dataclasses

import com.google.firebase.Timestamp

data class ReqData(
    var id : String = "",
    var sender : String = "",
    var receiver : String = "",
    var message : String = "",
    var time : Timestamp = Timestamp.now(),
    var status : String = ""

)
