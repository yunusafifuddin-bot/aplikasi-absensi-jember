package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import com.example.data.local.EmployeeEntity
import com.example.ui.components.AvatarCircle
import com.example.ui.components.StatusBadge
import com.example.ui.components.formatRupiah
import com.example.ui.theme.SjPrimary
import com.example.ui.viewmodel.HrisViewModel

@Composable
fun EmployeeManagementScreen(
  viewModel: HrisViewModel,
  modifier: Modifier = Modifier
) {
  val employees by viewModel.allEmployees.collectAsState()
  val shifts by viewModel.allShifts.collectAsState()

  val shiftMap = remember(shifts) { shifts.associateBy({ it.shiftId }, { it.namaShift }) }

  var editingEmployee by remember { mutableStateOf<EmployeeEntity?>(null) }
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
          text = "Manajemen Karyawan",
          fontWeight = FontWeight.Black,
          fontSize = 22.sp
        )
        Text(
          text = "Tambah, ubah data & aktifkan/nonaktifkan karyawan",
          fontSize = 12.5.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Button(
        onClick = {
          editingEmployee = EmployeeEntity(
            nik = "SJ00${employees.size + 1}",
            nama = "",
            jabatan = "Staff Operasional",
            role = "USER",
            status = "Aktif",
            limitKasbon = 2500000L,
            gajiPokok = 4000000L,
            tunjangan = 750000L,
            shiftId = shifts.firstOrNull()?.shiftId ?: "S1"
          )
          isAddingNew = true
        },
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = SjPrimary),
        modifier = Modifier.testTag("btn_tambah_karyawan")
      ) {
        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Karyawan")
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      items(employees, key = { it.nik }) { emp ->
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = CardDefaults.outlinedCardBorder()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            AvatarCircle(name = emp.nama, photoUrl = emp.fotoUrl, size = 48.dp, fontSize = 16)
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
              Text(text = emp.nama, fontWeight = FontWeight.Bold, fontSize = 15.sp)
              Text(
                text = "${emp.nik} · ${emp.jabatan}",
                fontSize = 12.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = "Shift: ${shiftMap[emp.shiftId] ?: "-"} · Limit: ${formatRupiah(emp.limitKasbon)}",
                fontSize = 11.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Spacer(modifier = Modifier.height(4.dp))
              Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                StatusBadge(status = emp.role)
                StatusBadge(status = emp.status)
              }
            }

            Column(horizontalAlignment = Alignment.End) {
              IconButton(onClick = {
                editingEmployee = emp
                isAddingNew = false
              }) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Karyawan", tint = SjPrimary)
              }
              IconButton(onClick = {
                viewModel.deleteEmployee(emp.nik)
              }) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Hapus Karyawan", tint = MaterialTheme.colorScheme.error)
              }
            }
          }
        }
      }
    }
  }

  // Edit / Add Employee Dialog
  if (editingEmployee != null) {
    var nik by remember { mutableStateOf(editingEmployee!!.nik) }
    var nama by remember { mutableStateOf(editingEmployee!!.nama) }
    var jabatan by remember { mutableStateOf(editingEmployee!!.jabatan) }
    var role by remember { mutableStateOf(editingEmployee!!.role) }
    var status by remember { mutableStateOf(editingEmployee!!.status) }
    var shiftId by remember { mutableStateOf(editingEmployee!!.shiftId) }
    var limitInput by remember { mutableStateOf(editingEmployee!!.limitKasbon.toString()) }
    var gajiInput by remember { mutableStateOf(editingEmployee!!.gajiPokok.toString()) }
    var tunjanganInput by remember { mutableStateOf(editingEmployee!!.tunjangan.toString()) }

    AlertDialog(
      onDismissRequest = { editingEmployee = null },
      title = { Text(if (isAddingNew) "Tambah Karyawan Baru" else "Edit Data Karyawan", fontWeight = FontWeight.Bold) },
      text = {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
        ) {
          OutlinedTextField(
            value = nik,
            onValueChange = { nik = it },
            label = { Text("NIK") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            enabled = isAddingNew
          )
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = nama,
            onValueChange = { nama = it },
            label = { Text("Nama Lengkap") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = jabatan,
            onValueChange = { jabatan = it },
            label = { Text("Jabatan") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(8.dp))

          // Role selection chips
          Text("Role:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("USER", "HR", "ADMIN").forEach { r ->
              FilterChip(
                selected = role == r,
                onClick = { role = r },
                label = { Text(r, fontSize = 11.sp) }
              )
            }
          }

          Spacer(modifier = Modifier.height(8.dp))
          // Status selection chips
          Text("Status:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("Aktif", "Nonaktif").forEach { s ->
              FilterChip(
                selected = status == s,
                onClick = { status = s },
                label = { Text(s, fontSize = 11.sp) }
              )
            }
          }

          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = limitInput,
            onValueChange = { limitInput = it },
            label = { Text("Limit Kasbon (Rp)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = gajiInput,
            onValueChange = { gajiInput = it },
            label = { Text("Gaji Pokok (Rp)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = tunjanganInput,
            onValueChange = { tunjanganInput = it },
            label = { Text("Tunjangan (Rp)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (nik.isBlank() || nama.isBlank()) {
              viewModel.emitMessage("NIK dan Nama tidak boleh kosong.")
              return@Button
            }
            val updated = editingEmployee!!.copy(
              nik = nik.trim(),
              nama = nama.trim(),
              jabatan = jabatan.trim(),
              role = role,
              status = status,
              shiftId = shiftId,
              limitKasbon = limitInput.toLongOrNull() ?: 2500000L,
              gajiPokok = gajiInput.toLongOrNull() ?: 4000000L,
              tunjangan = tunjanganInput.toLongOrNull() ?: 750000L
            )
            viewModel.saveEmployee(updated)
            editingEmployee = null
          }
        ) {
          Text("Simpan")
        }
      },
      dismissButton = {
        TextButton(onClick = { editingEmployee = null }) {
          Text("Batal")
        }
      }
    )
  }
}
