package me.amryousef.webrtc_demo.models


data class OfferModel(
    val name: String,
    val offer: SdpModel,
    var type: String
)