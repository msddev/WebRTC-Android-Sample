package me.amryousef.webrtc_demo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import io.ktor.util.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import me.amryousef.webrtc_demo.models.IceCandidatesModel
import me.amryousef.webrtc_demo.models.LoginModel
import me.amryousef.webrtc_demo.models.SdpModel
import me.amryousef.webrtc_demo.models.SessionDescriptionModel
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.SessionDescription
import java.util.*

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
class MainActivity : AppCompatActivity() {

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 1
        private const val CAMERA_PERMISSION = Manifest.permission.CAMERA
    }

    private lateinit var rtcClient: RTCClient
    private lateinit var signallingClient: SignallingClient

    private val sdpObserver = object : AppSdpObserver() {
        override fun onCreateSuccess(sessionDescription: SessionDescription?) {
            super.onCreateSuccess(sessionDescription)

            sessionDescription?.let {
                val sdp =
                    SdpModel(sdp = it.description, type = it.type.name.toLowerCase(Locale.ROOT))
                val sessionDescriptionModel = SessionDescriptionModel(
                    type = it.type.name.toLowerCase(Locale.ROOT),
                    offer = sdp,
                    name = "Test2"
                )
                signallingClient.send(sessionDescriptionModel)
            } ?: run {
                Toast.makeText(this@MainActivity, "Error in sdpObserver", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkCameraPermission()
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                CAMERA_PERMISSION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestCameraPermission()
        } else {
            onCameraPermissionGranted()
        }
    }

    private fun onCameraPermissionGranted() {
        rtcClient = RTCClient(
            application,
            object : PeerConnectionObserver() {
                override fun onIceCandidate(iceCandidate: IceCandidate?) {
                    super.onIceCandidate(iceCandidate)
                    iceCandidate?.let {
                        val iceCandidatesModel =
                            IceCandidatesModel(type = "candidate", candidate = iceCandidate.sdp)
                        signallingClient.send(iceCandidatesModel)
                        rtcClient.addIceCandidate(iceCandidate)
                    } ?: run {
                        Toast.makeText(
                            this@MainActivity,
                            "Error in iceCandidate",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onAddStream(p0: MediaStream?) {
                    super.onAddStream(p0)
                    p0?.videoTracks?.get(0)?.addSink(remote_view)
                }
            }
        )
        rtcClient.initSurfaceView(remote_view)
        rtcClient.initSurfaceView(local_view)
        rtcClient.startLocalVideoCapture(local_view)
        signallingClient = SignallingClient(createSignallingClientListener())

        btnCall.setOnClickListener {
            rtcClient.call(sdpObserver)
        }

        btnLogin.setOnClickListener {
            val username: String? = etLoginUserName.text.toString()
            if (!username.isNullOrEmpty()) {
                val loginModel = LoginModel(type = "login", name = username)
                signallingClient.send(loginModel)
            } else {
                Toast.makeText(this, "Please enter username", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun createSignallingClientListener() = object : SignallingClientListener {
        override fun onConnectionEstablished() {
            groupLogin.isVisible = true
        }

        override fun onLoggedIn(isLogeIn: Boolean) {
            groupLogin.isVisible = !isLogeIn
            btnCall.isVisible = isLogeIn
        }

        override fun onOfferReceived(offer: SessionDescriptionModel) {
            val sessionDescription = SessionDescription(
                SessionDescription.Type.valueOf(offer.type),
                offer.name
            )

            rtcClient.onRemoteSessionReceived(sessionDescription)
            rtcClient.answer(sdpObserver)
            remote_view_loading.isGone = true
        }

        override fun onAnswerReceived(answer: SessionDescriptionModel) {
            val sessionDescription = SessionDescription(
                SessionDescription.Type.valueOf(answer.type),
                answer.name
            )

            rtcClient.onRemoteSessionReceived(sessionDescription)
            remote_view_loading.isGone = true
        }

        override fun onIceCandidateReceived(iceCandidateModel: IceCandidatesModel) {
            val iceCandidate = IceCandidate("audio", 0, iceCandidateModel.candidate)
            rtcClient.addIceCandidate(iceCandidate)
        }
    }

    private fun requestCameraPermission(dialogShown: Boolean = false) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                CAMERA_PERMISSION
            ) && !dialogShown
        ) {
            showPermissionRationaleDialog()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(CAMERA_PERMISSION),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun showPermissionRationaleDialog() {
        AlertDialog.Builder(this)
            .setTitle("Camera Permission Required")
            .setMessage("This app need the camera to function")
            .setPositiveButton("Grant") { dialog, _ ->
                dialog.dismiss()
                requestCameraPermission(true)
            }
            .setNegativeButton("Deny") { dialog, _ ->
                dialog.dismiss()
                onCameraPermissionDenied()
            }
            .show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            onCameraPermissionGranted()
        } else {
            onCameraPermissionDenied()
        }
    }

    private fun onCameraPermissionDenied() {
        Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        signallingClient.destroy()
        super.onDestroy()
    }
}
