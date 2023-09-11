package com.sujitbhoir.campusdiary.dataclasses

data class UserData(
    var id : String = "",
    var name : String = "",
    var email : String = "",
    var username : String = "",
    var age : String = "",
    var about : String = "",
    var gender : String = "",
    var campus : String = "",
    var mobileNo : String = "",
    var interests : List<String> = listOf()
)
