package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Face
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.local.AttendanceEntity
import com.example.ui.viewmodel.GpsLocationState
import com.example.ui.viewmodel.HrisViewModel
import java.io.File
import java.io.FileOutputStream

@Composable
fun DashboardScreen(viewModel: HrisViewModel, onOpenDrawer: () -> Unit) {
  val user by viewModel.currentUser.collectAsState()
  val gps by viewModel.gpsState.collectAsState()
  val attendance by viewModel.todayAttendance.collectAsState()
  val cameraType by viewModel.cameraAttendanceType.collectAsState()
  LaunchedEffect(Unit) { viewModel.syncDatabase(false) }

  Box(Modifier.fillMaxSize()) {
    Column(Modifier.fillMaxSize().padding(16.dp).systemBarsPadding()) {
      Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
          Text("SUKSES JAYA", fontWeight = FontWeight.Black, fontSize = 20.sp)
          Text("Selamat datang, ${user?.nama ?: "Karyawan"}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        IconButton(onClick = onOpenDrawer) { Icon(Icons.Default.Face, contentDescription = "Menu") }
      }
      Spacer(Modifier.height(16.dp))
      AttendanceStatusCard(attendance, gps)
      Spacer(Modifier.height(16.dp))
      Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        QuickButton("Absen Masuk", Icons.Default.Face) { viewModel.openCameraAttendance("masuk") }
        QuickButton("Absen Pulang", Icons.Default.Face) { viewModel.openCameraAttendance("pulang") }
      }
      Spacer(Modifier.height(12.dp))
      Text("Lokasi", fontWeight = FontWeight.Bold, fontSize = 14.sp)
      Spacer(Modifier.height(6.dp))
      Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
        Column(Modifier.padding(14.dp)) {
          Text("Jarak ke kantor: ${gps.distanceToOfficeMeters} m", fontWeight = FontWeight.Bold)
          Text(if (gps.isWithinGeofence) "Dalam radius absensi (maks. 100 m)" else "Di luar radius absensi", fontSize = 12.sp, color = if (gps.isWithinGeofence) Color(0xFF16A34A) else Color(0xFFDC2626))
        }
      }
    }
    if (cameraType != null && user != null) CameraAttendanceDialog(cameraType!!, user!!.nama, user!!.nik, gps, viewModel) { viewModel.closeCameraAttendance() }
  }
}

@Composable
private fun AttendanceStatusCard(attendance: AttendanceEntity?, gps: GpsLocationState) {
  Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
    Column(Modifier.padding(16.dp)) {
      Text("ABSENSI HARI INI", fontWeight = FontWeight.Black, fontSize = 12.sp)
      Spacer(Modifier.height(10.dp))
      Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        AttendanceTile("MASUK", attendance?.jamMasuk ?: "--:--", Color(0xFF16A34A), attendance?.jamMasuk != null, Modifier.weight(1f))
        AttendanceTile("PULANG", attendance?.jamPulang ?: "--:--", Color(0xFFDC2626), attendance?.jamPulang != null, Modifier.weight(1f))
      }
    }
  }
}

@Composable private fun AttendanceTile(label: String, time: String, color: Color, done: Boolean, modifier: Modifier) {
  Card(modifier, shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = if (done) color.copy(alpha = .10f) else Color(0xFFF8FAFC))) {
    Column(Modifier.padding(14.dp)) { Text(label, color = color, fontWeight = FontWeight.Bold, fontSize = 10.sp); Text(time, fontWeight = FontWeight.Black, fontSize = 21.sp); Text(if (done) "Tercatat" else "Belum", fontSize = 11.sp) }
  }
}

@Composable private fun RowScope.QuickButton(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
  OutlinedButton(onClick, Modifier.weight(1f).height(68.dp), contentPadding = PaddingValues(2.dp), shape = RoundedCornerShape(12.dp)) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(icon, contentDescription = title, modifier = Modifier.size(19.dp)); Text(title, fontSize = 9.sp) }
  }
}

@Composable
private fun CameraAttendanceDialog(type: String, name: String, nik: String, gps: GpsLocationState, viewModel: HrisViewModel, onDismiss: () -> Unit) {
  val context = LocalContext.current
  var bitmap by remember { mutableStateOf<Bitmap?>(null) }
  val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { result -> if (result != null) bitmap = result }
  val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted -> if (granted) cameraLauncher.launch(null) }
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(if (type == "masuk") "Foto Absen Masuk" else "Foto Absen Pulang", fontWeight = FontWeight.Black) },
    text = { Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Box(Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(18.dp)).background(Color(0xFF0F172A)).border(2.dp, if (type == "masuk") Color(0xFF16A34A) else Color(0xFFDC2626), RoundedCornerShape(18.dp)), contentAlignment = Alignment.Center) {
        if (bitmap != null) Image(bitmap!!.asImageBitmap(), contentDescription = "Foto presensi", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        else Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.Face, contentDescription = null, tint = Color.White, modifier = Modifier.size(52.dp)); Text("Ambil foto selfie", color = Color.White) }
      }
      Spacer(Modifier.height(8.dp)); Text("$name · $nik", fontWeight = FontWeight.Bold, fontSize = 12.sp); Text("GPS ${gps.distanceToOfficeMeters} m · radius maksimal 100 m", fontSize = 11.sp); Spacer(Modifier.height(8.dp))
      Button(onClick = { if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) cameraLauncher.launch(null) else permissionLauncher.launch(Manifest.permission.CAMERA) }, Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) { Icon(Icons.Default.CameraAlt, contentDescription = null); Spacer(Modifier.width(6.dp)); Text("Buka Kamera") }
    } },
    confirmButton = { Button(enabled = bitmap != null, onClick = { val file = File(context.cacheDir, "absen_${type}_${System.currentTimeMillis()}.jpg"); FileOutputStream(file).use { bitmap!!.compress(Bitmap.CompressFormat.JPEG, 88, it) }; viewModel.performAttendance(type, file.absolutePath) }, colors = ButtonDefaults.buttonColors(containerColor = if (type == "masuk") Color(0xFF16A34A) else Color(0xFFDC2626))) { Text(if (type == "masuk") "Konfirmasi Masuk" else "Konfirmasi Pulang") } },
    dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
  )
}
