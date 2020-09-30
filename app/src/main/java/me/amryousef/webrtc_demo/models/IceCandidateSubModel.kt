package me.amryousef.webrtc_demo.models

data class IceCandidateSubModel(
    val sdpMid: String,
    val sdpMLineIndex: Int,
    val candidate: String
)