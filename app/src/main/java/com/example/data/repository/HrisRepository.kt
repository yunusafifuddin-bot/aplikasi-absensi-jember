package com.example.data.repository

import android.util.Log
import com.example.data.local.*
import com.example.data.remote.GasApiClient
import com.example.data.remote.GasSyncResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class HrisRepository(private val db: AppDatabase) {
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

  /** Google Sheets is authoritative. Room is only a refreshed transient mirror. */
  suspend fun syncWithRemote(): Result<GasSyncResult> = withContext(Dispatchers.IO) {
    try {
      val remote = GasApiClient.fetchSyncData() ?: return@withContext Result.failure(Exception("Tidak dapat terhubung ke Google Sheets."))
      db.employeeDao().clearAll(); db.shiftDao().clearAll(); db.attendanceDao().clearAll(); db.kasbonDao().clearAll(); db.cutiDao().clearAll(); db.lemburDao().clearAll(); db.payrollDao().clearAll(); db.announcementDao().clearAll()
      if (remote.employees.isNotEmpty()) db.employeeDao().insertAll(remote.employees)
      if (remote.shifts.isNotEmpty()) db.shiftDao().insertAll(remote.shifts)
      if (remote.attendances.isNotEmpty()) db.attendanceDao().insertAll(remote.attendances)
      if (remote.kasbons.isNotEmpty()) db.kasbonDao().insertAll(remote.kasbons)
      if (remote.cutis.isNotEmpty()) db.cutiDao().insertAll(remote.cutis)
      if (remote.lemburs.isNotEmpty()) db.lemburDao().insertAll(remote.lemburs)
      if (remote.payrolls.isNotEmpty()) db.payrollDao().insertAll(remote.payrolls)
      if (remote.announcements.isNotEmpty()) db.announcementDao().insertAll(remote.announcements)
      Result.success(remote)
    } catch (e: Exception) { Log.e(tag, "Remote sync failed", e); Result.failure(e) }
  }

  suspend fun login(nik: String, pass: String): EmployeeEntity? = try { GasApiClient.login(nik.trim(), pass) } catch (e: Exception) { Log.e(tag, "Online login error", e); null }

  /** Refreshes from the server immediately before attendance, so stale local state cannot authorize a second punch. */
  suspend fun clockIn(nik: String, date: String, time: String, distanceMeters: Int, photoUrl: String? = null, lat: Double = -8.1724, lng: Double = 113.6995): Result<AttendanceEntity> = withContext(Dispatchers.IO) {
    val fresh = syncWithRemote()
    if (fresh.isFailure) return@withContext Result.failure(Exception("Server Google Sheets tidak tersedia. Presensi dibatalkan agar tidak terjadi data ganda."))
    val existing = db.attendanceDao().getAttendance(nik, date)
    if (existing?.jamMasuk != null) return@withContext Result.failure(Exception("Anda sudah absen masuk hari ini pada ${existing.jamMasuk}. Absen masuk hanya 1 kali per hari."))
    val remote = try { GasApiClient.clockIn(nik, date, time, distanceMeters, photoUrl, lat, lng) } catch (e: Exception) { Log.e(tag, "Remote clockIn", e); null }
    if (remote == null) Result.failure(Exception("Server menolak/tidak merespons absensi masuk. Data tidak disimpan lokal.")) else { db.attendanceDao().insertOrUpdate(remote); Result.success(remote) }
  }

  suspend fun clockOut(nik: String, date: String, time: String, distanceMeters: Int, photoUrl: String? = null, lat: Double = -8.1724, lng: Double = 113.6995): Result<AttendanceEntity> = withContext(Dispatchers.IO) {
    val fresh = syncWithRemote()
    if (fresh.isFailure) return@withContext Result.failure(Exception("Server Google Sheets tidak tersedia. Presensi dibatalkan."))
    val existing = db.attendanceDao().getAttendance(nik, date) ?: return@withContext Result.failure(Exception("Belum melakukan absen masuk hari ini."))
    if (existing.jamMasuk == null) return@withContext Result.failure(Exception("Belum melakukan absen masuk hari ini."))
    if (existing.jamPulang != null) return@withContext Result.failure(Exception("Anda sudah absen pulang hari ini pada ${existing.jamPulang}. Absen pulang hanya 1 kali per hari."))
    val remote = try { GasApiClient.clockOut(nik, date, time, distanceMeters, photoUrl, lat, lng) } catch (e: Exception) { Log.e(tag, "Remote clockOut", e); null }
    if (remote == null) Result.failure(Exception("Server menolak/tidak merespons absensi pulang. Data tidak disimpan lokal.")) else { db.attendanceDao().insertOrUpdate(remote); Result.success(remote) }
  }

  suspend fun submitKasbon(nik: String, nama: String, amount: Long, reason: String) { try { GasApiClient.submitKasbon(nik, nama, amount, reason) } catch (e: Exception) { Log.e(tag, "submitKasbon", e) } }
  suspend fun submitCuti(nik: String, nama: String, jenis: String, startDate: String, endDate: String, reason: String) { try { GasApiClient.submitCuti(nik, nama, jenis, startDate, endDate, reason) } catch (e: Exception) { Log.e(tag, "submitCuti", e) } }
  suspend fun submitLembur(nik: String, nama: String, date: String, startTime: String, endTime: String, desc: String) {
    try { val p1 = startTime.split(":").map { it.toInt() }; val p2 = endTime.split(":").map { it.toInt() }; val duration = maxOf(0.5, (p2[0] * 60 + p2[1] - p1[0] * 60 - p1[1]) / 60.0); GasApiClient.submitLembur(nik, nama, date, startTime, endTime, desc, duration) } catch (e: Exception) { Log.e(tag, "submitLembur", e) }
  }
  suspend fun updateKasbonStatus(id: String, status: String) { try { GasApiClient.updateKasbonStatus(id, status) } catch (e: Exception) { Log.e(tag, "updateKasbonStatus", e) } }
  suspend fun updateCutiStatus(id: String, status: String) { try { GasApiClient.updateCutiStatus(id, status) } catch (e: Exception) { Log.e(tag, "updateCutiStatus", e) } }
  suspend fun updateLemburStatus(id: String, status: String) { try { GasApiClient.updateLemburStatus(id, status) } catch (e: Exception) { Log.e(tag, "updateLemburStatus", e) } }
  suspend fun saveEmployee(employee: EmployeeEntity) { try { GasApiClient.saveEmployee(employee) } catch (e: Exception) { Log.e(tag, "saveEmployee", e) } }
  suspend fun deleteEmployee(nik: String) { try { GasApiClient.deleteEmployee(nik) } catch (e: Exception) { Log.e(tag, "deleteEmployee", e) } }
  suspend fun saveShift(shift: ShiftEntity) { try { GasApiClient.saveShift(shift) } catch (e: Exception) { Log.e(tag, "saveShift", e) } }
  suspend fun deleteShift(id: String) { try { GasApiClient.deleteShift(id) } catch (e: Exception) { Log.e(tag, "deleteShift", e) } }
  suspend fun markPayrollPaid(id: String) { try { GasApiClient.markPayrollPaid(id) } catch (e: Exception) { Log.e(tag, "markPayrollPaid", e) } }
  suspend fun generatePayroll(periode: String, targetNik: String = "") { try { GasApiClient.generatePayroll(periode, targetNik) } catch (e: Exception) { Log.e(tag, "generatePayroll", e) } }
  suspend fun addAnnouncement(judul: String, isi: String, kategori: String) { try { GasApiClient.addAnnouncement(judul, isi, kategori) } catch (e: Exception) { Log.e(tag, "addAnnouncement", e) } }
  suspend fun updateEmployeePhoto(nik: String, photoUrl: String) { Log.d(tag, "Remote photo update requested: $nik") }
}
