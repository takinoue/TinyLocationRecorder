package cc.atte.tinylocationrecorder

import android.app.Application
import androidx.preference.PreferenceManager
import io.realm.Realm

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
    }

    private val settingRecordTiming = "record_timing"
    private val settingFollowRecord = "follow_timing"
    private val settingServiceRunning = "service_running"

    private fun getPreference() =
        PreferenceManager.getDefaultSharedPreferences(this)

    fun getRecordTiming(): Int =
        getPreference().getInt(settingRecordTiming, 15)

    fun putRecordTiming(value: Int): Unit =
        getPreference().edit().putInt(settingRecordTiming, value).apply()

    fun getFollowRecord(): Boolean =
        getPreference().getBoolean(settingFollowRecord, false)

    fun putFollowRecord(value: Boolean): Unit =
        getPreference().edit().putBoolean(settingFollowRecord, value).apply()

    fun getServiceRunning(): Boolean =
        getPreference().getBoolean(settingServiceRunning, false)

    fun putServiceRunning(value: Boolean): Unit =
        getPreference().edit().putBoolean(settingServiceRunning, value).apply()
}