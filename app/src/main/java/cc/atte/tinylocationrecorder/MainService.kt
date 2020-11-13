package cc.atte.tinylocationrecorder

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.location.*

class MainService : Service() {
    companion object {
        const val CHANNEL_ID = "TLR_channel"
        const val CHANNEL_NAME = "TLR"
        const val CHANNEL_DESCRIPTION = "TLR status"
    }

    private lateinit var fusedClient: FusedLocationProviderClient

    override fun onCreate() {
        super.onCreate()
        fusedClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private var recordTiming = 10

    private var locationRequest: LocationRequest? = null
    private var locationCallback: LocationCallback? = null


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val app = application as MainApplication
        app.putServiceRunning(true)

        recordTiming = app.getRecordTiming()
        locationRequest = createLocationRequest()
        locationCallback = createLocationCallback()

        val notification = createNotificationRunning()
        startForeground(1, notification)
        startLocationUpdates()

        return START_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        stopLocationUpdates()
        val app = application as MainApplication
        app.putServiceRunning(false)
        super.onDestroy()
    }

    private fun startLocationUpdates() {
        if (PackageManager.PERMISSION_GRANTED
            != checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            val notification = createNotificationProhibited()
            NotificationManagerCompat.from(this).notify(2, notification)
        } else
            fusedClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    private fun stopLocationUpdates() {
        fusedClient.removeLocationUpdates(locationCallback)
    }

    private fun createNotificationRunning(): Notification {
        val openIntent = Intent(this, MainActivity::class.java).let {
            PendingIntent.getActivity(this, 0, it, 0)
        }
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_my_notification)
            .setContentTitle("Recording location")
            .setContentText("Tap to check log messages")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(openIntent)
            .setAutoCancel(true)
            .setWhen(System.currentTimeMillis())
        return notificationBuilder.build()
    }

    private fun createNotificationProhibited(): Notification {
        val openIntent = Intent().let {
            it.data = Uri.parse("package:${packageName}")
            it.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            PendingIntent.getActivity(this, 0, it, 0)
        }
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_my_notification)
            .setContentTitle("Location access prohibited")
            .setContentText("Tap to grant location permission")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(openIntent)
            .setAutoCancel(true)
        return notificationBuilder.build()
    }

    private fun createLocationRequest() = LocationRequest.create().apply {
        interval = recordTiming * 1000L
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    private fun createLocationCallback() = object: LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult ?: return
            for (location in locationResult.locations) {
                val newId = RecordModel.append(
                    location.time / 1000.0,
                    location.longitude, location.latitude, location.altitude
                )
                Log.d("TLR", "newId:$newId")
            }
        }
    }
}