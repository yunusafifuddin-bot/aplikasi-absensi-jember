package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.StatusBadge
import com.example.ui.viewmodel.HrisViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AdminDashboardScreen(viewModel: HrisViewModel, modifier: Modifier = Modifier) {
  val employees by viewModel.allEmployees.collectAsState()
  val attendances by viewModel.allAttendances.collectAsState()
  val kasbons by viewModel.allKasbons.collectAsState()
  val lemburs by viewModel.allLemburs.collectAsState()
  val today = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
  val todayRows = remember(attendances, today) { attendances.filter { it.tanggal == today } }
  val pendingKasbon = kasbons.filter { it.statusPersetujuan == "Pending" }
  val pendingLembur = lemburs.filter { it.statusPersetujuan == "Pending" }

  LazyColumn(modifier.fillMaxSize().background(Color(0xFFF8FAFC)).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
    item {
      Text("HR Dashboard", fontWeight = FontWeight.Black, fontSize = 22.sp)
      Text("Monitoring absensi dan operasional", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    item {
      Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Stat("Karyawan", employees.size.toString(), Modifier.weight(1f))
        Stat("Hadir", todayRows.size.toString(), Modifier.weight(1f))
        Stat("Pending", (pendingKasbon.size + pendingLembur.size).toString(), Modifier.weight(1f))
      }
    }
    item { Text("Persetujuan", fontWeight = FontWeight.Black, fontSize = 17.sp) }
    if (pendingKasbon.isNotEmpty()) {
      items(pendingKasbon, key = { "kb-${it.idKasbon}" }) { item ->
        ApprovalCard("KASBON", "${item.nama} · ${item.nik}", item.keterangan, { viewModel.approveApproval("kasbon", item.idKasbon) }, { viewModel.rejectApproval("kasbon", item.idKasbon) })
      }
    }
    if (pendingLembur.isNotEmpty()) {
      items(pendingLembur, key = { "ot-${it.idLembur}" }) { item ->
        ApprovalCard("LEMBUR", "${item.nama} · ${item.tanggal}", item.deskripsi, { viewModel.approveApproval("lembur", item.idLembur) }, { viewModel.rejectApproval("lembur", item.idLembur) })
      }
    }
    if (pendingKasbon.isEmpty() && pendingLembur.isEmpty()) item { Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) { Text("Tidak ada persetujuan pending.", Modifier.padding(20.dp)) } }
  }
}

@Composable
private fun Stat(title: String, value: String, modifier: Modifier) {
  Card(modifier, shape = RoundedCornerShape(16.dp)) { Column(Modifier.padding(14.dp)) { Text(title, fontSize = 11.sp); Text(value, fontSize = 22.sp, fontWeight = FontWeight.Black) } }
}

@Composable
private fun ApprovalCard(type: String, title: String, detail: String, approve: () -> Unit, reject: () -> Unit) {
  Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
    Column(Modifier.padding(14.dp)) {
      Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(title, fontWeight = FontWeight.Bold); StatusBadge(type) }
      Spacer(Modifier.height(6.dp)); Text(detail, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
      Spacer(Modifier.height(10.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        OutlinedButton(onClick = reject) { Text("Tolak") }
        Spacer(Modifier.width(8.dp)); Button(onClick = approve) { Text("Setujui") }
      }
    }
  }
}
