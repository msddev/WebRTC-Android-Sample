package me.amryousef.webrtc_demo

import me.amryousef.webrtc_demo.models.IceCandidatesModel
import me.amryousef.webrtc_demo.models.SessionDescriptionModel

interface SignallingClientListener {
    fun onConnectionEstablished()
    fun onLoggedIn(isLogeIn: Boolean)
    fun onOfferReceived(sessionDescription: SessionDescriptionModel)
    fun onAnswerReceived(sessionDescription: SessionDescriptionModel)
    fun onIceCandidateReceived(iceCandidate: IceCandidatesModel)
}