package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.components.AvatarCircle
import com.example.ui.components.StatusBadge
import com.example.ui.viewmodel.AppPage
import com.example.ui.viewmodel.GpsLocationState
import com.example.ui.viewmodel.HrisViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(viewModel: HrisViewModel, onOpenDrawer: (() -> Unit)? = null, modifier: Modifier = Modifier) {
  val user by viewModel.currentUser.collectAsState()
  val attendances by viewModel.allAttendances.collectAsState()
  val gps by viewModel.gpsState.collectAsState()
  val cameraType by viewModel.cameraAttendanceType.collectAsState()
  val context = LocalContext.current
  val today = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
  val todayAttendance = attendances.firstOrNull { it.nik == user?.nik && it.tanggal == today }
  val hasIn = todayAttendance?.jamMasuk != null
  val hasOut = todayAttendance?.jamPulang != null

  val locationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
    if (result[Manifest.permission.ACCESS_FINE_LOCATION] == true || result[Manifest.permission.ACCESS_COARSE_LOCATION] == true) updateCurrentLocation(context, viewModel)
  }
  fun requestOrRefreshLocation() {
    val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    if (fine || coarse) updateCurrentLocation(context, viewModel)
    else locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
  }

  LaunchedEffect(Unit) { requestOrRefreshLocation() }

  Box(modifier.fillMaxSize().background(Color(0xFFF1F5F9))) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
      Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        AvatarCircle(user?.nama ?: "SJ", size = 46.dp); Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) { Text("Selamat datang", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(user?.nama ?: "Karyawan", fontWeight = FontWeight.Black, fontSize = 19.sp, maxLines = 1); Text(user?.jabatan ?: "", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        IconButton(onClick = { onOpenDrawer?.invoke() }) { Icon(Icons.Default.Menu, "Menu") }
      }

      Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(16.dp)) {
          Text("Presensi Hari Ini", fontWeight = FontWeight.Black, fontSize = 18.sp); Spacer(Modifier.height(10.dp))
          Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AttendanceTile("MASUK", todayAttendance?.jamMasuk ?: "--:--", Color(0xFF16A34A), hasIn, Modifier.weight(1f))
            AttendanceTile("PULANG", todayAttendance?.jamPulang ?: "--:--", Color(0xFFDC2626), hasOut, Modifier.weight(1f))
          }
          Spacer(Modifier.height(12.dp))
          Surface(Modifier.fillMaxWidth(), color = if (gps.isWithinGeofence) Color(0xFFECFDF5) else Color(0xFFFEF2F2), shape = RoundedCornerShape(14.dp)) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.LocationOn, null, tint = if (gps.isWithinGeofence) Color(0xFF15803D) else Color(0xFFB91C1C)); Spacer(Modifier.width(8.dp))
              Column(Modifier.weight(1f)) { Text(if (gps.isWithinGeofence) "Lokasi valid" else "Lokasi tidak valid", fontWeight = FontWeight.Bold, fontSize = 13.sp); Text("${gps.distanceToOfficeMeters} m / 100 m · ±${gps.accuracyMeters.toInt()} m", fontSize = 11.sp) }
              TextButton(onClick = { requestOrRefreshLocation() }) { Text("Refresh") }
            }
          }
        }
      }

      Button(
        onClick = { if (!hasIn) { requestOrRefreshLocation(); viewModel.openCameraAttendance("masuk") } else if (!hasOut) { requestOrRefreshLocation(); viewModel.openCameraAttendance("pulang") } },
        enabled = gps.isWithinGeofence && !hasOut,
        modifier = Modifier.fillMaxWidth().height(58.dp), shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = if (!hasIn) Color(0xFF16A34A) else Color(0xFFDC2626), disabledContainerColor = Color(0xFFCBD5E1))
      ) { Icon(Icons.Default.CameraAlt, null); Spacer(Modifier.width(8.dp)); Text(when { !hasIn -> "ABSEN MASUK"; !hasOut -> "ABSEN PULANG"; else -> "ABSENSI SELESAI" }, fontWeight = FontWeight.Black, fontSize = 15.sp) }

      if (hasIn && !hasOut) Text("Absen masuk ${todayAttendance?.jamMasuk} sudah tercatat. Berikutnya hanya absen pulang.", fontSize = 12.sp, color = Color(0xFF166534), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
      if (hasOut) Text("Absen masuk dan pulang hari ini sudah lengkap. Tidak dapat melakukan double absen.", fontSize = 12.sp, color = Color(0xFF991B1B), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())

      Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(16.dp)) {
          Text("Menu Cepat", fontWeight = FontWeight.Black, fontSize = 16.sp); Spacer(Modifier.height(10.dp))
          Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QuickButton("Riwayat", Icons.Default.History) { viewModel.selectPage(AppPage.HISTORY) }
            QuickButton("Kalender", Icons.Default.CalendarMonth) { viewModel.selectPage(AppPage.CALENDAR) }
            QuickButton("Lembur", Icons.Default.Timelapse) { viewModel.selectPage(AppPage.REQUESTS) }
            QuickButton("Slip", Icons.Default.ReceiptLong) { viewModel.selectPage(AppPage.PAYSLIP) }
          }
        }
      }

      Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(16.dp)) {
          Text("Sumber Data", fontWeight = FontWeight.Black, fontSize = 16.sp); Spacer(Modifier.height(6.dp)
          Text("Google Sheets / Google Apps Script", fontWeight = FontWeight.Bold, fontSize = 13.sp)
          Text("Data lokal tidak digunakan sebagai sumber kebenaran. Cache lokal hanya diperbarui setelah sinkronisasi remote.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
      Spacer(Modifier.height(16.dp))
    }
    if (cameraType != null) CameraAttendanceDialog(cameraType!!, user?.nama ?: "Karyawan", user?.nik ?: "", gps, viewModel, onDismiss = { viewModel.closeCameraAttendance() })
  }
}

@Composable private fun AttendanceTile(label: String, time: String, color: Color, done: Boolean, modifier: Modifier) { Card(modifier, shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = if (done) color.copy(alpha = .10f) else Color(0xFFF8FAFC))) { Column(Modifier.padding(14.dp)) { Text(label, color = color, fontWeight = FontWeight.Bold, fontSize = 10.sp); Text(time, fontWeight = FontWeight.Black, fontSize = 21.sp); Text(if (done) "Tercatat" else "Belum", fontSize = 11.sp) } } }

@Composable private fun RowScope.QuickButton(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) { OutlinedButton(onClick, Modifier.weight(1f).height(68.dp), contentPadding = PaddingValues(2.dp), shape = RoundedCornerShape(12.dp)) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(icon, null, Modifier.size(19.dp)); Text(title, fontSize = 9.sp) } } }

@Composable
private fun CameraAttendanceDialog(type: String, name: String, nik: String, gps: GpsLocationState, viewModel: HrisViewModel, onDismiss: () -> Unit) {
  val context = LocalContext.current
  var bitmap by remember { mutableStateOf<Bitmap?>(null) }
  val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { result -> if (result != null) bitmap = result }
  val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted -> if (granted) cameraLauncher.launch(null) }
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(if (type == "masuk") "Foto Absen Masuk" else "Foto Absen Pulang", fontWeight = FontWeight.Black) },
    text = {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(18.dp)).background(Color(0xFF0F172A)).border(2.dp, if (type == "masuk") Color(0xFF16A34A) else Color(0xFFDC2626), RoundedCornerShape(18.dp)), contentAlignment = Alignment.Center) {
          if (bitmap != null) Image(bitmap!!.asImageBitmap(), "Foto presensi", Modifier.fillMaxSize(), contentScale = ContentScale.Crop) else Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.Face, null, tint = Color.White, Modifier.size(52.dp)); Text("Ambil foto selfie", color = Color.White) }
        }
        Spacer(Modifier.height(8.dp)); Text("$name · $nik", fontWeight = FontWeight.Bold, fontSize = 12.sp); Text("GPS ${gps.distanceToOfficeMeters} m · radius maksimal 100 m", fontSize = 11.sp); Spacer(Modifier.height(8.dp))
        Button(onClick = { if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) cameraLauncher.launch(null) else permissionLauncher.launch(Manifest.permission.CAMERA) }, Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) { Icon(Icons.Default.CameraAlt, null); Spacer(Modifier.width(6.dp)); Text("Buka Kamera") }
      }
    },
    confirmButton = { Button(enabled = bitmap != null, onClick = { val file = File(context.cacheDir, "absen_${type}_${System.currentTimeMillis()}.jpg"); FileOutputStream(file).use { bitmap!!.compress(Bitmap.CompressFormat.JPEG, 88, it) }; viewModel.performAttendance(type, file.absolutePath) }, colors = ButtonDefaults.buttonColors(containerColor = if (type == "masuk") Color(0xFF16A34A) else Color(0xFFDC2626))) { Text(if (type == "masuk") "Konfirmasi Masuk" else "Konfirmasi Pulang") } },
    dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
  )
}

private fun updateCurrentLocation(context: Context, viewModel: HrisViewModel) {
  val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
  val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
  if (!fine && !coarse) return
  LocationServices.getFusedLocationProviderClient(context).getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token).addOnSuccessListener { location -> if (location != null) viewModel.setGpsLocation(location.latitude, location.longitude, location.accuracy) }
}
