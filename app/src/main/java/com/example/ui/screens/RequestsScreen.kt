package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timelapse
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
fun RequestsScreen(viewModel: HrisViewModel, modifier: Modifier = Modifier) {
  val user by viewModel.currentUser.collectAsState()
  val lemburs by viewModel.allLemburs.collectAsState()
  val userLemburs = remember(lemburs, user) { lemburs.filter { it.nik == user?.nik } }
  var showDialog by remember { mutableStateOf(false) }
  var date by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
  var start by remember { mutableStateOf("17:30") }
  var end by remember { mutableStateOf("20:30") }
  var desc by remember { mutableStateOf("") }

  Column(modifier.fillMaxSize().background(Color(0xFFF8FAFC)).padding(16.dp)) {
    Text("Lembur", fontWeight = FontWeight.Black, fontSize = 22.sp)
    Text("Pengajuan dan status lembur", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
    Spacer(Modifier.height(14.dp))
    Button(onClick = { showDialog = true }, shape = RoundedCornerShape(12.dp)) {
      Icon(Icons.Default.Timelapse, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Ajukan Lembur")
    }
    Spacer(Modifier.height(14.dp))
    if (userLemburs.isEmpty()) {
      Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) { Text("Belum ada riwayat lembur.", Modifier.padding(20.dp)) }
    } else {
      LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(userLemburs, key = { it.idLembur }) { item ->
          Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Column(Modifier.padding(16.dp)) {
              Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(item.tanggal, fontWeight = FontWeight.Bold); StatusBadge(item.statusPersetujuan)
              }
              Spacer(Modifier.height(6.dp))
              Text("${item.jamMulai} – ${item.jamSelesai} · ${item.durasiJam} jam", fontSize = 13.sp)
              Text(item.deskripsi, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
            }
          }
        }
      }
    }
  }

  if (showDialog) {
    AlertDialog(
      onDismissRequest = { showDialog = false },
      title = { Text("Ajukan Lembur", fontWeight = FontWeight.Bold) },
      text = {
        Column {
          OutlinedTextField(date, { date = it }, label = { Text("Tanggal") }, singleLine = true)
          Spacer(Modifier.height(8.dp))
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(start, { start = it }, label = { Text("Mulai") }, modifier = Modifier.weight(1f), singleLine = true)
            OutlinedTextField(end, { end = it }, label = { Text("Selesai") }, modifier = Modifier.weight(1f), singleLine = true)
          }
          Spacer(Modifier.height(8.dp))
          OutlinedTextField(desc, { desc = it }, label = { Text("Pekerjaan") }, modifier = Modifier.fillMaxWidth(), maxLines = 3)
        }
      },
      confirmButton = {
        Button(onClick = { if (desc.isNotBlank()) { viewModel.submitLembur(date, start, end, desc); desc = ""; showDialog = false } }) { Text("Kirim") }
      },
      dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Batal") } }
    )
  }
}
