package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AttendanceEntity
import com.example.ui.components.StatusBadge
import com.example.ui.theme.SjPrimary
import com.example.ui.viewmodel.HrisViewModel

@Composable
fun AttendanceHistoryScreen(
  viewModel: HrisViewModel,
  modifier: Modifier = Modifier
) {
  val user by viewModel.currentUser.collectAsState()
  val attendances by viewModel.allAttendances.collectAsState()
  val selectedMonth by viewModel.selectedMonth.collectAsState()

  var previewAttendance by remember { mutableStateOf<AttendanceEntity?>(null) }

  val filteredAttendances = remember(attendances, user, selectedMonth) {
    attendances.filter { it.nik == user?.nik && it.tanggal.startsWith(selectedMonth) }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(16.dp)
  ) {
    // Header & Month Selector
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "Riwayat Absensi",
          fontWeight = FontWeight.Black,
          fontSize = 22.sp
        )
        Text(
          text = "Rekap kehadiran bulanan karyawan",
          fontSize = 12.5.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      // Quick month switcher chip
      Row(verticalAlignment = Alignment.CenterVertically) {
        FilterChip(
          selected = selectedMonth == "2026-09",
          onClick = { viewModel.setSelectedMonth("2026-09") },
          label = { Text("Sep 2026", fontSize = 11.5.sp) }
        )
        Spacer(modifier = Modifier.width(4.dp))
        FilterChip(
          selected = selectedMonth == "2026-08",
          onClick = { viewModel.setSelectedMonth("2026-08") },
          label = { Text("Agt 2026", fontSize = 11.5.sp) }
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Monthly summary Kehadiran card with gradient
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = Color.Transparent),
      border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
      elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .background(com.example.ui.theme.BentoKehadiranGradient)
          .padding(18.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceAround
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("TOTAL MASUK", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD1FAE5), letterSpacing = 0.8.sp)
            Spacer(modifier = Modifier.height(3.dp))
            Text("${filteredAttendances.size} Hari", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color.White)
          }
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("TEPAT WAKTU", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD1FAE5), letterSpacing = 0.8.sp)
            Spacer(modifier = Modifier.height(3.dp))
            Text(
              "${filteredAttendances.count { it.status.equals("tepat waktu", true) }}",
              fontWeight = FontWeight.Black,
              fontSize = 18.sp,
              color = Color.White
            )
          }
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("TERLAMBAT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD1FAE5), letterSpacing = 0.8.sp)
            Spacer(modifier = Modifier.height(3.dp))
            Text(
              "${filteredAttendances.count { it.status.equals("terlambat", true) }}",
              fontWeight = FontWeight.Black,
              fontSize = 18.sp,
              color = Color(0xFFFDE68A)
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    if (filteredAttendances.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(Icons.Default.EventBusy, contentDescription = null, modifier = Modifier.size(48.dp), tint = com.example.ui.theme.BentoSlate400)
          Spacer(modifier = Modifier.height(8.dp))
          Text("Belum ada data presensi pada periode ini.", color = com.example.ui.theme.BentoSlate400)
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        items(filteredAttendances, key = { it.id }) { item ->
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.BentoCardWhite),
            border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.BentoBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = item.tanggal,
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                  text = "Masuk: ${item.jamMasuk ?: "-"}  •  Pulang: ${item.jamPulang ?: "-"}",
                  fontSize = 12.5.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  text = "Radius GPS: ${item.jarakMeter}m  •  Durasi: ${item.durasiJam} jam",
                  fontSize = 11.5.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }

              Column(horizontalAlignment = Alignment.End) {
                StatusBadge(status = item.status)
                Spacer(modifier = Modifier.height(6.dp))
                IconButton(
                  onClick = { previewAttendance = item },
                  modifier = Modifier.size(32.dp)
                ) {
                  Icon(Icons.Default.PhotoCamera, contentDescription = "Lihat Selfie", tint = SjPrimary, modifier = Modifier.size(20.dp))
                }
              }
            }
          }
        }
      }
    }
  }

  // Preview Dialog for Selfie Proof
  if (previewAttendance != null) {
    val att = previewAttendance!!
    AlertDialog(
      onDismissRequest = { previewAttendance = null },
      title = { Text("Verifikasi Presensi Selfie") },
      text = {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
          Box(
            modifier = Modifier
              .size(160.dp)
              .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(96.dp), tint = SjPrimary)
          }
          Spacer(modifier = Modifier.height(14.dp))
          Text("Tanggal: ${att.tanggal}", fontWeight = FontWeight.Bold)
          Text("Jam Masuk: ${att.jamMasuk ?: "-"} (Toleransi OK)")
          Text("Jarak Lokasi GPS: ${att.jarakMeter} meter dari kantor")
          Text("Status: ${att.status}", color = SjPrimary, fontWeight = FontWeight.Bold)
        }
      },
      confirmButton = {
        Button(onClick = { previewAttendance = null }) {
          Text("Tutup")
        }
      }
    )
  }
}
