package me.amryousef.webrtc_demo.models


data class SessionDescriptionModel(
    val name: String,
    val offer: SdpModel,
    var type: String
)