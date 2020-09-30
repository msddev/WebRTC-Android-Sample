package me.amryousef.webrtc_demo.models


data class AnswerModel(
    val name: String? = null,
    val answer: SdpModel,
    var type: String
)