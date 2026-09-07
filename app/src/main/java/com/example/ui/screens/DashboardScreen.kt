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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(viewModel: HrisViewModel, onOpenDrawer: () -> Unit) {
  val user by viewModel.currentUser.collectAsState()
  val gps by viewModel.gpsState.collectAsState()
  val attendances by viewModel.allAttendances.collectAsState()
  val cameraType by viewModel.cameraAttendanceType.collectAsState()
  val context = LocalContext.current
  val today = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
  val attendance = remember(attendances, user?.nik, today) {
    attendances.firstOrNull { it.nik == user?.nik && it.tanggal == today }
  }
  val hasIn = attendance?.jamMasuk != null
  val hasOut = attendance?.jamPulang != null

  LaunchedEffect(Unit) { viewModel.syncDatabase(false) }

  Box(Modifier.fillMaxSize()) {
    Column(
      modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp).systemBarsPadding(),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
          Text("SUKSES JAYA", fontWeight = FontWeight.Black, fontSize = 20.sp)
          Text("Selamat datang, ${user?.nama ?: "Karyawan"}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        IconButton(onClick = onOpenDrawer) { Icon(Icons.Default.Face, contentDescription = "Menu") }
      }

      AttendanceStatusCard(attendance)

      Button(
        onClick = { if (!hasIn) viewModel.openCameraAttendance("masuk") else if (!hasOut) viewModel.openCameraAttendance("pulang") },
        enabled = gps.isWithinGeofence && !hasOut,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = if (!hasIn) Color(0xFF16A34A) else Color(0xFFDC2626),
          disabledContainerColor = Color(0xFFCBD5E1)
        )
      ) {
        Icon(Icons.Default.CameraAlt, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text(
          when { !hasIn -> "ABSEN MASUK"; !hasOut -> "ABSEN PULANG"; else -> "ABSENSI HARI INI SELESAI" },
          fontWeight = FontWeight.Black,
          fontSize = 15.sp
        )
      }

      if (hasIn && !hasOut) {
        Text("Absen masuk ${attendance?.jamMasuk} sudah tercatat. Berikutnya hanya absen pulang.", fontSize = 12.sp, color = Color(0xFF166534), modifier = Modifier.fillMaxWidth())
      } else if (hasOut) {
        Text("Absen masuk dan pulang hari ini sudah lengkap. Tidak dapat melakukan double absen.", fontSize = 12.sp, color = Color(0xFF991B1B), modifier = Modifier.fillMaxWidth())
      }

      Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(14.dp)) {
          Text("Lokasi Absensi", fontWeight = FontWeight.Black, fontSize = 15.sp)
          Spacer(Modifier.height(6.dp))
          Text("Jarak ke kantor: ${gps.distanceToOfficeMeters} m", fontWeight = FontWeight.Bold)
          Text(
            if (gps.isWithinGeofence) "Lokasi valid · radius maksimal 100 m" else "Lokasi tidak valid · di luar radius 100 m",
            fontSize = 12.sp,
            color = if (gps.isWithinGeofence) Color(0xFF16A34A) else Color(0xFFDC2626)
          )
        }
      }

      Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(14.dp)) {
          Text("Menu Cepat", fontWeight = FontWeight.Black, fontSize = 15.sp)
          Spacer(Modifier.height(8.dp))
          Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QuickButton("Riwayat", Icons.Default.Face) { viewModel.selectPage(com.example.ui.viewmodel.AppPage.HISTORY) }
            QuickButton("Kalender", Icons.Default.Face) { viewModel.selectPage(com.example.ui.viewmodel.AppPage.CALENDAR) }
            QuickButton("Lembur", Icons.Default.Face) { viewModel.selectPage(com.example.ui.viewmodel.AppPage.REQUESTS) }
            QuickButton("Slip", Icons.Default.Face) { viewModel.selectPage(com.example.ui.viewmodel.AppPage.PAYSLIP) }
          }
        }
      }
    }

    if (cameraType != null && user != null) {
      CameraAttendanceDialog(
        type = cameraType!!,
        name = user!!.nama,
        nik = user!!.nik,
        gps = gps,
        viewModel = viewModel,
        onDismiss = { viewModel.closeCameraAttendance() }
      )
    }
  }
}

@Composable
private fun AttendanceStatusCard(attendance: AttendanceEntity?) {
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

@Composable
private fun AttendanceTile(label: String, time: String, color: Color, done: Boolean, modifier: Modifier) {
  Card(modifier, shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = if (done) color.copy(alpha = .10f) else Color(0xFFF8FAFC))) {
    Column(Modifier.padding(14.dp)) {
      Text(label, color = color, fontWeight = FontWeight.Bold, fontSize = 10.sp)
      Text(time, fontWeight = FontWeight.Black, fontSize = 21.sp)
      Text(if (done) "Tercatat" else "Belum", fontSize = 11.sp)
    }
  }
}

@Composable
private fun RowScope.QuickButton(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
  OutlinedButton(onClick = onClick, modifier = Modifier.weight(1f).height(64.dp), contentPadding = PaddingValues(2.dp), shape = RoundedCornerShape(12.dp)) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Icon(icon, contentDescription = title, modifier = Modifier.size(18.dp))
      Text(title, fontSize = 9.sp)
    }
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
    text = {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
          Modifier.fillMaxWidth().height(210.dp).clip(RoundedCornerShape(18.dp)).background(Color(0xFF0F172A)).border(2.dp, if (type == "masuk") Color(0xFF16A34A) else Color(0xFFDC2626), RoundedCornerShape(18.dp)),
          contentAlignment = Alignment.Center
        ) {
          if (bitmap != null) Image(bitmap!!.asImageBitmap(), contentDescription = "Foto presensi", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
          else Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.Face, contentDescription = null, tint = Color.White, modifier = Modifier.size(52.dp)); Text("Ambil foto selfie", color = Color.White) }
        }
        Spacer(Modifier.height(8.dp))
        Text("$name · $nik", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Text("GPS ${gps.distanceToOfficeMeters} m · radius maksimal 100 m", fontSize = 11.sp)
        Spacer(Modifier.height(8.dp))
        Button(
          onClick = {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) cameraLauncher.launch(null)
            else permissionLauncher.launch(Manifest.permission.CAMERA)
          },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp)
        ) {
          Icon(Icons.Default.CameraAlt, contentDescription = null)
          Spacer(Modifier.width(6.dp))
          Text("Buka Kamera")
        }
      }
    },
    confirmButton = {
      Button(
        enabled = bitmap != null,
        onClick = {
          val file = File(context.cacheDir, "absen_${type}_${System.currentTimeMillis()}.jpg")
          FileOutputStream(file).use { bitmap!!.compress(Bitmap.CompressFormat.JPEG, 88, it) }
          viewModel.performAttendance(type, file.absolutePath)
        },
        colors = ButtonDefaults.buttonColors(containerColor = if (type == "masuk") Color(0xFF16A34A) else Color(0xFFDC2626))
      ) { Text(if (type == "masuk") "Konfirmasi Masuk" else "Konfirmasi Pulang") }
    },
    dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
  )
}
