package com.example.data.repository

import android.util.Log
import com.example.data.local.*
import com.example.data.remote.GasApiClient
import com.example.data.remote.GasSyncResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class HrisRepository(private val db: AppDatabase) {
  private val coroutineScope = kotlinx.coroutines.CoroutineScope(Dispatchers.IO)
  private val tag = "HrisRepository"

  val allEmployees: Flow<List<EmployeeEntity>> = db.employeeDao().getAllEmployees()
  val allShifts: Flow<List<ShiftEntity>> = db.shiftDao().getAllShifts()
  val allAnnouncements: Flow<List<AnnouncementEntity>> = db.announcementDao().getAllAnnouncements()
  val allAttendances: Flow<List<AttendanceEntity>> = db.attendanceDao().getAllAttendances()
  val allKasbons: Flow<List<KasbonEntity>> = db.kasbonDao().getAllKasbons()
  val allCutis: Flow<List<CutiEntity>> = db.cutiDao().getAllCutis()
  val allLemburs: Flow<List<LemburEntity>> = db.lemburDao().getAllLemburs()
  val allPayrolls: Flow<List<PayrollEntity>> = db.payrollDao().getAllPayrolls()

  fun observeEmployee(nik: String) = db.employeeDao().observeEmployeeByNik(nik)
  fun observeTodayAttendance(nik: String, date: String) = db.attendanceDao().observeAttendance(nik, date)
  fun getEmployeeAttendances(nik: String) = db.attendanceDao().getAttendancesByNik(nik)
  fun getEmployeeKasbons(nik: String) = db.kasbonDao().getKasbonsByNik(nik)
  fun getEmployeeCutis(nik: String) = db.cutiDao().getCutisByNik(nik)
  fun getEmployeeLemburs(nik: String) = db.lemburDao().getLembursByNik(nik)
  fun getEmployeePayrolls(nik: String) = db.payrollDao().getPayrollsByNik(nik)
  fun getPayrollsByPeriode(periode: String) = db.payrollDao().getPayrollsByPeriode(periode)

  /**
   * Google Sheets is the authoritative source. Room is only a transient rendering cache.
   * Every successful sync first clears the cache so stale/duplicated rows cannot remain.
   */
  suspend fun syncWithRemote(): Result<GasSyncResult> = withContext(Dispatchers.IO) {
    try {
      val remoteData = GasApiClient.fetchSyncData()
        ?: return@withContext Result.failure(Exception("Tidak dapat terhubung ke server Google Sheets."))

      db.employeeDao().clearAll()
      db.shiftDao().clearAll()
      db.attendanceDao().clearAll()
      db.kasbonDao().clearAll()
      db.cutiDao().clearAll()
      db.lemburDao().clearAll()
      db.payrollDao().clearAll()
      db.announcementDao().clearAll()

      if (remoteData.employees.isNotEmpty()) db.employeeDao().insertAll(remoteData.employees)
      if (remoteData.shifts.isNotEmpty()) db.shiftDao().insertAll(remoteData.shifts)
      if (remoteData.attendances.isNotEmpty()) db.attendanceDao().insertAll(remoteData.attendances)
      if (remoteData.kasbons.isNotEmpty()) db.kasbonDao().insertAll(remoteData.kasbons)
      if (remoteData.cutis.isNotEmpty()) db.cutiDao().insertAll(remoteData.cutis)
      if (remoteData.lemburs.isNotEmpty()) db.lemburDao().insertAll(remoteData.lemburs)
      if (remoteData.payrolls.isNotEmpty()) db.payrollDao().insertAll(remoteData.payrolls)
      if (remoteData.announcements.isNotEmpty()) db.announcementDao().insertAll(remoteData.announcements)

      Log.d(tag, "Remote data is authoritative; local mirror refreshed")
      Result.success(remoteData)
    } catch (e: Exception) {
      Log.e(tag, "Failed to sync with remote database", e)
      Result.failure(e)
    }
  }

  suspend fun login(nik: String, pass: String): EmployeeEntity? {
    // Online-only authentication. Local Room data must never be used as an independent source of truth.
    return try {
      GasApiClient.login(nik.trim(), pass)
    } catch (e: Exception) {
      Log.e(tag, "Online login error", e)
      null
    }
  }

  suspend fun clockIn(nik: String, date: String, time: String, distanceMeters: Int, photoUrl: String? = null, lat: Double, lng: Double): Result<AttendanceEntity> = withContext(Dispatchers.IO) {
    val existing = try { GasApiClient.fetchTodayAttendance(nik, date) } catch (_: Exception) { null }
    if (existing?.jamMasuk != null) return@withContext Result.failure(Exception("Anda sudah absen masuk hari ini pada ${existing.jamMasuk}. Absen masuk hanya boleh 1 kali per hari."))

    val remote = try {
      GasApiClient.clockIn(nik, date, time, distanceMeters, photoUrl, lat, lng)
    } catch (e: Exception) {
      Log.e(tag, "Remote clockIn error", e)
      null
    }
    remote?.let { db.attendanceDao().insertOrUpdate(it); return@withContext Result.success(it) }
    Result.failure(Exception("Server menolak atau tidak merespons absensi masuk. Data tidak disimpan lokal."))
  }

  suspend fun clockOut(nik: String, date: String, time: String, distanceMeters: Int, photoUrl: String? = null, lat: Double, lng: Double): Result<AttendanceEntity> = withContext(Dispatchers.IO) {
    val existing = try { GasApiClient.fetchTodayAttendance(nik, date) } catch (_: Exception) { null }
      ?: return@withContext Result.failure(Exception("Belum melakukan absen masuk hari ini."))
    if (existing.jamMasuk == null) return@withContext Result.failure(Exception("Belum melakukan absen masuk hari ini."))
    if (existing.jamPulang != null) return@withContext Result.failure(Exception("Anda sudah absen pulang hari ini pada ${existing.jamPulang}. Absen pulang hanya boleh 1 kali per hari."))

    val remote = try {
      GasApiClient.clockOut(nik, date, time, distanceMeters, photoUrl, lat, lng)
    } catch (e: Exception) {
      Log.e(tag, "Remote clockOut error", e)
      null
    }
    remote?.let { db.attendanceDao().insertOrUpdate(it); return@withContext Result.success(it) }
    Result.failure(Exception("Server menolak atau tidak merespons absensi pulang. Data tidak disimpan lokal."))
  }

  // Remaining non-attendance functions intentionally retain their existing remote-write behavior.
  suspend fun submitKasbon(nik: String, nama: String, amount: Long, reason: String) {
    val id = "KB-" + System.currentTimeMillis().toString().takeLast(6)
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val entity = KasbonEntity(id, nik, nama, sdf.format(Date()), amount, reason, "Pending")
    coroutineScope.launch { try { GasApiClient.submitKasbon(nik, nama, amount, reason) } catch (e: Exception) { Log.e(tag, "submitKasbon", e) } }
  }

  suspend fun submitCuti(nik: String, nama: String, jenis: String, startDate: String, endDate: String, reason: String) {
    coroutineScope.launch { try { GasApiClient.submitCuti(nik, nama, jenis, startDate, endDate, reason) } catch (e: Exception) { Log.e(tag, "submitCuti", e) } }
  }

  suspend fun submitLembur(nik: String, nama: String, date: String, startTime: String, endTime: String, desc: String) {
    coroutineScope.launch { try { GasApiClient.submitLembur(nik, nama, date, startTime, endTime, desc, 0.0) } catch (e: Exception) { Log.e(tag, "submitLembur", e) } }
  }

  suspend fun updateKasbonStatus(id: String, status: String) { coroutineScope.launch { try { GasApiClient.updateKasbonStatus(id, status) } catch (e: Exception) { Log.e(tag, "updateKasbonStatus", e) } } }
  suspend fun updateCutiStatus(id: String, status: String) { coroutineScope.launch { try { GasApiClient.updateCutiStatus(id, status) } catch (e: Exception) { Log.e(tag, "updateCutiStatus", e) } } }
  suspend fun updateLemburStatus(id: String, status: String) { coroutineScope.launch { try { GasApiClient.updateLemburStatus(id, status) } catch (e: Exception) { Log.e(tag, "updateLemburStatus", e) } } }

  suspend fun saveEmployee(employee: EmployeeEntity) { try { GasApiClient.saveEmployee(employee) } catch (e: Exception) { Log.e(tag, "saveEmployee", e) } }
  suspend fun deleteEmployee(nik: String) { try { GasApiClient.deleteEmployee(nik) } catch (e: Exception) { Log.e(tag, "deleteEmployee", e) } }
  suspend fun saveShift(shift: ShiftEntity) { try { GasApiClient.saveShift(shift) } catch (e: Exception) { Log.e(tag, "saveShift", e) } }
  suspend fun deleteShift(shiftId: String) { try { GasApiClient.deleteShift(shiftId) } catch (e: Exception) { Log.e(tag, "deleteShift", e) } }
  suspend fun markPayrollPaid(id: String) { try { GasApiClient.markPayrollPaid(id) } catch (e: Exception) { Log.e(tag, "markPayrollPaid", e) } }
  suspend fun generatePayroll(periode: String, targetNik: String = "") { try { GasApiClient.generatePayroll(periode, targetNik) } catch (e: Exception) { Log.e(tag, "generatePayroll", e) } }
  suspend fun addAnnouncement(judul: String, isi: String, kategori: String) { try { GasApiClient.addAnnouncement(judul, isi, kategori) } catch (e: Exception) { Log.e(tag, "addAnnouncement", e) } }
  suspend fun updateEmployeePhoto(nik: String, photoUrl: String) { val emp = try { GasApiClient.login(nik, "") } catch (_: Exception) { null }; if (emp != null) try { GasApiClient.saveEmployee(emp.copy(fotoUrl = photoUrl)) } catch (e: Exception) { Log.e(tag, "updateEmployeePhoto", e) } }
}
