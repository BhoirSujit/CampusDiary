package com.sujitbhoir.campusdiary.dataclasses

data class CommunityData(
    var id : String = "",
    var name : String = "",
    var communityPic : String = "",
    var profilePicId : String = "",
    var about : String = "",
    var campus : String = "",
    var members : List<String> = listOf(),
    var editors : List<String> = listOf(),
    var admin : String = "",
    var tags : List<String> = listOf()

)
