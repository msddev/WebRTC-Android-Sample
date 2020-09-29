package me.amryousef.webrtc_demo.models

import org.webrtc.IceCandidate

data class IceCandidatesModel(
    val candidate: IceCandidate,
    val type: String,
    val name:String
)