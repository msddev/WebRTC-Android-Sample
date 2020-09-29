package me.amryousef.webrtc_demo.models

import org.webrtc.SessionDescription


data class SessionDescriptionModel(
    val name: String,
    val offer: SdpModel,
    var type: String
)