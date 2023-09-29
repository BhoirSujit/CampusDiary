package com.sujitbhoir.campusdiary.dataclasses

import com.google.firebase.Timestamp
import java.util.Date

data class PostData(
    var id : String = "",
    var title : String = "",
    var images : List<String> = listOf(),
    var communityName : String = "",
    var profilePicId : String = "",
    var authUName : String = "",
    var communityId : String = "",
    var context : String = "",
    var creationDate : String = "",
    var tags : List<String> = listOf(),
    var likes : List<String> = listOf()
)