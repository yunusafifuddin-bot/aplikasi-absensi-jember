package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

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
  version = 1,
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
        ).build()
        INSTANCE = instance
        instance
      }
    }
  }
}
