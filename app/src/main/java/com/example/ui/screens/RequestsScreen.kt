package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.StatusBadge
import com.example.ui.theme.SjPrimary
import com.example.ui.viewmodel.HrisViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestsScreen(
  viewModel: HrisViewModel,
  modifier: Modifier = Modifier
) {
  val user by viewModel.currentUser.collectAsState()
  val cutis by viewModel.allCutis.collectAsState()
  val lemburs by viewModel.allLemburs.collectAsState()

  val userCutis = remember(cutis, user) { cutis.filter { it.nik == user?.nik } }
  val userLemburs = remember(lemburs, user) { lemburs.filter { it.nik == user?.nik } }

  var selectedTab by remember { mutableIntStateOf(0) } // 0 = Cuti, 1 = Lembur
  var showCutiDialog by remember { mutableStateOf(false) }
  var showLemburDialog by remember { mutableStateOf(false) }

  // Cuti Form State
  var cutiJenis by remember { mutableStateOf("Cuti Tahunan") }
  var cutiStart by remember {
    mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
  }
  var cutiEnd by remember {
    mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
  }
  var cutiReason by remember { mutableStateOf("") }

  // Lembur Form State
  var otDate by remember {
    mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
  }
  var otStart by remember { mutableStateOf("17:30") }
  var otEnd by remember { mutableStateOf("20:30") }
  var otDesc by remember { mutableStateOf("") }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(16.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "Cuti & Lembur",
          fontWeight = FontWeight.Black,
          fontSize = 22.sp
        )
        Text(
          text = "Pengajuan dan status persetujuan",
          fontSize = 12.5.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(
          onClick = { showCutiDialog = true },
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.testTag("btn_ajukan_cuti")
        ) {
          Icon(Icons.Default.BeachAccess, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Cuti")
        }

        Button(
          onClick = { showLemburDialog = true },
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(containerColor = SjPrimary),
          modifier = Modifier.testTag("btn_ajukan_lembur")
        ) {
          Icon(Icons.Default.Timelapse, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Lembur")
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Tab Switcher
    TabRow(
      selectedTabIndex = selectedTab,
      containerColor = MaterialTheme.colorScheme.surface
    ) {
      Tab(
        selected = selectedTab == 0,
        onClick = { selectedTab = 0 },
        text = { Text("Cuti / Izin (${userCutis.size})", fontWeight = FontWeight.Bold) }
      )
      Tab(
        selected = selectedTab == 1,
        onClick = { selectedTab = 1 },
        text = { Text("Lembur (${userLemburs.size})", fontWeight = FontWeight.Bold) }
      )
    }

    Spacer(modifier = Modifier.height(14.dp))

    if (selectedTab == 0) {
      // Gradient Sisa Cuti Feature Card
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
            .background(com.example.ui.theme.BentoSisaCutiGradient)
            .padding(18.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "SISA HAK CUTI TAHUNAN",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFEF3C7),
                letterSpacing = 0.8.sp
              )
              Spacer(modifier = Modifier.height(3.dp))
              Text(
                text = "12 Hari Tersedia",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
              )
              Text(
                text = "Kuota 12 hari/tahun · Cuti disetujui: ${userCutis.count { it.status.equals("disetujui", true) }} hari",
                fontSize = 11.5.sp,
                color = Color.White.copy(alpha = 0.88f)
              )
            }
            Box(
              modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.22f))
                .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(16.dp)),
              contentAlignment = Alignment.Center
            ) {
              Text(text = "🏖️", fontSize = 22.sp)
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Cuti List
      if (userCutis.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          Text("Belum ada riwayat pengajuan cuti.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          items(userCutis, key = { it.idCuti }) { item ->
            Card(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(22.dp),
              colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.BentoCardWhite),
              border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.BentoBorder),
              elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
              Column(modifier = Modifier.padding(16.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(text = item.jenisCuti, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = com.example.ui.theme.BentoSlate800)
                  StatusBadge(status = item.status)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "Periode: ${item.tglMulai} s/d ${item.tglSelesai}",
                  fontSize = 12.sp,
                  color = com.example.ui.theme.BentoSlate400
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = "Alasan: ${item.alasan}",
                  fontSize = 13.sp,
                  color = com.example.ui.theme.BentoSlate600
                )
              }
            }
          }
        }
      }
    } else {
      // Lembur List
      if (userLemburs.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          Text("Belum ada riwayat pengajuan lembur.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          items(userLemburs, key = { it.idLembur }) { item ->
            Card(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(22.dp),
              colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.BentoCardWhite),
              border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.BentoBorder),
              elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
              Column(modifier = Modifier.padding(16.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(text = "Lembur: ${item.tanggal}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = com.example.ui.theme.BentoSlate800)
                  StatusBadge(status = item.statusPersetujuan)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "Jam: ${item.jamMulai} – ${item.jamSelesai} (${item.durasiJam} Jam)",
                  fontSize = 12.sp,
                  color = com.example.ui.theme.BentoSlate400
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = "Tugas: ${item.deskripsi}",
                  fontSize = 13.sp,
                  color = com.example.ui.theme.BentoSlate600
                )
              }
            }
          }
        }
      }
    }
  }

  // Dialog Cuti
  if (showCutiDialog) {
    AlertDialog(
      onDismissRequest = { showCutiDialog = false },
      title = { Text("Pengajuan Cuti / Izin", fontWeight = FontWeight.Bold) },
      text = {
        Column {
          val types = listOf("Cuti Tahunan", "Izin", "Sakit", "Cuti Khusus")
          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            types.take(2).forEach { t ->
              FilterChip(
                selected = cutiJenis == t,
                onClick = { cutiJenis = t },
                label = { Text(t, fontSize = 11.sp) }
              )
            }
          }
          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            types.drop(2).forEach { t ->
              FilterChip(
                selected = cutiJenis == t,
                onClick = { cutiJenis = t },
                label = { Text(t, fontSize = 11.sp) }
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))
          OutlinedTextField(
            value = cutiStart,
            onValueChange = { cutiStart = it },
            label = { Text("Tanggal Mulai (YYYY-MM-DD)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = cutiEnd,
            onValueChange = { cutiEnd = it },
            label = { Text("Tanggal Selesai (YYYY-MM-DD)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = cutiReason,
            onValueChange = { cutiReason = it },
            label = { Text("Alasan Cuti / Keterangan") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (cutiReason.isBlank()) {
              viewModel.emitMessage("Mohon isi alasan cuti.")
              return@Button
            }
            viewModel.submitCuti(cutiJenis, cutiStart, cutiEnd, cutiReason)
            showCutiDialog = false
            cutiReason = ""
          }
        ) {
          Text("Kirim Pengajuan")
        }
      },
      dismissButton = {
        TextButton(onClick = { showCutiDialog = false }) {
          Text("Batal")
        }
      }
    )
  }

  // Dialog Lembur
  if (showLemburDialog) {
    AlertDialog(
      onDismissRequest = { showLemburDialog = false },
      title = { Text("Pengajuan Lembur", fontWeight = FontWeight.Bold) },
      text = {
        Column {
          OutlinedTextField(
            value = otDate,
            onValueChange = { otDate = it },
            label = { Text("Tanggal (YYYY-MM-DD)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(8.dp))
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
              value = otStart,
              onValueChange = { otStart = it },
              label = { Text("Mulai (HH:mm)") },
              singleLine = true,
              modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
              value = otEnd,
              onValueChange = { otEnd = it },
              label = { Text("Selesai (HH:mm)") },
              singleLine = true,
              modifier = Modifier.weight(1f)
            )
          }
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = otDesc,
            onValueChange = { otDesc = it },
            label = { Text("Deskripsi Pekerjaan Lembur") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (otDesc.isBlank()) {
              viewModel.emitMessage("Mohon isi deskripsi pekerjaan lembur.")
              return@Button
            }
            viewModel.submitLembur(otDate, otStart, otEnd, otDesc)
            showLemburDialog = false
            otDesc = ""
          }
        ) {
          Text("Kirim Pengajuan")
        }
      },
      dismissButton = {
        TextButton(onClick = { showLemburDialog = false }) {
          Text("Batal")
        }
      }
    )
  }
}
