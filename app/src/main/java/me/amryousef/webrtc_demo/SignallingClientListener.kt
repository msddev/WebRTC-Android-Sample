package me.amryousef.webrtc_demo

import me.amryousef.webrtc_demo.models.IceCandidatesModel
import me.amryousef.webrtc_demo.models.SessionDescriptionModel
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription

interface SignallingClientListener {
    fun onConnectionEstablished()
    fun onLoggedIn(isLogeIn: Boolean)
    fun onOfferReceived(offer: SessionDescriptionModel)
    fun onAnswerReceived(answer: SessionDescriptionModel)
    fun onIceCandidateReceived(iceCandidate: IceCandidatesModel)
}