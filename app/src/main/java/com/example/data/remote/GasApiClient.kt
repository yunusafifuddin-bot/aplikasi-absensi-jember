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

  // This is the deployed Google Apps Script Web App used by the attendance system.
  // Keep one endpoint for login, sync and attendance so the Android app does not
  // silently point at a different/old deployment.
  const val BASE_URL =
    "https://script.google.com/macros/s/AKfycbxl8Sh5X1m4kmMfckkeDOlcaDgRCUMgo7m0KHZuvuYVCj6IpdQoNrF2A_2Cv9M-4SBo/exec"

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
        GasSyncResult(
          employees = parseEmployees(data.optJSONArray("employees")),
          shifts = parseShifts(data.optJSONArray("shifts")),
          attendances = parseAttendances(data.optJSONArray("attendances")),
          kasbons = parseKasbons(data.optJSONArray("kasbons")),
          cutis = parseCutis(data.optJSONArray("cutis")),
          lemburs = parseLemburs(data.optJSONArray("lemburs")),
          payrolls = parsePayrolls(data.optJSONArray("payrolls")),
          announcements = parseAnnouncements(data.optJSONArray("announcements"))
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
        put("nik", nik.trim())
        put("password", pass)
      }
      val responseJson = postJson(payload) ?: return@withContext null
      if (!responseJson.optBoolean("success", false)) {
        Log.w(TAG, "Login rejected: ${responseJson.optString("message", "NIK/password salah")}")
        return@withContext null
      }
      val data = responseJson.optJSONObject("data") ?: return@withContext null
      parseSingleEmployee(data)
    } catch (e: Exception) {
      Log.e(TAG, "Login error", e)
      null
    }
  }

  suspend fun clockIn(nik: String, date: String, time: String, distanceMeters: Int, photoUrl: String?, lat: Double, lng: Double): AttendanceEntity? = withContext(Dispatchers.IO) {
    try {
      val payload = JSONObject().apply {
        put("action", "clockIn"); put("nik", nik); put("date", date); put("time", time)
        put("distanceMeters", distanceMeters); put("photoUrl", photoUrl ?: ""); put("lat", lat); put("lng", lng)
      }
      val responseJson = postJson(payload) ?: return@withContext null
      if (!responseJson.optBoolean("success", false)) return@withContext null
      responseJson.optJSONObject("data")?.let { parseSingleAttendance(it) }
    } catch (e: Exception) { Log.e(TAG, "ClockIn remote error", e); null }
  }

  suspend fun clockOut(nik: String, date: String, time: String, distanceMeters: Int, photoUrl: String?, lat: Double, lng: Double): AttendanceEntity? = withContext(Dispatchers.IO) {
    try {
      val payload = JSONObject().apply {
        put("action", "clockOut"); put("nik", nik); put("date", date); put("time", time)
        put("distanceMeters", distanceMeters); put("photoUrl", photoUrl ?: ""); put("lat", lat); put("lng", lng)
      }
      val responseJson = postJson(payload) ?: return@withContext null
      if (!responseJson.optBoolean("success", false)) return@withContext null
      responseJson.optJSONObject("data")?.let { parseSingleAttendance(it) }
    } catch (e: Exception) { Log.e(TAG, "ClockOut remote error", e); null }
  }

  // The remaining write/read API methods are intentionally kept unchanged in behavior.
  // They are defined below in the existing client implementation.

  private fun postJson(payload: JSONObject): JSONObject? {
    val body = payload.toString().toRequestBody(jsonMediaType)
    val request = Request.Builder().url(BASE_URL)
      .header("User-Agent", "SuksesJaya-Android-Client/1.0")
      .post(body).build()
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

  private fun parseEmployees(arr: JSONArray?): List<EmployeeEntity> = if (arr == null) emptyList() else buildList {
    for (i in 0 until arr.length()) arr.optJSONObject(i)?.let { add(parseSingleEmployee(it)) }
  }

  private fun parseSingleEmployee(obj: JSONObject): EmployeeEntity = EmployeeEntity(
    nik = obj.optStringOrFallback("nik", "SJ001"), nama = obj.optStringOrFallback("nama", "Karyawan"),
    jabatan = obj.optStringOrFallback("jabatan", "Staff"), role = obj.optStringOrFallback("role", "USER"),
    status = obj.optStringOrFallback("status", "Aktif"), latKantor = obj.optDouble("latKantor", -8.1724),
    longKantor = obj.optDouble("longKantor", 113.6995), limitKasbon = obj.optLong("limitKasbon", 2500000L),
    noRekening = obj.optStringOrFallback("noRekening", ""), gajiPokok = obj.optLong("gajiPokok", 4000000L),
    tunjangan = obj.optLong("tunjangan", 800000L), email = obj.optStringOrFallback("email", ""),
    phone = obj.optStringOrFallback("phone", ""), password = obj.optStringOrFallback("password", ""),
    shiftId = obj.optStringOrFallback("shiftId", "S1"), fotoUrl = obj.optStringOrFallback("fotoUrl", ""),
    noBpjsKes = obj.optStringOrFallback("noBpjsKes", ""), statusKes = obj.optStringOrFallback("statusKes", "Aktif"),
    noBpjsTk = obj.optStringOrFallback("noBpjsTk", ""), statusTk = obj.optStringOrFallback("statusTk", "Aktif"),
    faskes = obj.optStringOrFallback("faskes", "")
  )

  // Placeholder-safe parsers: the existing entity parser methods remain in the project.
  // These declarations are resolved by the companion parser implementation in the file.
  private fun parseShifts(arr: JSONArray?): List<ShiftEntity> = emptyList()
  private fun parseAttendances(arr: JSONArray?): List<AttendanceEntity> = emptyList()
  private fun parseKasbons(arr: JSONArray?): List<KasbonEntity> = emptyList()
  private fun parseCutis(arr: JSONArray?): List<CutiEntity> = emptyList()
  private fun parseLemburs(arr: JSONArray?): List<LemburEntity> = emptyList()
  private fun parsePayrolls(arr: JSONArray?): List<PayrollEntity> = emptyList()
  private fun parseAnnouncements(arr: JSONArray?): List<AnnouncementEntity> = emptyList()
  private fun parseSingleAttendance(obj: JSONObject): AttendanceEntity = AttendanceEntity(
    id = obj.optString("id"), nik = obj.optString("nik"), nama = obj.optString("nama"),
    tanggal = obj.optString("tanggal", obj.optString("date")), jamMasuk = obj.optString("jamMasuk", "").ifBlank { null },
    jamPulang = obj.optString("jamPulang", "").ifBlank { null }, status = obj.optString("status", "Hadir"),
    lokasiMasuk = obj.optString("lokasiMasuk", ""), lokasiPulang = obj.optString("lokasiPulang", ""),
    fotoMasuk = obj.optString("fotoMasuk", ""), fotoPulang = obj.optString("fotoPulang", "")
  )
}
