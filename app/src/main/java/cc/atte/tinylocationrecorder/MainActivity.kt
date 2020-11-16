package cc.atte.tinylocationrecorder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cc.atte.tinylocationrecorder.databinding.ActivityMainBinding
import io.realm.Realm
import io.realm.Sort

class MainActivity : AppCompatActivity() {
    private lateinit var realm: Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        realm = Realm.getDefaultInstance()

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        createNotificationChannel()

        val app = application as MainApplication

        val recordData = realm.where(RecordModel::class.java)
            .sort("id", Sort.DESCENDING).findAll()
        val adapter = RecordAdapter(recordData)
        val layoutManager = object: LinearLayoutManager(this) {
            override fun onItemsAdded(
                recyclerView: RecyclerView,
                positionStart: Int, itemCount: Int
            ) {
                super.onItemsAdded(recyclerView, positionStart, itemCount)
                val followRecord = app.getFollowRecord()
                if (followRecord) recyclerView.scrollToPosition(0)
            }
        }
        binding.viewerRecord.adapter = adapter
        binding.viewerRecord.layoutManager = layoutManager

        binding.toggleRecord.isChecked = app.getServiceRunning()
        binding.toggleRecord.setOnCheckedChangeListener { _, isChecked ->
            val intent = Intent(this, MainService::class.java)
            if (isChecked) startForegroundService(intent) else stopService(intent)
        }

        binding.shareRecord.setOnClickListener {
            val recordLine = recordData.map {
                "0:%.6f,%.6f,%.3f,ts=%.3f\n"
                    .format(it.longitude, it.latitude, it.altitude, it.timestamp)
            }
            val recordText = recordLine.reversed().joinToString("")
            val sendIntent = Intent(Intent.ACTION_SEND)
            sendIntent.type = "text/plain"
            sendIntent.putExtra(Intent.EXTRA_TEXT, recordText)
            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        }

        binding.deleteRecord.setOnClickListener {
            realm.executeTransactionAsync { db -> db.delete(RecordModel::class.java) }
        }

        binding.settingRecord.setOnClickListener {
            SettingDialog().show(supportFragmentManager,null)
        }
   }

    override fun onDestroy() {
        realm.close()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            MainService.CHANNEL_ID,
            MainService.CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = MainService.CHANNEL_DESCRIPTION
            setSound(null, null)
            enableLights(false)
            enableVibration(false)
        }
        notificationManager.createNotificationChannel(channel)
    }
}