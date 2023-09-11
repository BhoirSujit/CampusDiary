package com.sujitbhoir.campusdiary.dataclasses

data class ReqData(
    var sender : String = "",
    var senderName : String = "",
    var senderUName : String = "",
    var receiver : String = "",
    var receiverName: String = "",
    var message : String = "",
    var time : String = "",
    var isRestrict : Boolean = false
)
