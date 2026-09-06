package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.*
import com.example.data.repository.HrisRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

enum class AppPage(val title: String) {
  HOME("Dashboard"),
  HISTORY("Riwayat Absensi"),
  CALENDAR("Kalender Kehadiran"),
  FINANCIAL("Financial / Kasbon"),
  REQUESTS("Cuti & Lembur"),
  PAYSLIP("Slip Gaji"),
  PROFILE("Profil & BPJS"),
  ADMIN("HR Dashboard"),
  EMPLOYEES("Karyawan"),
  SHIFTS("Shift Kerja"),
  PAYROLL_ADMIN("Payroll"),
  APPROVALS("Approval"),
  REPORTS("Laporan"),
  ANNOUNCEMENTS("Pengumuman")
}

data class GpsLocationState(
  val lat: Double = -8.17242,
  val lng: Double = 113.69948,
  val accuracyMeters: Float = 12f,
  val distanceToOfficeMeters: Int = 18,
  val isWithinGeofence: Boolean = true, // <= 100m
  val officeLat: Double = -8.1724,
  val officeLng: Double = 113.6995,
  val geofenceRadiusMeters: Int = 100
)

class HrisViewModel(application: Application) : AndroidViewModel(application) {

  private val database = AppDatabase.getDatabase(application)
  val repository = HrisRepository(database)

  private val _currentUser = MutableStateFlow<EmployeeEntity?>(null)
  val currentUser: StateFlow<EmployeeEntity?> = _currentUser.asStateFlow()

  private val _currentPage = MutableStateFlow(AppPage.HOME)
  val currentPage: StateFlow<AppPage> = _currentPage.asStateFlow()

  private val _snackbarMessage = MutableSharedFlow<String>()
  val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

  private val _gpsState = MutableStateFlow(GpsLocationState())
  val gpsState: StateFlow<GpsLocationState> = _gpsState.asStateFlow()

  private val _capturedSelfieUri = MutableStateFlow<String?>(null)
  val capturedSelfieUri: StateFlow<String?> = _capturedSelfieUri.asStateFlow()

  private val _cameraAttendanceType = MutableStateFlow<String?>(null)
  val cameraAttendanceType: StateFlow<String?> = _cameraAttendanceType.asStateFlow()

  private val _isSyncing = MutableStateFlow(false)
  val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

  private val _lastSyncTime = MutableStateFlow<String?>(null)
  val lastSyncTime: StateFlow<String?> = _lastSyncTime.asStateFlow()

  // Selected filters
  val currentMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
  private val _selectedMonth = MutableStateFlow(currentMonth)
  val selectedMonth: StateFlow<String> = _selectedMonth.asStateFlow()

  val allEmployees = repository.allEmployees.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
  val allShifts = repository.allShifts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
  val allAnnouncements = repository.allAnnouncements.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
  val allAttendances = repository.allAttendances.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
  val allKasbons = repository.allKasbons.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
  val allCutis = repository.allCutis.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
  val allLemburs = repository.allLemburs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
  val allPayrolls = repository.allPayrolls.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  init {
    viewModelScope.launch {
      DatabaseInitializer.preseedIfEmpty(database)
      // Auto login with default employee for preview ease, or user can log out
      val defaultEmp = database.employeeDao().getEmployeeByNik("SJ001")
      _currentUser.value = defaultEmp
      updateGpsDistance()
      // Initial background sync with Google Apps Script
      syncDatabase(showFeedback = false)
    }
  }

  fun syncDatabase(showFeedback: Boolean = true) {
    if (_isSyncing.value) return
    viewModelScope.launch {
      _isSyncing.value = true
      val res = repository.syncWithRemote()
      _isSyncing.value = false
      if (res.isSuccess) {
        val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        _lastSyncTime.value = timeStr
        val curNik = _currentUser.value?.nik
        if (curNik != null) {
          val updatedEmp = database.employeeDao().getEmployeeByNik(curNik)
          if (updatedEmp != null) _currentUser.value = updatedEmp
        }
        if (showFeedback) {
          emitMessage("Sinkronisasi database Google Sheets berhasil ($timeStr)")
        }
      } else if (showFeedback) {
        emitMessage("Gagal sinkron database: ${res.exceptionOrNull()?.message ?: "Periksa koneksi"}")
      }
    }
  }

  fun selectPage(page: AppPage) {
    _currentPage.value = page
  }

  fun setSelectedMonth(month: String) {
    _selectedMonth.value = month
  }

  fun login(nik: String, pass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
    viewModelScope.launch {
      val emp = repository.login(nik.trim(), pass)
      if (emp != null) {
        _currentUser.value = emp
        _currentPage.value = AppPage.HOME
        onSuccess()
      } else {
        onError("NIK atau password salah. Coba NIK: SJ001 / Pass: 123 atau NIK: SJ003 / Pass: admin")
      }
    }
  }

  fun logout() {
    _currentUser.value = null
    _currentPage.value = AppPage.HOME
  }

  fun switchUser(nik: String) {
    viewModelScope.launch {
      val emp = database.employeeDao().getEmployeeByNik(nik)
      if (emp != null) {
        _currentUser.value = emp
        emitMessage("Beralih ke akun ${emp.nama} (${emp.role})")
      }
    }
  }

  fun setGpsLocation(lat: Double, lng: Double, accuracy: Float = 15f) {
    val cur = _gpsState.value
    val distance = calculateHaversine(lat, lng, cur.officeLat, cur.officeLng)
    _gpsState.value = cur.copy(
      lat = lat,
      lng = lng,
      accuracyMeters = accuracy,
      distanceToOfficeMeters = distance.toInt(),
      isWithinGeofence = distance <= cur.geofenceRadiusMeters && accuracy <= 100f
    )
  }

  fun toggleGpsTestLocation(insideOffice: Boolean) {
    if (insideOffice) {
      // 18 meters from office
      setGpsLocation(-8.17242, 113.69948, 12f)
      emitMessage("Lokasi diset: Dalam radius kantor (18m - Valid)")
    } else {
      // 350 meters outside office
      setGpsLocation(-8.17550, 113.70250, 20f)
      emitMessage("Lokasi diset: Di luar radius kantor (350m - Tidak Valid)")
    }
  }

  private fun updateGpsDistance() {
    val cur = _gpsState.value
    val distance = calculateHaversine(cur.lat, cur.lng, cur.officeLat, cur.officeLng)
    _gpsState.value = cur.copy(
      distanceToOfficeMeters = distance.toInt(),
      isWithinGeofence = distance <= cur.geofenceRadiusMeters
    )
  }

  private fun calculateHaversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371000.0 // Earth radius in meters
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return r * c
  }

  fun setCapturedSelfie(uri: String?) {
    _capturedSelfieUri.value = uri
  }

  fun openCameraAttendance(type: String? = null) {
    val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val user = _currentUser.value
    val todayAtt = allAttendances.value.firstOrNull { it.nik == user?.nik && it.tanggal == todayDate }
    val determinedType = type ?: if (todayAtt?.jamMasuk == null) {
      "masuk"
    } else if (todayAtt.jamPulang == null) {
      "pulang"
    } else {
      "masuk"
    }
    _cameraAttendanceType.value = determinedType
  }

  fun closeCameraAttendance() {
    _cameraAttendanceType.value = null
  }

  fun performAttendance(type: String, photoUrl: String? = null) { // "masuk" or "pulang"
    val user = _currentUser.value ?: return
    val gps = _gpsState.value

    if (!gps.isWithinGeofence) {
      emitMessage("Presensi ditolak! Jarak Anda ${gps.distanceToOfficeMeters}m dari kantor (Maksimal 100m).")
      return
    }

    val finalPhoto = photoUrl ?: _capturedSelfieUri.value
    val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val nowTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

    viewModelScope.launch {
      if (type == "masuk") {
        val res = repository.clockIn(
          nik = user.nik,
          date = todayDate,
          time = nowTime,
          distanceMeters = gps.distanceToOfficeMeters,
          photoUrl = finalPhoto
        )
        if (res.isSuccess) {
          emitMessage("Berhasil absen masuk pada $nowTime dengan foto kamera.")
        } else {
          emitMessage("Gagal absen: ${res.exceptionOrNull()?.message}")
        }
      } else {
        val res = repository.clockOut(
          nik = user.nik,
          date = todayDate,
          time = nowTime,
          distanceMeters = gps.distanceToOfficeMeters,
          photoUrl = finalPhoto
        )
        if (res.isSuccess) {
          emitMessage("Berhasil absen pulang pada $nowTime dengan foto kamera.")
        } else {
          emitMessage("Gagal absen: ${res.exceptionOrNull()?.message}")
        }
      }
      closeCameraAttendance()
    }
  }

  fun submitKasbon(amount: Long, reason: String) {
    val user = _currentUser.value ?: return
    viewModelScope.launch {
      repository.submitKasbon(user.nik, user.nama, amount, reason)
      emitMessage("Pengajuan kasbon sebesar Rp ${Number(amount)} berhasil dikirim.")
    }
  }

  fun submitCuti(jenis: String, start: String, end: String, reason: String) {
    val user = _currentUser.value ?: return
    viewModelScope.launch {
      repository.submitCuti(user.nik, user.nama, jenis, start, end, reason)
      emitMessage("Pengajuan $jenis berhasil dikirim.")
    }
  }

  fun submitLembur(date: String, start: String, end: String, desc: String) {
    val user = _currentUser.value ?: return
    viewModelScope.launch {
      repository.submitLembur(user.nik, user.nama, date, start, end, desc)
      emitMessage("Pengajuan lembur tanggal $date berhasil dikirim.")
    }
  }

  fun approveApproval(type: String, id: String) {
    viewModelScope.launch {
      when (type.lowercase()) {
        "kasbon" -> repository.updateKasbonStatus(id, "Approved")
        "cuti" -> repository.updateCutiStatus(id, "Approved")
        "lembur" -> repository.updateLemburStatus(id, "Approved")
      }
      emitMessage("Pengajuan $id telah disetujui (Approved).")
    }
  }

  fun rejectApproval(type: String, id: String) {
    viewModelScope.launch {
      when (type.lowercase()) {
        "kasbon" -> repository.updateKasbonStatus(id, "Rejected")
        "cuti" -> repository.updateCutiStatus(id, "Rejected")
        "lembur" -> repository.updateLemburStatus(id, "Rejected")
      }
      emitMessage("Pengajuan $id telah ditolak (Rejected).")
    }
  }

  fun saveEmployee(employee: EmployeeEntity) {
    viewModelScope.launch {
      repository.saveEmployee(employee)
      emitMessage("Data karyawan ${employee.nama} tersimpan.")
    }
  }

  fun deleteEmployee(nik: String) {
    viewModelScope.launch {
      repository.deleteEmployee(nik)
      emitMessage("Karyawan $nik dihapus.")
    }
  }

  fun saveShift(shift: ShiftEntity) {
    viewModelScope.launch {
      repository.saveShift(shift)
      emitMessage("Shift ${shift.namaShift} tersimpan.")
    }
  }

  fun deleteShift(shiftId: String) {
    viewModelScope.launch {
      repository.deleteShift(shiftId)
      emitMessage("Shift $shiftId dihapus.")
    }
  }

  fun generatePayroll(periode: String, targetNik: String = "") {
    viewModelScope.launch {
      repository.generatePayroll(periode, targetNik)
      emitMessage("Payroll periode $periode berhasil di-generate.")
    }
  }

  fun markPayrollPaid(id: String) {
    viewModelScope.launch {
      repository.markPayrollPaid(id)
      emitMessage("Slip $id ditandai sebagai Sudah Dibayar (Paid).")
    }
  }

  fun addAnnouncement(judul: String, isi: String, kategori: String) {
    viewModelScope.launch {
      repository.addAnnouncement(judul, isi, kategori)
      emitMessage("Pengumuman berhasil diterbitkan.")
    }
  }

  fun updateProfilePhoto(uri: String) {
    val user = _currentUser.value ?: return
    viewModelScope.launch {
      repository.updateEmployeePhoto(user.nik, uri)
      _currentUser.value = _currentUser.value?.copy(fotoUrl = uri)
      emitMessage("Foto profil diperbarui.")
    }
  }

  fun emitMessage(msg: String) {
    viewModelScope.launch {
      _snackbarMessage.emit(msg)
    }
  }

  private fun Number(n: Long): String {
    return String.format(Locale.GERMANY, "%,d", n)
  }
}
