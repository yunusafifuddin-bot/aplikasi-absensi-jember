package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.*
import com.example.data.remote.GitHubUpdateInfo
import com.example.data.remote.GitHubUpdateService
import com.example.data.repository.HrisRepository
import com.example.util.NotificationHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

enum class AppPage(val title: String) {
  HOME("Dashboard"), HISTORY("Riwayat Absensi"), CALENDAR("Kalender Kehadiran"), FINANCIAL("Financial / Kasbon"), REQUESTS("Lembur"), PAYSLIP("Slip Gaji"), PROFILE("Profil & BPJS"), ADMIN("HR Dashboard"), EMPLOYEES("Karyawan"), SHIFTS("Shift Kerja"), PAYROLL_ADMIN("Payroll"), APPROVALS("Approval"), REPORTS("Laporan"), ANNOUNCEMENTS("Pengumuman")
}

data class GpsLocationState(
  val lat: Double = -8.17242,
  val lng: Double = 113.69948,
  val accuracyMeters: Float = 12f,
  val distanceToOfficeMeters: Int = 18,
  val isWithinGeofence: Boolean = true,
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
  private val _cameraAttendanceType = MutableStateFlow<String?>(null)
  val cameraAttendanceType: StateFlow<String?> = _cameraAttendanceType.asStateFlow()
  private val _isSyncing = MutableStateFlow(false)
  val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()
  private val _lastSyncTime = MutableStateFlow<String?>(null)
  val lastSyncTime: StateFlow<String?> = _lastSyncTime.asStateFlow()

  private val _githubRepo = MutableStateFlow(GitHubUpdateService.getConfiguredRepo(application))
  val githubRepo: StateFlow<String> = _githubRepo.asStateFlow()
  private val _githubUpdate = MutableStateFlow<GitHubUpdateInfo?>(null)
  val githubUpdate: StateFlow<GitHubUpdateInfo?> = _githubUpdate.asStateFlow()
  private val _isCheckingUpdate = MutableStateFlow(false)
  val isCheckingUpdate: StateFlow<Boolean> = _isCheckingUpdate.asStateFlow()
  private val _showUpdateDialog = MutableStateFlow(false)
  val showUpdateDialog: StateFlow<Boolean> = _showUpdateDialog.asStateFlow()
  private val _showRepoSettingsDialog = MutableStateFlow(false)
  val showRepoSettingsDialog: StateFlow<Boolean> = _showRepoSettingsDialog.asStateFlow()

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

  init { viewModelScope.launch { syncDatabase(false); checkAppUpdate(false) } }

  fun syncDatabase(showFeedback: Boolean = true) {
    if (_isSyncing.value) return
    viewModelScope.launch {
      _isSyncing.value = true
      val result = repository.syncWithRemote()
      _isSyncing.value = false
      if (result.isSuccess) {
        _lastSyncTime.value = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        _currentUser.value?.nik?.let { nik -> database.employeeDao().getEmployeeByNik(nik)?.let { _currentUser.value = it } }
        if (showFeedback) emitMessage("Data Google Sheets berhasil disegarkan.")
      } else if (showFeedback) emitMessage("Gagal mengambil data Google Sheets: ${result.exceptionOrNull()?.message ?: "Periksa koneksi"}")
    }
  }

  fun selectPage(page: AppPage) { _currentPage.value = page }
  fun setSelectedMonth(month: String) { _selectedMonth.value = month }

  fun login(nik: String, pass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
    viewModelScope.launch {
      val employee = repository.login(nik.trim(), pass)
      if (employee != null) { _currentUser.value = employee; _currentPage.value = AppPage.HOME; syncDatabase(false); onSuccess() }
      else onError("NIK atau password salah, atau server Google Sheets tidak dapat diakses.")
    }
  }
  fun logout() { _currentUser.value = null; _currentPage.value = AppPage.HOME }

  fun setGpsLocation(lat: Double, lng: Double, accuracy: Float = 15f) {
    val cur = _gpsState.value; val distance = calculateHaversine(lat, lng, cur.officeLat, cur.officeLng)
    _gpsState.value = cur.copy(lat = lat, lng = lng, accuracyMeters = accuracy, distanceToOfficeMeters = distance.toInt(), isWithinGeofence = distance <= cur.geofenceRadiusMeters && accuracy <= 100f)
  }
  fun toggleGpsTestLocation(insideOffice: Boolean) { if (insideOffice) setGpsLocation(-8.17242, 113.69948, 12f) else setGpsLocation(-8.17550, 113.70250, 20f) }
  private fun calculateHaversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double { val r = 6371000.0; val dLat = Math.toRadians(lat2 - lat1); val dLon = Math.toRadians(lon2 - lon1); val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2); return r * 2 * atan2(sqrt(a), sqrt(1 - a)) }

  fun setCapturedSelfie(uri: String?) { _capturedSelfieUri.value = uri }
  fun openCameraAttendance(type: String? = null) {
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val row = allAttendances.value.firstOrNull { it.nik == _currentUser.value?.nik && it.tanggal == today }
    _cameraAttendanceType.value = type ?: when { row?.jamMasuk == null -> "masuk"; row.jamPulang == null -> "pulang"; else -> "pulang" }
  }
  fun closeCameraAttendance() { _cameraAttendanceType.value = null }

  fun performAttendance(type: String, photoUrl: String? = null) {
    val user = _currentUser.value ?: return; val gps = _gpsState.value
    if (!gps.isWithinGeofence) { emitMessage("Presensi ditolak: jarak ${gps.distanceToOfficeMeters}m, maksimal 100m."); return }
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()); val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()); val photo = photoUrl ?: _capturedSelfieUri.value
    viewModelScope.launch {
      val result = if (type == "masuk") repository.clockIn(user.nik, today, time, gps.distanceToOfficeMeters, photo, gps.lat, gps.lng) else repository.clockOut(user.nik, today, time, gps.distanceToOfficeMeters, photo, gps.lat, gps.lng)
      if (result.isSuccess) { emitMessage(if (type == "masuk") "Absen masuk berhasil." else "Absen pulang berhasil."); syncDatabase(false) } else emitMessage(result.exceptionOrNull()?.message ?: "Presensi gagal.")
      closeCameraAttendance()
    }
  }

  fun submitKasbon(amount: Long, reason: String) { _currentUser.value?.let { user -> viewModelScope.launch { repository.submitKasbon(user.nik, user.nama, amount, reason); syncDatabase(false); emitMessage("Pengajuan kasbon dikirim.") } } }
  fun submitCuti(jenis: String, start: String, end: String, reason: String) { _currentUser.value?.let { user -> viewModelScope.launch { repository.submitCuti(user.nik, user.nama, jenis, start, end, reason); syncDatabase(false) } } }
  fun submitLembur(date: String, start: String, end: String, desc: String) { _currentUser.value?.let { user -> viewModelScope.launch { repository.submitLembur(user.nik, user.nama, date, start, end, desc); syncDatabase(false); emitMessage("Pengajuan lembur dikirim.") } } }

  fun approveApproval(type: String, id: String) { viewModelScope.launch { when (type.lowercase()) { "kasbon" -> repository.updateKasbonStatus(id, "Approved"); "cuti" -> repository.updateCutiStatus(id, "Approved"); "lembur" -> repository.updateLemburStatus(id, "Approved") }; syncDatabase(false); emitMessage("Pengajuan disetujui.") } }
  fun rejectApproval(type: String, id: String) { viewModelScope.launch { when (type.lowercase()) { "kasbon" -> repository.updateKasbonStatus(id, "Rejected"); "cuti" -> repository.updateCutiStatus(id, "Rejected"); "lembur" -> repository.updateLemburStatus(id, "Rejected") }; syncDatabase(false); emitMessage("Pengajuan ditolak.") } }
  fun saveEmployee(employee: EmployeeEntity) { viewModelScope.launch { repository.saveEmployee(employee); syncDatabase(false) } }
  fun deleteEmployee(nik: String) { viewModelScope.launch { repository.deleteEmployee(nik); syncDatabase(false) } }
  fun saveShift(shift: ShiftEntity) { viewModelScope.launch { repository.saveShift(shift); syncDatabase(false) } }
  fun deleteShift(id: String) { viewModelScope.launch { repository.deleteShift(id); syncDatabase(false) } }
  fun markPayrollPaid(id: String) { viewModelScope.launch { repository.markPayrollPaid(id); syncDatabase(false) } }
  fun generatePayroll(periode: String, targetNik: String = "") { viewModelScope.launch { repository.generatePayroll(periode, targetNik); syncDatabase(false) } }
  fun addAnnouncement(judul: String, isi: String, kategori: String) { viewModelScope.launch { repository.addAnnouncement(judul, isi, kategori); syncDatabase(false) } }
  fun updateEmployeePhoto(nik: String, photoUrl: String) { viewModelScope.launch { repository.updateEmployeePhoto(nik, photoUrl); syncDatabase(false) } }

  fun checkAppUpdate(userInitiated: Boolean = false) {
    if (_isCheckingUpdate.value) return
    viewModelScope.launch {
      _isCheckingUpdate.value = true
      try {
        val info = GitHubUpdateService.checkForUpdates(getApplication(), _githubRepo.value); _githubUpdate.value = info
        if (info?.hasUpdate == true && userInitiated) { _showUpdateDialog.value = true; emitMessage("Pembaruan aplikasi tersedia: ${info.latestVersion}") }
        if (info?.hasUpdate == true) NotificationHelper.showUpdateNotification(getApplication(), "Pembaruan tersedia", "Versi ${info.latestVersion} tersedia.", info.apkDownloadUrl ?: info.releasePageUrl)
      } catch (e: Exception) { if (userInitiated) emitMessage("Gagal memeriksa pembaruan: ${e.message}") } finally { _isCheckingUpdate.value = false }
    }
  }
  fun setGithubRepo(repo: String) { val clean = repo.trim(); _githubRepo.value = clean; GitHubUpdateService.saveConfiguredRepo(getApplication(), clean); checkAppUpdate(true) }
  fun dismissUpdate() { _showUpdateDialog.value = false; _githubUpdate.value = null }
  fun openUpdateDialog() { _showUpdateDialog.value = true }
  fun closeUpdateDialog() { _showUpdateDialog.value = false }
  fun openRepoSettings() { _showRepoSettingsDialog.value = true }
  fun closeRepoSettings() { _showRepoSettingsDialog.value = false }
  fun emitMessage(message: String) { viewModelScope.launch { _snackbarMessage.emit(message) } }
}
