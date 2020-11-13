package cc.atte.tinylocationrecorder

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import cc.atte.tinylocationrecorder.databinding.DialogSettingBinding

class SettingDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)

        val act = activity ?: error("null activity")
        val app = act.application as? MainApplication ?: error("null application")

        val inflater = act.layoutInflater
        val binding = DialogSettingBinding.inflate(inflater)

        val recordTiming = app.getRecordTiming()
        binding.settingRecordTiming.setText(recordTiming.toString())
        val followRecord = app.getFollowRecord()
        binding.settingFollowRecord.isChecked = followRecord

        return AlertDialog.Builder(act)
            .setView(binding.root)
            .setTitle("Setting")
            .setPositiveButton("COMMIT") { _, _ ->
                val newRecordTiming = binding.settingRecordTiming.text.toString().toInt()
                if (recordTiming != newRecordTiming && 10 <= newRecordTiming)
                    app.putRecordTiming(newRecordTiming)
                val newFollowRecord = binding.settingFollowRecord.isChecked
                if (followRecord != newFollowRecord)
                    app.putFollowRecord(newFollowRecord)
            }
            .setNegativeButton("CANCEL") { _, _ -> }
            .create()
    }
}