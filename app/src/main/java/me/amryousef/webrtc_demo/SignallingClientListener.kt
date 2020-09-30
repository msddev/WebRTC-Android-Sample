package me.amryousef.webrtc_demo

import me.amryousef.webrtc_demo.models.AnswerModel
import me.amryousef.webrtc_demo.models.IceCandidatesModel
import me.amryousef.webrtc_demo.models.OfferModel

interface SignallingClientListener {
    fun onConnectionEstablished()
    fun onLoggedIn(isLogeIn: Boolean)
    fun onOfferReceived(offerData: OfferModel)
    fun onAnswerReceived(answerData: AnswerModel)
    fun onIceCandidateReceived(iceCandidate: IceCandidatesModel)
    fun onError(msg: String)
}