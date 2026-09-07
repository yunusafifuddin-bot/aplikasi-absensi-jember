package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Room is only a temporary cache. Google Sheets / Apps Script is the source of truth.
 * Because cached data can always be rebuilt from the server, schema changes may safely
 * recreate the local database instead of risking an application crash on startup.
 */
@Database(
  entities = [
    EmployeeEntity::class,
    ShiftEntity::class,
    AttendanceEntity::class,
    KasbonEntity::class,
    CutiEntity::class,
    LemburEntity::class,
    PayrollEntity::class,
    AnnouncementEntity::class
  ],
  version = 2,
  exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun employeeDao(): EmployeeDao
  abstract fun shiftDao(): ShiftDao
  abstract fun attendanceDao(): AttendanceDao
  abstract fun kasbonDao(): KasbonDao
  abstract fun cutiDao(): CutiDao
  abstract fun lemburDao(): LemburDao
  abstract fun payrollDao(): PayrollDao
  abstract fun announcementDao(): AnnouncementDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "sukses_jaya_hris.db"
        )
          // Local data is only a cache, so rebuilding it is safe and prevents
          // Room schema-mismatch crashes after an application update.
          .fallbackToDestructiveMigration()
          .build()
        INSTANCE = instance
        instance
      }
    }
  }
}
