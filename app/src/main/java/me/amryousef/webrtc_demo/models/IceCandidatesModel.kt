package me.amryousef.webrtc_demo.models

data class IceCandidatesModel(
    val candidate: IceCandidateSubModel,
    val type: String,
    val name: String
)