package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ShiftEntity
import com.example.ui.theme.SjPrimary
import com.example.ui.viewmodel.HrisViewModel

@Composable
fun ShiftManagementScreen(
  viewModel: HrisViewModel,
  modifier: Modifier = Modifier
) {
  val shifts by viewModel.allShifts.collectAsState()

  var editingShift by remember { mutableStateOf<ShiftEntity?>(null) }
  var isAddingNew by remember { mutableStateOf(false) }

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
          text = "Manajemen Shift",
          fontWeight = FontWeight.Black,
          fontSize = 22.sp
        )
        Text(
          text = "Konfigurasi jam masuk, pulang & toleransi keterlambatan",
          fontSize = 12.5.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Button(
        onClick = {
          editingShift = ShiftEntity(
            shiftId = "S${shifts.size + 1}",
            namaShift = "Shift Baru",
            jamMasuk = "08:00",
            jamPulang = "17:00",
            toleransiMenit = 15,
            keterangan = "Jam kerja operasional reguler"
          )
          isAddingNew = true
        },
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = SjPrimary),
        modifier = Modifier.testTag("btn_tambah_shift")
      ) {
        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Shift")
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      items(shifts, key = { it.shiftId }) { shift ->
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = CardDefaults.outlinedCardBorder()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "${shift.namaShift} (${shift.shiftId})",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
              )
              Text(
                text = "Jam Kerja: ${shift.jamMasuk} – ${shift.jamPulang}",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
              )
              Text(
                text = "Toleransi keterlambatan: ${shift.toleransiMenit} Menit",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              if (shift.keterangan.isNotBlank()) {
                Text(
                  text = shift.keterangan,
                  fontSize = 12.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            Row {
              IconButton(onClick = {
                editingShift = shift
                isAddingNew = false
              }) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Shift", tint = SjPrimary)
              }
              IconButton(onClick = {
                viewModel.deleteShift(shift.shiftId)
              }) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Hapus Shift", tint = MaterialTheme.colorScheme.error)
              }
            }
          }
        }
      }
    }
  }

  // Dialog Edit/Add Shift
  if (editingShift != null) {
    var id by remember { mutableStateOf(editingShift!!.shiftId) }
    var nama by remember { mutableStateOf(editingShift!!.namaShift) }
    var masuk by remember { mutableStateOf(editingShift!!.jamMasuk) }
    var pulang by remember { mutableStateOf(editingShift!!.jamPulang) }
    var toleransiInput by remember { mutableStateOf(editingShift!!.toleransiMenit.toString()) }
    var ket by remember { mutableStateOf(editingShift!!.keterangan) }

    AlertDialog(
      onDismissRequest = { editingShift = null },
      title = { Text(if (isAddingNew) "Tambah Shift Baru" else "Edit Jam Shift", fontWeight = FontWeight.Bold) },
      text = {
        Column {
          OutlinedTextField(
            value = id,
            onValueChange = { id = it },
            label = { Text("ID Shift") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            enabled = isAddingNew
          )
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = nama,
            onValueChange = { nama = it },
            label = { Text("Nama Shift") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(8.dp))
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
              value = masuk,
              onValueChange = { masuk = it },
              label = { Text("Masuk (HH:mm)") },
              singleLine = true,
              modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
              value = pulang,
              onValueChange = { pulang = it },
              label = { Text("Pulang (HH:mm)") },
              singleLine = true,
              modifier = Modifier.weight(1f)
            )
          }
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = toleransiInput,
            onValueChange = { toleransiInput = it },
            label = { Text("Toleransi (Menit)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = ket,
            onValueChange = { ket = it },
            label = { Text("Keterangan") },
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (id.isBlank() || nama.isBlank()) {
              viewModel.emitMessage("ID dan Nama Shift tidak boleh kosong.")
              return@Button
            }
            val updated = editingShift!!.copy(
              shiftId = id.trim(),
              namaShift = nama.trim(),
              jamMasuk = masuk.trim(),
              jamPulang = pulang.trim(),
              toleransiMenit = toleransiInput.toIntOrNull() ?: 15,
              keterangan = ket.trim()
            )
            viewModel.saveShift(updated)
            editingShift = null
          }
        ) {
          Text("Simpan")
        }
      },
      dismissButton = {
        TextButton(onClick = { editingShift = null }) {
          Text("Batal")
        }
      }
    )
  }
}
