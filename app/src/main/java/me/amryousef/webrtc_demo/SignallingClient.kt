package me.amryousef.webrtc_demo

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.features.websocket.*
import io.ktor.http.cio.websocket.*
import io.ktor.util.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
class SignallingClient(
    private val listener: SignallingClientListener
) : CoroutineScope {

    companion object {
        private const val HOST_ADDRESS = "192.168.18.243"
    }

    private val job = Job()

    private val gson = Gson()

    override val coroutineContext = Dispatchers.IO + job

    private val client = HttpClient(CIO) {
        install(WebSockets)
        install(JsonFeature) {
            serializer = GsonSerializer()
        }
    }

    private val sendChannel = ConflatedBroadcastChannel<String>()

    init {
        connect()
    }

    private fun connect() = launch {
        client.wss(host = HOST_ADDRESS, port = 8080, path = "/connect") {
            listener.onConnectionEstablished()
            val sendData = sendChannel.openSubscription()
            try {
                while (true) {

                    sendData.poll()?.let {
                        Log.v(this@SignallingClient.javaClass.simpleName, "Sending: $it")
                        outgoing.send(Frame.Text(it))
                    }
                    incoming.poll()?.let { frame ->
                        if (frame is Frame.Text) {
                            val data = frame.readText()
                            Log.v(this@SignallingClient.javaClass.simpleName, "Received: $data")
                            val jsonObject = gson.fromJson(data, JsonObject::class.java)
                            withContext(Dispatchers.Main) {
                                if (jsonObject.has("serverUrl")) {
                                    listener.onIceCandidateReceived(
                                        gson.fromJson(
                                            jsonObject,
                                            IceCandidate::class.java
                                        )
                                    )
                                } else if (jsonObject.has("type") && jsonObject.get("type").asString == "OFFER") {
                                    listener.onOfferReceived(
                                        gson.fromJson(
                                            jsonObject,
                                            SessionDescription::class.java
                                        )
                                    )
                                } else if (jsonObject.has("type") && jsonObject.get("type").asString == "ANSWER") {
                                    listener.onAnswerReceived(
                                        gson.fromJson(
                                            jsonObject,
                                            SessionDescription::class.java
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            } catch (exception: Throwable) {
                Log.e("asd", "asd", exception)
            }
        }
    }

    fun send(dataObject: Any?) = runBlocking {
        sendChannel.send(gson.toJson(dataObject))
    }

    fun destroy() {
        client.close()
        job.complete()
    }
}