package com.example.ui.screens

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.StatusBadge
import com.example.ui.theme.SjPrimary
import com.example.ui.viewmodel.HrisViewModel

@Composable
fun AnnouncementsScreen(
  viewModel: HrisViewModel,
  modifier: Modifier = Modifier
) {
  val announcements by viewModel.allAnnouncements.collectAsState()
  val user by viewModel.currentUser.collectAsState()

  val isHrOrAdmin = user?.role in listOf("HR", "ADMIN")
  var showDialog by remember { mutableStateOf(false) }

  var judulInput by remember { mutableStateOf("") }
  var isiInput by remember { mutableStateOf("") }
  var kategoriInput by remember { mutableStateOf("Info") }

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
          text = "Pengumuman Kantor",
          fontWeight = FontWeight.Black,
          fontSize = 22.sp
        )
        Text(
          text = "Pembaruan informasi, surat edaran dan agenda",
          fontSize = 12.5.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      if (isHrOrAdmin) {
        Button(
          onClick = { showDialog = true },
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(containerColor = SjPrimary),
          modifier = Modifier.testTag("btn_buat_pengumuman")
        ) {
          Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Buat")
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    if (announcements.isEmpty()) {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Belum ada pengumuman.", color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        items(announcements, key = { it.id }) { ann ->
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder()
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(text = ann.judul, fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.weight(1f))
                StatusBadge(status = ann.kategori)
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Dipublikasikan: ${ann.tanggal}",
                fontSize = 11.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Spacer(modifier = Modifier.height(10.dp))
              Text(
                text = ann.isi,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 19.sp
              )
            }
          }
        }
      }
    }
  }

  // Dialog Buat Pengumuman
  if (showDialog) {
    AlertDialog(
      onDismissRequest = { showDialog = false },
      title = { Text("Terbitkan Pengumuman", fontWeight = FontWeight.Bold) },
      text = {
        Column {
          OutlinedTextField(
            value = judulInput,
            onValueChange = { judulInput = it },
            label = { Text("Judul Pengumuman") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text("Kategori:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("Info", "Urgent", "Agenda").forEach { cat ->
              FilterChip(
                selected = kategoriInput == cat,
                onClick = { kategoriInput = cat },
                label = { Text(cat, fontSize = 11.sp) }
              )
            }
          }
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = isiInput,
            onValueChange = { isiInput = it },
            label = { Text("Isi Pengumuman") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 5
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (judulInput.isBlank() || isiInput.isBlank()) {
              viewModel.emitMessage("Judul dan isi pengumuman tidak boleh kosong.")
              return@Button
            }
            viewModel.addAnnouncement(judulInput.trim(), isiInput.trim(), kategoriInput)
            showDialog = false
            judulInput = ""
            isiInput = ""
          }
        ) {
          Text("Terbitkan")
        }
      },
      dismissButton = {
        TextButton(onClick = { showDialog = false }) {
          Text("Batal")
        }
      }
    )
  }
}
