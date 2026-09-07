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
        if (!response.isSuccessful) return@withContext null
        val root = JSONObject(response.body?.string() ?: return@withContext null)
        if (!root.optBoolean("success", true)) return@withContext null
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
    } catch (e: Exception) { Log.e(TAG, "sync", e); null }
  }

  suspend fun login(nik: String, pass: String): EmployeeEntity? = withContext(Dispatchers.IO) {
    try {
      val payload = JSONObject().apply { put("action", "login"); put("nik", nik.trim()); put("password", pass) }
      val responseJson = postJson(payload) ?: return@withContext null
      if (!responseJson.optBoolean("success", false)) return@withContext null
      val data = responseJson.optJSONObject("data") ?: return@withContext null
      parseSingleEmployee(data)
    } catch (e: Exception) { Log.e(TAG, "login", e); null }
  }

  suspend fun clockIn(nik:String,date:String,time:String,distanceMeters:Int,photoUrl:String?,lat:Double,lng:Double): AttendanceEntity? = withContext(Dispatchers.IO) {
    try {
      val p=JSONObject().apply{put("action","clockIn");put("nik",nik);put("date",date);put("time",time);put("distanceMeters",distanceMeters);put("photoUrl",photoUrl?:"");put("lat",lat);put("lng",lng)}
      val r=postJson(p) ?: return@withContext null
      if(!r.optBoolean("success",false)) return@withContext null
      r.optJSONObject("data")?.let{parseSingleAttendance(it)}
    }catch(e:Exception){Log.e(TAG,"clockIn",e);null}
  }

  suspend fun clockOut(nik:String,date:String,time:String,distanceMeters:Int,photoUrl:String?,lat:Double,lng:Double): AttendanceEntity? = withContext(Dispatchers.IO) {
    try {
      val p=JSONObject().apply{put("action","clockOut");put("nik",nik);put("date",date);put("time",time);put("distanceMeters",distanceMeters);put("photoUrl",photoUrl?:"");put("lat",lat);put("lng",lng)}
      val r=postJson(p) ?: return@withContext null
      if(!r.optBoolean("success",false)) return@withContext null
      r.optJSONObject("data")?.let{parseSingleAttendance(it)}
    }catch(e:Exception){Log.e(TAG,"clockOut",e);null}
  }

  suspend fun submitKasbon(nik:String,nama:String,amount:Long,reason:String):KasbonEntity?=null
  suspend fun submitCuti(nik:String,nama:String,jenis:String,startDate:String,endDate:String,reason:String):CutiEntity?=null
  suspend fun submitLembur(nik:String,nama:String,date:String,startTime:String,endTime:String,desc:String,durationHours:Double):LemburEntity?=null
  suspend fun updateKasbonStatus(id:String,status:String):Boolean=false
  suspend fun updateCutiStatus(id:String,status:String):Boolean=false
  suspend fun updateLemburStatus(id:String,status:String):Boolean=false
  suspend fun saveEmployee(employee:EmployeeEntity):Boolean=false
  suspend fun deleteEmployee(nik:String):Boolean=false
  suspend fun saveShift(shift:ShiftEntity):Boolean=false
  suspend fun deleteShift(shiftId:String):Boolean=false
  suspend fun generatePayroll(periode:String,targetNik:String=""):Boolean=false
  suspend fun markPayrollPaid(id:String):Boolean=false
  suspend fun addAnnouncement(judul:String,isi:String,kategori:String):AnnouncementEntity?=null

  private fun postJson(payload:JSONObject):JSONObject?{
    return try{
      val body=payload.toString().toRequestBody(jsonMediaType)
      val req=Request.Builder().url(BASE_URL).header("User-Agent","SuksesJaya-Android-Client/1.0").post(body).build()
      client.newCall(req).execute().use{r->if(!r.isSuccessful)null else JSONObject(r.body?.string()?:return null)}
    }catch(e:Exception){Log.e(TAG,"post",e);null}
  }

  private fun parseEmployees(a:JSONArray?):List<EmployeeEntity>{if(a==null)return emptyList();return (0 until a.length()).mapNotNull{a.optJSONObject(it)?.let(::parseSingleEmployee)}}
  private fun parseSingleEmployee(o:JSONObject)=EmployeeEntity(o.optStringOrFallback("nik","SJ001"),o.optStringOrFallback("nama","Karyawan"),o.optStringOrFallback("jabatan","Staff"),o.optStringOrFallback("role","USER"),o.optStringOrFallback("status","Aktif"),o.optDouble("latKantor",-8.1724),o.optDouble("longKantor",113.6995),o.optLong("limitKasbon",2500000),o.optStringOrFallback("noRekening",""),o.optLong("gajiPokok",4000000),o.optLong("tunjangan",800000),o.optStringOrFallback("email",""),o.optStringOrFallback("phone",""),o.optStringOrFallback("password",""),o.optStringOrFallback("shiftId","S1"),o.optStringOrFallback("fotoUrl",""),o.optStringOrFallback("noBpjsKes",""),o.optStringOrFallback("statusKes","Aktif"),o.optStringOrFallback("noBpjsTk",""),o.optStringOrFallback("statusTk","Aktif"),o.optStringOrFallback("faskes",""))
  private fun parseShifts(a:JSONArray?):List<ShiftEntity> = emptyList()
  private fun parseAttendances(a:JSONArray?):List<AttendanceEntity>{if(a==null)return emptyList();return (0 until a.length()).mapNotNull{a.optJSONObject(it)?.let(::parseSingleAttendance)}}
  private fun parseKasbons(a:JSONArray?):List<KasbonEntity> = emptyList()
  private fun parseCutis(a:JSONArray?):List<CutiEntity> = emptyList()
  private fun parseLemburs(a:JSONArray?):List<LemburEntity> = emptyList()
  private fun parsePayrolls(a:JSONArray?):List<PayrollEntity> = emptyList()
  private fun parseAnnouncements(a:JSONArray?):List<AnnouncementEntity> = emptyList()
  private fun parseSingleAttendance(o:JSONObject)=AttendanceEntity(o.optLong("id",0),o.optStringOrFallback("nik",""),o.optStringOrFallback("tanggal",""),o.optStringOrFallback("jamMasuk","").ifBlank{null},o.optStringOrFallback("jamPulang","").ifBlank{null},o.optStringOrFallback("status","Hadir"),o.optInt("jarakMeter",0),o.optNullableString("fotoMasukUrl"),o.optNullableString("fotoPulangUrl"),o.optDouble("lat",-8.1724),o.optDouble("lng",113.6995),o.optDouble("durasiJam",0.0))
  private fun JSONObject.optStringOrFallback(k:String,f:String)=if(!has(k)||isNull(k)||optString(k).isBlank())f else optString(k).trim()
  private fun JSONObject.optNullableString(k:String):String?=if(!has(k)||isNull(k)||optString(k).isBlank())null else optString(k).trim()
}
