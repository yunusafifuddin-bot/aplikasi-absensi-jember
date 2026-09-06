package com.example.data.repository

import com.example.data.local.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.*

class HrisRepository(private val db: AppDatabase) {

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

  suspend fun login(nik: String, pass: String): EmployeeEntity? {
    val emp = db.employeeDao().getEmployeeByNik(nik) ?: return null
    return if (emp.password == pass) emp else null
  }

  suspend fun clockIn(
    nik: String,
    date: String,
    time: String,
    distanceMeters: Int,
    photoUrl: String? = null
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
      durasiJam = existing?.durasiJam ?: 8.0
    )
    db.attendanceDao().insertOrUpdate(updated)
    return Result.success(updated)
  }

  suspend fun clockOut(
    nik: String,
    date: String,
    time: String,
    distanceMeters: Int,
    photoUrl: String? = null
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
      jarakMeter = distanceMeters
    )
    db.attendanceDao().insertOrUpdate(updated)
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
  }

  suspend fun updateKasbonStatus(id: String, status: String) {
    db.kasbonDao().updateStatus(id, status)
  }

  suspend fun updateCutiStatus(id: String, status: String) {
    db.cutiDao().updateStatus(id, status)
  }

  suspend fun updateLemburStatus(id: String, status: String) {
    db.lemburDao().updateStatus(id, status)
  }

  suspend fun saveEmployee(employee: EmployeeEntity) {
    db.employeeDao().insertOrUpdate(employee)
  }

  suspend fun deleteEmployee(nik: String) {
    db.employeeDao().deleteByNik(nik)
  }

  suspend fun saveShift(shift: ShiftEntity) {
    db.shiftDao().insertOrUpdate(shift)
  }

  suspend fun deleteShift(shiftId: String) {
    db.shiftDao().deleteById(shiftId)
  }

  suspend fun markPayrollPaid(id: String) {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    db.payrollDao().updateStatus(id, "Paid", sdf.format(Date()))
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
      // calculate approved kasbon outstanding
      val kasbonDeduction = allKasbons
        .filter { it.nik == emp.nik && it.statusPersetujuan == "Approved" }
        .sumOf { it.jumlah }
        .coerceAtMost(emp.limitKasbon)

      // calculate approved overtime pay (e.g. 50,000 per hour)
      val otHours = allLemburs
        .filter { it.nik == emp.nik && it.statusPersetujuan == "Approved" && it.tanggal.startsWith(periode) }
        .sumOf { it.durasiJam }
      val otPay = (otHours * 50000).toLong()

      val potonganLain = 120000L // BPJS Kesehatan & Ketenagakerjaan contribution
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
  }

  suspend fun addAnnouncement(judul: String, isi: String, kategori: String) {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    db.announcementDao().insert(
      AnnouncementEntity(
        judul = judul,
        isi = isi,
        kategori = kategori,
        tanggal = sdf.format(Date())
      )
    )
  }

  suspend fun updateEmployeePhoto(nik: String, photoUrl: String) {
    val emp = db.employeeDao().getEmployeeByNik(nik) ?: return
    db.employeeDao().insertOrUpdate(emp.copy(fotoUrl = photoUrl))
  }
}
