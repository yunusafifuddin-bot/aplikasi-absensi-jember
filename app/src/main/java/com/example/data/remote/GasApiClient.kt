package com.example.data.remote

import android.util.Log
import com.example.data.local.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import java.util.concurrent.TimeUnit

data class GasSyncResult(
  val employees: List<EmployeeEntity>,
  val shifts: List<ShiftEntity>,
  val attendances: List<AttendanceEntity>,
  val kasbons: List<KasbonEntity>,
  val cutis: List<CutiEntity>,
  val lemburs: List<LemburEntity>,
  val payrolls: List<PayrollEntity>,
  val announcements: List<AnnouncementEntity>
)

object GasApiClient {
  private const val TAG = "GasApiClient"
  const val BASE_URL =
    "https://script.google.com/macros/s/AKfycbys9JlNR3YB2nb4xsgp6QeN_qw1cezJ1gjrinKA5dI7P_UTofxAM9U2h4ateMJf6WFnWg/exec"

  private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

  private val client = OkHttpClient.Builder()
    .connectTimeout(25, TimeUnit.SECONDS)
    .readTimeout(25, TimeUnit.SECONDS)
    .writeTimeout(25, TimeUnit.SECONDS)
    .followRedirects(true)
    .followSslRedirects(true)
    .build()

  suspend fun fetchSyncData(): GasSyncResult? = withContext(Dispatchers.IO) {
    try {
      val request = Request.Builder()
        .url("$BASE_URL?action=sync")
        .header("User-Agent", "SuksesJaya-Android-Client/1.0")
        .get()
        .build()

      client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
          Log.e(TAG, "Sync failed with HTTP code ${response.code}")
          return@withContext null
        }
        val bodyStr = response.body?.string() ?: return@withContext null
        val root = JSONObject(bodyStr)
        if (!root.optBoolean("success", true)) {
          Log.e(TAG, "Sync returned failure: ${root.optString("message")}")
          return@withContext null
        }

        val data = root.optJSONObject("data") ?: return@withContext null

        val employees = parseEmployees(data.optJSONArray("employees"))
        val shifts = parseShifts(data.optJSONArray("shifts"))
        val attendances = parseAttendances(data.optJSONArray("attendances"))
        val kasbons = parseKasbons(data.optJSONArray("kasbons"))
        val cutis = parseCutis(data.optJSONArray("cutis"))
        val lemburs = parseLemburs(data.optJSONArray("lemburs"))
        val payrolls = parsePayrolls(data.optJSONArray("payrolls"))
        val announcements = parseAnnouncements(data.optJSONArray("announcements"))

        GasSyncResult(
          employees = employees,
          shifts = shifts,
          attendances = attendances,
          kasbons = kasbons,
          cutis = cutis,
          lemburs = lemburs,
          payrolls = payrolls,
          announcements = announcements
        )
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error fetching sync data from GAS", e)
      null
    }
  }

  suspend fun login(nik: String, pass: String): EmployeeEntity? = withContext(Dispatchers.IO) {
    try {
      val payload = JSONObject().apply {
        put("action", "login")
        put("nik", nik)
        put("password", pass)
      }
      val responseJson = postJson(payload) ?: return@withContext null
      if (!responseJson.optBoolean("success", false)) return@withContext null

      val data = responseJson.optJSONObject("data") ?: return@withContext null
      parseSingleEmployee(data)
    } catch (e: Exception) {
      Log.e(TAG, "Login error", e)
      null
    }
  }

  suspend fun clockIn(
    nik: String,
    date: String,
    time: String,
    distanceMeters: Int,
    photoUrl: String?,
    lat: Double,
    lng: Double
  ): AttendanceEntity? = withContext(Dispatchers.IO) {
    try {
      val payload = JSONObject().apply {
        put("action", "clockIn")
        put("nik", nik)
        put("date", date)
        put("time", time)
        put("distanceMeters", distanceMeters)
        put("photoUrl", photoUrl ?: "")
        put("lat", lat)
        put("lng", lng)
      }
      val responseJson = postJson(payload) ?: return@withContext null
      if (!responseJson.optBoolean("success", false)) return@withContext null

      val data = responseJson.optJSONObject("data")
      if (data != null) {
        parseSingleAttendance(data)
      } else {
        null
      }
    } catch (e: Exception) {
      Log.e(TAG, "ClockIn remote error", e)
      null
    }
  }

  suspend fun clockOut(
    nik: String,
    date: String,
    time: String,
    distanceMeters: Int,
    photoUrl: String?,
    lat: Double,
    lng: Double
  ): AttendanceEntity? = withContext(Dispatchers.IO) {
    try {
      val payload = JSONObject().apply {
        put("action", "clockOut")
        put("nik", nik)
        put("date", date)
        put("time", time)
        put("distanceMeters", distanceMeters)
        put("photoUrl", photoUrl ?: "")
        put("lat", lat)
        put("lng", lng)
      }
      val responseJson = postJson(payload) ?: return@withContext null
      if (!responseJson.optBoolean("success", false)) return@withContext null

      val data = responseJson.optJSONObject("data")
      if (data != null) {
        parseSingleAttendance(data)
      } else {
        null
      }
    } catch (e: Exception) {
      Log.e(TAG, "ClockOut remote error", e)
      null
    }
  }

  suspend fun submitKasbon(nik: String, nama: String, amount: Long, reason: String): KasbonEntity? =
    withContext(Dispatchers.IO) {
      try {
        val payload = JSONObject().apply {
          put("action", "submitKasbon")
          put("nik", nik)
          put("nama", nama)
          put("amount", amount)
          put("reason", reason)
        }
        val resp = postJson(payload) ?: return@withContext null
        if (!resp.optBoolean("success", false)) return@withContext null
        val data = resp.optJSONObject("data") ?: return@withContext null
        parseSingleKasbon(data)
      } catch (e: Exception) {
        Log.e(TAG, "submitKasbon error", e)
        null
      }
    }

  suspend fun submitCuti(
    nik: String,
    nama: String,
    jenis: String,
    startDate: String,
    endDate: String,
    reason: String
  ): CutiEntity? = withContext(Dispatchers.IO) {
    try {
      val payload = JSONObject().apply {
        put("action", "submitCuti")
        put("nik", nik)
        put("nama", nama)
        put("jenis", jenis)
        put("startDate", startDate)
        put("endDate", endDate)
        put("reason", reason)
        put("lampiran", "")
      }
      val resp = postJson(payload) ?: return@withContext null
      if (!resp.optBoolean("success", false)) return@withContext null
      val data = resp.optJSONObject("data") ?: return@withContext null
      parseSingleCuti(data)
    } catch (e: Exception) {
      Log.e(TAG, "submitCuti error", e)
      null
    }
  }

  suspend fun submitLembur(
    nik: String,
    nama: String,
    date: String,
    startTime: String,
    endTime: String,
    desc: String,
    durationHours: Double
  ): LemburEntity? = withContext(Dispatchers.IO) {
    try {
      val payload = JSONObject().apply {
        put("action", "submitLembur")
        put("nik", nik)
        put("nama", nama)
        put("date", date)
        put("startTime", startTime)
        put("endTime", endTime)
        put("desc", desc)
        put("deskripsi", desc)
        put("durasiJam", durationHours)
      }
      val resp = postJson(payload) ?: return@withContext null
      if (!resp.optBoolean("success", false)) return@withContext null
      val data = resp.optJSONObject("data") ?: return@withContext null
      parseSingleLembur(data)
    } catch (e: Exception) {
      Log.e(TAG, "submitLembur error", e)
      null
    }
  }

  suspend fun updateKasbonStatus(id: String, status: String): Boolean = withContext(Dispatchers.IO) {
    try {
      val payload = JSONObject().apply {
        put("action", "updateKasbonStatus")
        put("id", id)
        put("status", status)
      }
      val resp = postJson(payload)
      resp?.optBoolean("success", false) == true
    } catch (e: Exception) {
      Log.e(TAG, "updateKasbonStatus error", e)
      false
    }
  }

  suspend fun updateCutiStatus(id: String, status: String): Boolean = withContext(Dispatchers.IO) {
    try {
      val payload = JSONObject().apply {
        put("action", "updateCutiStatus")
        put("id", id)
        put("status", status)
      }
      val resp = postJson(payload)
      resp?.optBoolean("success", false) == true
    } catch (e: Exception) {
      Log.e(TAG, "updateCutiStatus error", e)
      false
    }
  }

  suspend fun updateLemburStatus(id: String, status: String): Boolean = withContext(Dispatchers.IO) {
    try {
      val payload = JSONObject().apply {
        put("action", "updateLemburStatus")
        put("id", id)
        put("status", status)
      }
      val resp = postJson(payload)
      resp?.optBoolean("success", false) == true
    } catch (e: Exception) {
      Log.e(TAG, "updateLemburStatus error", e)
      false
    }
  }

  suspend fun saveEmployee(employee: EmployeeEntity): Boolean = withContext(Dispatchers.IO) {
    try {
      val empObj = JSONObject().apply {
        put("nik", employee.nik)
        put("nama", employee.nama)
        put("jabatan", employee.jabatan)
        put("role", employee.role)
        put("status", employee.status)
        put("latKantor", employee.latKantor)
        put("longKantor", employee.longKantor)
        put("limitKasbon", employee.limitKasbon)
        put("noRekening", employee.noRekening)
        put("gajiPokok", employee.gajiPokok)
        put("tunjangan", employee.tunjangan)
        put("email", employee.email)
        put("phone", employee.phone)
        put("password", employee.password)
        put("shiftId", employee.shiftId)
        put("fotoUrl", employee.fotoUrl)
        put("noBpjsKes", employee.noBpjsKes)
        put("statusKes", employee.statusKes)
        put("noBpjsTk", employee.noBpjsTk)
        put("statusTk", employee.statusTk)
        put("faskes", employee.faskes)
      }
      val payload = JSONObject().apply {
        put("action", "saveEmployee")
        put("employee", empObj)
        // Also put direct fields for maximum compatibility
        put("nik", employee.nik)
        put("nama", employee.nama)
        put("jabatan", employee.jabatan)
        put("role", employee.role)
      }
      val resp = postJson(payload)
      resp?.optBoolean("success", false) == true
    } catch (e: Exception) {
      Log.e(TAG, "saveEmployee error", e)
      false
    }
  }

  suspend fun deleteEmployee(nik: String): Boolean = withContext(Dispatchers.IO) {
    try {
      val payload = JSONObject().apply {
        put("action", "deleteEmployee")
        put("nik", nik)
      }
      val resp = postJson(payload)
      resp?.optBoolean("success", false) == true
    } catch (e: Exception) {
      Log.e(TAG, "deleteEmployee error", e)
      false
    }
  }

  suspend fun saveShift(shift: ShiftEntity): Boolean = withContext(Dispatchers.IO) {
    try {
      val shiftObj = JSONObject().apply {
        put("shiftId", shift.shiftId)
        put("namaShift", shift.namaShift)
        put("jamMasuk", shift.jamMasuk)
        put("jamPulang", shift.jamPulang)
        put("toleransiMenit", shift.toleransiMenit)
        put("keterangan", shift.keterangan)
      }
      val payload = JSONObject().apply {
        put("action", "saveShift")
        put("shift", shiftObj)
        put("shiftId", shift.shiftId)
        put("namaShift", shift.namaShift)
        put("jamMasuk", shift.jamMasuk)
        put("jamPulang", shift.jamPulang)
        put("toleransiMenit", shift.toleransiMenit)
        put("keterangan", shift.keterangan)
      }
      val resp = postJson(payload)
      resp?.optBoolean("success", false) == true
    } catch (e: Exception) {
      Log.e(TAG, "saveShift error", e)
      false
    }
  }

  suspend fun deleteShift(shiftId: String): Boolean = withContext(Dispatchers.IO) {
    try {
      val payload = JSONObject().apply {
        put("action", "deleteShift")
        put("shiftId", shiftId)
      }
      val resp = postJson(payload)
      resp?.optBoolean("success", false) == true
    } catch (e: Exception) {
      Log.e(TAG, "deleteShift error", e)
      false
    }
  }

  suspend fun generatePayroll(periode: String, targetNik: String = ""): Boolean =
    withContext(Dispatchers.IO) {
      try {
        val payload = JSONObject().apply {
          put("action", "generatePayroll")
          put("periode", periode)
          if (targetNik.isNotBlank()) put("targetNik", targetNik)
        }
        val resp = postJson(payload)
        resp?.optBoolean("success", false) == true
      } catch (e: Exception) {
        Log.e(TAG, "generatePayroll error", e)
        false
      }
    }

  suspend fun markPayrollPaid(id: String): Boolean = withContext(Dispatchers.IO) {
    try {
      val payload = JSONObject().apply {
        put("action", "markPayrollPaid")
        put("id", id)
      }
      val resp = postJson(payload)
      resp?.optBoolean("success", false) == true
    } catch (e: Exception) {
      Log.e(TAG, "markPayrollPaid error", e)
      false
    }
  }

  suspend fun addAnnouncement(judul: String, isi: String, kategori: String): AnnouncementEntity? =
    withContext(Dispatchers.IO) {
      try {
        val payload = JSONObject().apply {
          put("action", "addAnnouncement")
          put("judul", judul)
          put("isi", isi)
          put("kategori", kategori)
          put("target", "Semua")
        }
        val resp = postJson(payload) ?: return@withContext null
        if (!resp.optBoolean("success", false)) return@withContext null
        val data = resp.optJSONObject("data") ?: return@withContext null
        parseSingleAnnouncement(data)
      } catch (e: Exception) {
        Log.e(TAG, "addAnnouncement error", e)
        null
      }
    }

  private fun postJson(payload: JSONObject): JSONObject? {
    val body = payload.toString().toRequestBody(jsonMediaType)
    val request = Request.Builder()
      .url(BASE_URL)
      .header("User-Agent", "SuksesJaya-Android-Client/1.0")
      .post(body)
      .build()

    return client.newCall(request).execute().use { response ->
      if (!response.isSuccessful) {
        Log.e(TAG, "POST failed with code ${response.code}")
        null
      } else {
        val bodyStr = response.body?.string() ?: return null
        JSONObject(bodyStr)
      }
    }
  }

  // --- Parsing Helpers ---

  private fun parseEmployees(arr: JSONArray?): List<EmployeeEntity> {
    if (arr == null) return emptyList()
    val list = mutableListOf<EmployeeEntity>()
    for (i in 0 until arr.length()) {
      val obj = arr.optJSONObject(i) ?: continue
      list.add(parseSingleEmployee(obj))
    }
    return list
  }

  private fun parseSingleEmployee(obj: JSONObject): EmployeeEntity {
    return EmployeeEntity(
      nik = obj.optStringOrFallback("nik", "SJ001"),
      nama = obj.optStringOrFallback("nama", "Karyawan"),
      jabatan = obj.optStringOrFallback("jabatan", "Staff"),
      role = obj.optStringOrFallback("role", "USER"),
      status = obj.optStringOrFallback("status", "Aktif"),
      latKantor = obj.optDouble("latKantor", -8.1724),
      longKantor = obj.optDouble("longKantor", 113.6995),
      limitKasbon = obj.optLong("limitKasbon", 2500000L),
      noRekening = obj.optStringOrFallback("noRekening", "BCA 00000000"),
      gajiPokok = obj.optLong("gajiPokok", 4000000L),
      tunjangan = obj.optLong("tunjangan", 800000L),
      email = obj.optStringOrFallback("email", ""),
      phone = obj.optStringOrFallback("phone", ""),
      password = obj.optStringOrFallback("password", "123"),
      shiftId = obj.optStringOrFallback("shiftId", "S1"),
      fotoUrl = obj.optStringOrFallback("fotoUrl", ""),
      noBpjsKes = obj.optStringOrFallback("noBpjsKes", ""),
      statusKes = obj.optStringOrFallback("statusKes", "Aktif"),
      noBpjsTk = obj.optStringOrFallback("noBpjsTk", ""),
      statusTk = obj.optStringOrFallback("statusTk", "Aktif"),
      faskes = obj.optStringOrFallback("faskes", "Klinik Pratama Sukses Sehat")
    )
  }

  private fun parseShifts(arr: JSONArray?): List<ShiftEntity> {
    if (arr == null) return emptyList()
    val list = mutableListOf<ShiftEntity>()
    for (i in 0 until arr.length()) {
      val obj = arr.optJSONObject(i) ?: continue
      val shiftId = obj.optStringOrFallback("shiftId", "S1")
      val defaultStart = when (shiftId) {
        "S2" -> "06:00"
        "S3" -> "13:00"
        else -> "08:00"
      }
      val defaultEnd = when (shiftId) {
        "S2" -> "15:00"
        "S3" -> "21:00"
        else -> "17:00"
      }
      list.add(
        ShiftEntity(
          shiftId = shiftId,
          namaShift = obj.optStringOrFallback("namaShift", "Shift Standar"),
          jamMasuk = cleanTime(obj.optString("jamMasuk"), defaultStart),
          jamPulang = cleanTime(obj.optString("jamPulang"), defaultEnd),
          toleransiMenit = obj.optInt("toleransiMenit", 15),
          keterangan = obj.optStringOrFallback("keterangan", "")
        )
      )
    }
    return list
  }

  private fun parseAttendances(arr: JSONArray?): List<AttendanceEntity> {
    if (arr == null) return emptyList()
    val list = mutableListOf<AttendanceEntity>()
    for (i in 0 until arr.length()) {
      val obj = arr.optJSONObject(i) ?: continue
      list.add(parseSingleAttendance(obj))
    }
    return list
  }

  private fun parseSingleAttendance(obj: JSONObject): AttendanceEntity {
    return AttendanceEntity(
      id = obj.optLong("id", 0L),
      nik = obj.optStringOrFallback("nik", "SJ001"),
      tanggal = obj.optStringOrFallback("tanggal", "2026-09-01"),
      jamMasuk = cleanNullableTime(obj.optString("jamMasuk")),
      jamPulang = cleanNullableTime(obj.optString("jamPulang")),
      status = obj.optStringOrFallback("status", "Tepat Waktu"),
      jarakMeter = obj.optInt("jarakMeter", 18),
      fotoMasukUrl = obj.optNullableString("fotoMasukUrl"),
      fotoPulangUrl = obj.optNullableString("fotoPulangUrl"),
      lat = obj.optDouble("lat", -8.1724),
      lng = obj.optDouble("lng", 113.6995),
      durasiJam = obj.optDouble("durasiJam", 8.0)
    )
  }

  private fun parseKasbons(arr: JSONArray?): List<KasbonEntity> {
    if (arr == null) return emptyList()
    val list = mutableListOf<KasbonEntity>()
    for (i in 0 until arr.length()) {
      val obj = arr.optJSONObject(i) ?: continue
      list.add(parseSingleKasbon(obj))
    }
    return list
  }

  private fun parseSingleKasbon(obj: JSONObject): KasbonEntity {
    return KasbonEntity(
      idKasbon = obj.optStringOrFallback("idKasbon", "KB-${System.currentTimeMillis()}"),
      nik = obj.optStringOrFallback("nik", "SJ001"),
      nama = obj.optStringOrFallback("nama", "Karyawan"),
      tanggalPengajuan = obj.optStringOrFallback("tanggalPengajuan", "2026-09-01"),
      jumlah = obj.optLong("jumlah", 500000L),
      keterangan = obj.optStringOrFallback("keterangan", ""),
      statusPersetujuan = obj.optStringOrFallback("statusPersetujuan", "Pending")
    )
  }

  private fun parseCutis(arr: JSONArray?): List<CutiEntity> {
    if (arr == null) return emptyList()
    val list = mutableListOf<CutiEntity>()
    for (i in 0 until arr.length()) {
      val obj = arr.optJSONObject(i) ?: continue
      list.add(parseSingleCuti(obj))
    }
    return list
  }

  private fun parseSingleCuti(obj: JSONObject): CutiEntity {
    return CutiEntity(
      idCuti = obj.optStringOrFallback("idCuti", "CT-${System.currentTimeMillis()}"),
      nik = obj.optStringOrFallback("nik", "SJ001"),
      nama = obj.optStringOrFallback("nama", "Karyawan"),
      jenisCuti = obj.optStringOrFallback("jenisCuti", "Cuti Tahunan"),
      tglMulai = obj.optStringOrFallback("tglMulai", "2026-09-01"),
      tglSelesai = obj.optStringOrFallback("tglSelesai", "2026-09-02"),
      alasan = obj.optStringOrFallback("alasan", ""),
      status = obj.optStringOrFallback("status", "Pending"),
      lampiran = obj.optNullableString("lampiran")
    )
  }

  private fun parseLemburs(arr: JSONArray?): List<LemburEntity> {
    if (arr == null) return emptyList()
    val list = mutableListOf<LemburEntity>()
    for (i in 0 until arr.length()) {
      val obj = arr.optJSONObject(i) ?: continue
      list.add(parseSingleLembur(obj))
    }
    return list
  }

  private fun parseSingleLembur(obj: JSONObject): LemburEntity {
    return LemburEntity(
      idLembur = obj.optStringOrFallback("idLembur", "OT-${System.currentTimeMillis()}"),
      nik = obj.optStringOrFallback("nik", "SJ001"),
      nama = obj.optStringOrFallback("nama", "Karyawan"),
      tanggal = obj.optStringOrFallback("tanggal", "2026-09-01"),
      jamMulai = cleanTime(obj.optString("jamMulai"), "17:00"),
      jamSelesai = cleanTime(obj.optString("jamSelesai"), "20:00"),
      durasiJam = obj.optDouble("durasiJam", 2.0),
      deskripsi = obj.optStringOrFallback("deskripsi", obj.optString("desc")),
      statusPersetujuan = obj.optStringOrFallback("statusPersetujuan", "Pending")
    )
  }

  private fun parsePayrolls(arr: JSONArray?): List<PayrollEntity> {
    if (arr == null) return emptyList()
    val list = mutableListOf<PayrollEntity>()
    for (i in 0 until arr.length()) {
      val obj = arr.optJSONObject(i) ?: continue
      list.add(
        PayrollEntity(
          idPayroll = obj.optStringOrFallback("idPayroll", "PR-2026-09"),
          nik = obj.optStringOrFallback("nik", "SJ001"),
          nama = obj.optStringOrFallback("nama", "Karyawan"),
          jabatan = obj.optStringOrFallback("jabatan", "Staff"),
          periode = obj.optStringOrFallback("periode", "2026-09"),
          gajiPokok = obj.optLong("gajiPokok", 4000000L),
          tunjangan = obj.optLong("tunjangan", 800000L),
          uangLembur = obj.optLong("uangLembur", 0L),
          potonganKasbon = obj.optLong("potonganKasbon", 0L),
          potonganLain = obj.optLong("potonganLain", 120000L),
          gajiBersih = obj.optLong("gajiBersih", 4680000L),
          status = obj.optStringOrFallback("status", "Draft"),
          tanggalBayar = obj.optNullableString("tanggalBayar")
        )
      )
    }
    return list
  }

  private fun parseAnnouncements(arr: JSONArray?): List<AnnouncementEntity> {
    if (arr == null) return emptyList()
    val list = mutableListOf<AnnouncementEntity>()
    for (i in 0 until arr.length()) {
      val obj = arr.optJSONObject(i) ?: continue
      list.add(parseSingleAnnouncement(obj))
    }
    return list
  }

  private fun parseSingleAnnouncement(obj: JSONObject): AnnouncementEntity {
    return AnnouncementEntity(
      id = obj.optLong("id", 0L),
      judul = obj.optStringOrFallback("judul", "Pengumuman"),
      isi = obj.optStringOrFallback("isi", ""),
      kategori = obj.optStringOrFallback("kategori", "Info"),
      tanggal = obj.optStringOrFallback("tanggal", "2026-09-01"),
      target = obj.optStringOrFallback("target", "Semua")
    )
  }

  // --- String Sanitization Helpers ---

  private fun cleanTime(raw: String?, defaultTime: String): String {
    if (raw.isNullOrBlank() || raw.startsWith("1899") || !raw.contains(":")) return defaultTime
    val parts = raw.split(":")
    return if (parts.size >= 2) {
      val h = parts[0].toIntOrNull() ?: 8
      val m = parts[1].toIntOrNull() ?: 0
      String.format(Locale.US, "%02d:%02d", h, m)
    } else {
      defaultTime
    }
  }

  private fun cleanNullableTime(raw: String?): String? {
    if (raw.isNullOrBlank() || raw.startsWith("1899") || !raw.contains(":")) return null
    val parts = raw.split(":")
    return if (parts.size >= 2) {
      val h = parts[0].toIntOrNull() ?: return null
      val m = parts[1].toIntOrNull() ?: 0
      String.format(Locale.US, "%02d:%02d", h, m)
    } else {
      null
    }
  }

  private fun JSONObject.optStringOrFallback(key: String, fallback: String): String {
    if (!has(key) || isNull(key)) return fallback
    val value = opt(key)?.toString()?.trim()
    return if (value.isNullOrEmpty()) fallback else value
  }

  private fun JSONObject.optNullableString(key: String): String? {
    if (!has(key) || isNull(key)) return null
    val value = opt(key)?.toString()?.trim()
    return if (value.isNullOrEmpty()) null else value
  }
}
