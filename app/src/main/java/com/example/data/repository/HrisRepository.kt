package com.example.data.repository

import android.util.Log
import com.example.data.local.*
import com.example.data.remote.GasApiClient
import com.example.data.remote.GasSyncResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class HrisRepository(private val db: AppDatabase) {

  private val coroutineScope = CoroutineScope(Dispatchers.IO)
  private val tag = "HrisRepository"

  val allEmployees: Flow<List<EmployeeEntity>> = db.employeeDao().getAllEmployees()
  val allShifts: Flow<List<ShiftEntity>> = db.shiftDao().getAllShifts()
  val allAnnouncements: Flow<List<AnnouncementEntity>> = db.announcementDao().getAllAnnouncements()
  val allAttendances: Flow<List<AttendanceEntity>> = db.attendanceDao().getAllAttendances()
  val allKasbons: Flow<List<KasbonEntity>> = db.kasbonDao().getAllKasbons()
  val allCutis: Flow<List<CutiEntity>> = db.cutiDao().getAllCutis()
  val allLemburs: Flow<List<LemburEntity>> = db.lemburDao().getAllLemburs()
  val allPayrolls: Flow<List<PayrollEntity>> = db.payrollDao().getAllPayrolls()

  fun observeEmployee(nik: String): Flow<EmployeeEntity?> = db.employeeDao().observeEmployeeByNik(nik)
  fun observeTodayAttendance(nik: String, date: String): Flow<AttendanceEntity?> =
    db.attendanceDao().observeAttendance(nik, date)

  fun getEmployeeAttendances(nik: String): Flow<List<AttendanceEntity>> =
    db.attendanceDao().getAttendancesByNik(nik)

  fun getEmployeeKasbons(nik: String): Flow<List<KasbonEntity>> =
    db.kasbonDao().getKasbonsByNik(nik)

  fun getEmployeeCutis(nik: String): Flow<List<CutiEntity>> =
    db.cutiDao().getCutisByNik(nik)

  fun getEmployeeLemburs(nik: String): Flow<List<LemburEntity>> =
    db.lemburDao().getLembursByNik(nik)

  fun getEmployeePayrolls(nik: String): Flow<List<PayrollEntity>> =
    db.payrollDao().getPayrollsByNik(nik)

  fun getPayrollsByPeriode(periode: String): Flow<List<PayrollEntity>> =
    db.payrollDao().getPayrollsByPeriode(periode)

  /**
   * Sync all tables from Google Apps Script / Google Sheets database into Room.
   */
  suspend fun syncWithRemote(): Result<GasSyncResult> = withContext(Dispatchers.IO) {
    try {
      val remoteData = GasApiClient.fetchSyncData()
      if (remoteData != null) {
        if (remoteData.employees.isNotEmpty()) {
          db.employeeDao().insertAll(remoteData.employees)
        }
        if (remoteData.shifts.isNotEmpty()) {
          db.shiftDao().insertAll(remoteData.shifts)
        }
        if (remoteData.attendances.isNotEmpty()) {
          db.attendanceDao().insertAll(remoteData.attendances)
        }
        if (remoteData.kasbons.isNotEmpty()) {
          db.kasbonDao().insertAll(remoteData.kasbons)
        }
        if (remoteData.cutis.isNotEmpty()) {
          db.cutiDao().insertAll(remoteData.cutis)
        }
        if (remoteData.lemburs.isNotEmpty()) {
          db.lemburDao().insertAll(remoteData.lemburs)
        }
        if (remoteData.payrolls.isNotEmpty()) {
          db.payrollDao().insertAll(remoteData.payrolls)
        }
        if (remoteData.announcements.isNotEmpty()) {
          db.announcementDao().insertAll(remoteData.announcements)
        }
        Log.d(tag, "Successfully synced database from Google Apps Script")
        Result.success(remoteData)
      } else {
        Result.failure(Exception("Tidak dapat terhubung ke server Google Sheets."))
      }
    } catch (e: Exception) {
      Log.e(tag, "Failed to sync with remote database", e)
      Result.failure(e)
    }
  }

  suspend fun login(nik: String, pass: String): EmployeeEntity? {
    // 1. Try online authentication with Google Apps Script
    val remoteEmp = try {
      GasApiClient.login(nik, pass)
    } catch (e: Exception) {
      null
    }
    if (remoteEmp != null) {
      db.employeeDao().insertOrUpdate(remoteEmp)
      return remoteEmp
    }

    // 2. Fallback to local Room database (offline support)
    val emp = db.employeeDao().getEmployeeByNik(nik) ?: return null
    return if (emp.password == pass) emp else null
  }

  suspend fun clockIn(
    nik: String,
    date: String,
    time: String,
    distanceMeters: Int,
    photoUrl: String? = null,
    lat: Double = -8.1724,
    lng: Double = 113.6995
  ): Result<AttendanceEntity> {
    val shift = db.employeeDao().getEmployeeByNik(nik)?.let {
      db.shiftDao().getShiftById(it.shiftId)
    }
    val shiftStart = shift?.jamMasuk ?: "08:00"
    val tolerance = shift?.toleransiMenit ?: 15

    // Check if late
    val isLate = try {
      val (sh, sm) = shiftStart.split(":").map { it.toInt() }
      val (th, tm) = time.split(":").map { it.toInt() }
      val shiftMinutes = sh * 60 + sm + tolerance
      val actualMinutes = th * 60 + tm
      actualMinutes > shiftMinutes
    } catch (_: Exception) {
      false
    }

    val existing = db.attendanceDao().getAttendance(nik, date)
    val updated = AttendanceEntity(
      id = existing?.id ?: 0,
      nik = nik,
      tanggal = date,
      jamMasuk = time,
      jamPulang = existing?.jamPulang,
      status = if (isLate) "Terlambat" else "Tepat Waktu",
      jarakMeter = distanceMeters,
      fotoMasukUrl = photoUrl ?: existing?.fotoMasukUrl,
      fotoPulangUrl = existing?.fotoPulangUrl,
      lat = lat,
      lng = lng,
      durasiJam = existing?.durasiJam ?: 8.0
    )
    db.attendanceDao().insertOrUpdate(updated)

    // Push asynchronously to Google Apps Script
    coroutineScope.launch {
      try {
        val remoteResult = GasApiClient.clockIn(
          nik = nik,
          date = date,
          time = time,
          distanceMeters = distanceMeters,
          photoUrl = photoUrl,
          lat = lat,
          lng = lng
        )
        if (remoteResult != null) {
          db.attendanceDao().insertOrUpdate(remoteResult)
        }
      } catch (e: Exception) {
        Log.e(tag, "Remote clockIn sync error", e)
      }
    }

    return Result.success(updated)
  }

  suspend fun clockOut(
    nik: String,
    date: String,
    time: String,
    distanceMeters: Int,
    photoUrl: String? = null,
    lat: Double = -8.1724,
    lng: Double = 113.6995
  ): Result<AttendanceEntity> {
    val existing = db.attendanceDao().getAttendance(nik, date)
      ?: return Result.failure(Exception("Belum melakukan absen masuk hari ini."))

    // Calculate duration
    val durationHours = try {
      val (ih, im) = (existing.jamMasuk ?: "08:00").split(":").map { it.toInt() }
      val (oh, om) = time.split(":").map { it.toInt() }
      val minutes = (oh * 60 + om) - (ih * 60 + im)
      maxOf(1.0, String.format(Locale.US, "%.1f", minutes / 60.0).toDouble())
    } catch (_: Exception) {
      8.0
    }

    val updated = existing.copy(
      jamPulang = time,
      fotoPulangUrl = photoUrl ?: existing.fotoPulangUrl,
      durasiJam = durationHours,
      jarakMeter = distanceMeters,
      lat = lat,
      lng = lng
    )
    db.attendanceDao().insertOrUpdate(updated)

    // Push asynchronously to Google Apps Script
    coroutineScope.launch {
      try {
        val remoteResult = GasApiClient.clockOut(
          nik = nik,
          date = date,
          time = time,
          distanceMeters = distanceMeters,
          photoUrl = photoUrl,
          lat = lat,
          lng = lng
        )
        if (remoteResult != null) {
          db.attendanceDao().insertOrUpdate(remoteResult)
        }
      } catch (e: Exception) {
        Log.e(tag, "Remote clockOut sync error", e)
      }
    }

    return Result.success(updated)
  }

  suspend fun submitKasbon(nik: String, nama: String, amount: Long, reason: String) {
    val id = "KB-" + System.currentTimeMillis().toString().takeLast(6)
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val entity = KasbonEntity(
      idKasbon = id,
      nik = nik,
      nama = nama,
      tanggalPengajuan = sdf.format(Date()),
      jumlah = amount,
      keterangan = reason,
      statusPersetujuan = "Pending"
    )
    db.kasbonDao().insert(entity)

    coroutineScope.launch {
      try {
        val remote = GasApiClient.submitKasbon(nik, nama, amount, reason)
        if (remote != null) {
          db.kasbonDao().insert(remote)
        }
      } catch (e: Exception) {
        Log.e(tag, "Remote submitKasbon sync error", e)
      }
    }
  }

  suspend fun submitCuti(
    nik: String,
    nama: String,
    jenis: String,
    startDate: String,
    endDate: String,
    reason: String
  ) {
    val id = "CT-" + System.currentTimeMillis().toString().takeLast(6)
    val entity = CutiEntity(
      idCuti = id,
      nik = nik,
      nama = nama,
      jenisCuti = jenis,
      tglMulai = startDate,
      tglSelesai = endDate,
      alasan = reason,
      status = "Pending"
    )
    db.cutiDao().insert(entity)

    coroutineScope.launch {
      try {
        val remote = GasApiClient.submitCuti(nik, nama, jenis, startDate, endDate, reason)
        if (remote != null) {
          db.cutiDao().insert(remote)
        }
      } catch (e: Exception) {
        Log.e(tag, "Remote submitCuti sync error", e)
      }
    }
  }

  suspend fun submitLembur(
    nik: String,
    nama: String,
    date: String,
    startTime: String,
    endTime: String,
    desc: String
  ) {
    val id = "OT-" + System.currentTimeMillis().toString().takeLast(6)
    val durasi = try {
      val (sh, sm) = startTime.split(":").map { it.toInt() }
      val (eh, em) = endTime.split(":").map { it.toInt() }
      val diff = (eh * 60 + em) - (sh * 60 + sm)
      maxOf(0.5, String.format(Locale.US, "%.1f", diff / 60.0).toDouble())
    } catch (_: Exception) {
      2.0
    }

    val entity = LemburEntity(
      idLembur = id,
      nik = nik,
      nama = nama,
      tanggal = date,
      jamMulai = startTime,
      jamSelesai = endTime,
      durasiJam = durasi,
      deskripsi = desc,
      statusPersetujuan = "Pending"
    )
    db.lemburDao().insert(entity)

    coroutineScope.launch {
      try {
        val remote = GasApiClient.submitLembur(nik, nama, date, startTime, endTime, desc, durasi)
        if (remote != null) {
          db.lemburDao().insert(remote)
        }
      } catch (e: Exception) {
        Log.e(tag, "Remote submitLembur sync error", e)
      }
    }
  }

  suspend fun updateKasbonStatus(id: String, status: String) {
    db.kasbonDao().updateStatus(id, status)
    coroutineScope.launch {
      try {
        GasApiClient.updateKasbonStatus(id, status)
      } catch (e: Exception) {
        Log.e(tag, "Remote updateKasbonStatus error", e)
      }
    }
  }

  suspend fun updateCutiStatus(id: String, status: String) {
    db.cutiDao().updateStatus(id, status)
    coroutineScope.launch {
      try {
        GasApiClient.updateCutiStatus(id, status)
      } catch (e: Exception) {
        Log.e(tag, "Remote updateCutiStatus error", e)
      }
    }
  }

  suspend fun updateLemburStatus(id: String, status: String) {
    db.lemburDao().updateStatus(id, status)
    coroutineScope.launch {
      try {
        GasApiClient.updateLemburStatus(id, status)
      } catch (e: Exception) {
        Log.e(tag, "Remote updateLemburStatus error", e)
      }
    }
  }

  suspend fun saveEmployee(employee: EmployeeEntity) {
    db.employeeDao().insertOrUpdate(employee)
    coroutineScope.launch {
      try {
        GasApiClient.saveEmployee(employee)
      } catch (e: Exception) {
        Log.e(tag, "Remote saveEmployee error", e)
      }
    }
  }

  suspend fun deleteEmployee(nik: String) {
    db.employeeDao().deleteByNik(nik)
    coroutineScope.launch {
      try {
        GasApiClient.deleteEmployee(nik)
      } catch (e: Exception) {
        Log.e(tag, "Remote deleteEmployee error", e)
      }
    }
  }

  suspend fun saveShift(shift: ShiftEntity) {
    db.shiftDao().insertOrUpdate(shift)
    coroutineScope.launch {
      try {
        GasApiClient.saveShift(shift)
      } catch (e: Exception) {
        Log.e(tag, "Remote saveShift error", e)
      }
    }
  }

  suspend fun deleteShift(shiftId: String) {
    db.shiftDao().deleteById(shiftId)
    coroutineScope.launch {
      try {
        GasApiClient.deleteShift(shiftId)
      } catch (e: Exception) {
        Log.e(tag, "Remote deleteShift error", e)
      }
    }
  }

  suspend fun markPayrollPaid(id: String) {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    db.payrollDao().updateStatus(id, "Paid", sdf.format(Date()))
    coroutineScope.launch {
      try {
        GasApiClient.markPayrollPaid(id)
      } catch (e: Exception) {
        Log.e(tag, "Remote markPayrollPaid error", e)
      }
    }
  }

  suspend fun generatePayroll(periode: String, targetNik: String = "") {
    val employees = if (targetNik.isNotBlank()) {
      listOfNotNull(db.employeeDao().getEmployeeByNik(targetNik))
    } else {
      db.employeeDao().getAllEmployees().firstOrNull() ?: emptyList()
    }

    val allKasbons = db.kasbonDao().getAllKasbons().firstOrNull() ?: emptyList()
    val allLemburs = db.lemburDao().getAllLemburs().firstOrNull() ?: emptyList()

    for (emp in employees) {
      val payrollId = "PR-$periode-${emp.nik}"
      val kasbonDeduction = allKasbons
        .filter { it.nik == emp.nik && it.statusPersetujuan == "Approved" }
        .sumOf { it.jumlah }
        .coerceAtMost(emp.limitKasbon)

      val otHours = allLemburs
        .filter { it.nik == emp.nik && it.statusPersetujuan == "Approved" && it.tanggal.startsWith(periode) }
        .sumOf { it.durasiJam }
      val otPay = (otHours * 50000).toLong()

      val potonganLain = 120000L
      val netSalary = (emp.gajiPokok + emp.tunjangan + otPay - kasbonDeduction - potonganLain).coerceAtLeast(0)

      val payroll = PayrollEntity(
        idPayroll = payrollId,
        nik = emp.nik,
        nama = emp.nama,
        jabatan = emp.jabatan,
        periode = periode,
        gajiPokok = emp.gajiPokok,
        tunjangan = emp.tunjangan,
        uangLembur = otPay,
        potonganKasbon = kasbonDeduction,
        potonganLain = potonganLain,
        gajiBersih = netSalary,
        status = "Draft"
      )
      db.payrollDao().insert(payroll)
    }

    coroutineScope.launch {
      try {
        GasApiClient.generatePayroll(periode, targetNik)
      } catch (e: Exception) {
        Log.e(tag, "Remote generatePayroll error", e)
      }
    }
  }

  suspend fun addAnnouncement(judul: String, isi: String, kategori: String) {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val localEntity = AnnouncementEntity(
      judul = judul,
      isi = isi,
      kategori = kategori,
      tanggal = sdf.format(Date())
    )
    db.announcementDao().insert(localEntity)

    coroutineScope.launch {
      try {
        val remote = GasApiClient.addAnnouncement(judul, isi, kategori)
        if (remote != null) {
          db.announcementDao().insert(remote)
        }
      } catch (e: Exception) {
        Log.e(tag, "Remote addAnnouncement error", e)
      }
    }
  }

  suspend fun updateEmployeePhoto(nik: String, photoUrl: String) {
    val emp = db.employeeDao().getEmployeeByNik(nik) ?: return
    val updated = emp.copy(fotoUrl = photoUrl)
    db.employeeDao().insertOrUpdate(updated)
    coroutineScope.launch {
      try {
        GasApiClient.saveEmployee(updated)
      } catch (e: Exception) {
        Log.e(tag, "Remote updateEmployeePhoto error", e)
      }
    }
  }
}

