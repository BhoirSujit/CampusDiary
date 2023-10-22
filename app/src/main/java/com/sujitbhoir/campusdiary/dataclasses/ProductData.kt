package com.sujitbhoir.campusdiary.dataclasses

data class ProductData(
    var id : String= "",
    var name : String = "",
    var details : String = "",
    var price : String = "",
    var condition :String = "",
    var images : List<String> = listOf(),
    var contactWhatsapp : String = "",
    var tags : List<String> = listOf<String>(),
    var campus  : String = "",
    var sellerId : String = "",
    var sellerName : String = "",
    var sellerPic : String = "",
    var uploadDate : String = ""
)
