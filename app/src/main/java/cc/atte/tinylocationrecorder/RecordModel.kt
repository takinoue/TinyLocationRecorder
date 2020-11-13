package cc.atte.tinylocationrecorder

import io.realm.Realm
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class RecordModel(
    @PrimaryKey
    var id: Long = 0L,

    var timestamp: Double = Double.MIN_VALUE,
    var longitude: Double = Double.MIN_VALUE,
    var latitude: Double = Double.MIN_VALUE,
    var altitude: Double = Double.MIN_VALUE
): RealmObject() {
    companion object {
        private fun <T> use(block: (Realm) -> T): T =
            Realm.getDefaultInstance().use(block)

        private fun exec(block: (Realm) -> Unit): Unit =
            use { db -> db.executeTransactionAsync(block) }

        fun append(timestamp: Double,
                   longitude: Double, latitude: Double, altitude: Double) {
            val newRecord = RecordModel(0, timestamp, longitude, latitude, altitude)
            exec { db ->
                val query = db.where(RecordModel::class.java)
                val maxId = query.max("id")?.toLong() ?: 0L
                newRecord.id = maxId + 1
                db.copyToRealmOrUpdate(newRecord)
            }
        }
    }
}