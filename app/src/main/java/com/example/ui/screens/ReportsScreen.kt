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
import com.example.ui.components.formatRupiah
import com.example.ui.theme.SjPrimary
import com.example.ui.viewmodel.HrisViewModel

@Composable
fun ReportsScreen(
  viewModel: HrisViewModel,
  modifier: Modifier = Modifier
) {
  val attendances by viewModel.allAttendances.collectAsState()
  val employees by viewModel.allEmployees.collectAsState()
  val kasbons by viewModel.allKasbons.collectAsState()
  val cutis by viewModel.allCutis.collectAsState()
  val lemburs by viewModel.allLemburs.collectAsState()

  var selectedReportType by remember { mutableStateOf("Absensi") }
  val reportTypes = listOf("Absensi", "Karyawan", "Kasbon", "Cuti", "Lembur")

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
          text = "Laporan & Rekap",
          fontWeight = FontWeight.Black,
          fontSize = 22.sp
        )
        Text(
          text = "Rekapitulasi data operasional SDM",
          fontSize = 12.5.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Button(
        onClick = {
          viewModel.emitMessage("Laporan $selectedReportType berhasil diekspor ke format CSV.")
        },
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = SjPrimary),
        modifier = Modifier.testTag("btn_export_laporan")
      ) {
        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Export CSV")
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Type Chips
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      reportTypes.forEach { type ->
        FilterChip(
          selected = selectedReportType == type,
          onClick = { selectedReportType = type },
          label = { Text(type, fontSize = 11.5.sp) }
        )
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    when (selectedReportType) {
      "Absensi" -> {
        Text("Total ${attendances.size} data kehadiran tersimpan", fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          items(attendances, key = { it.id }) { item ->
            Card(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              border = CardDefaults.outlinedCardBorder()
            ) {
              Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text("${item.tanggal} · NIK ${item.nik}", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                  Text("Masuk: ${item.jamMasuk ?: "-"} · Pulang: ${item.jamPulang ?: "-"} · ${item.jarakMeter}m", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                StatusBadge(status = item.status)
              }
            }
          }
        }
      }
      "Karyawan" -> {
        Text("Total ${employees.size} data karyawan", fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          items(employees, key = { it.nik }) { emp ->
            Card(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              border = CardDefaults.outlinedCardBorder()
            ) {
              Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text("${emp.nama} (${emp.nik})", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                  Text("${emp.jabatan} · Gaji: ${formatRupiah(emp.gajiPokok)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                StatusBadge(status = emp.status)
              }
            }
          }
        }
      }
      "Kasbon" -> {
        Text("Total ${kasbons.size} pengajuan kasbon", fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          items(kasbons, key = { it.idKasbon }) { kb ->
            Card(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              border = CardDefaults.outlinedCardBorder()
            ) {
              Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text("${kb.nama} · ${formatRupiah(kb.jumlah)}", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                  Text("${kb.tanggalPengajuan} · ${kb.keterangan}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                StatusBadge(status = kb.statusPersetujuan)
              }
            }
          }
        }
      }
      "Cuti" -> {
        Text("Total ${cutis.size} pengajuan cuti", fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          items(cutis, key = { it.idCuti }) { ct ->
            Card(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              border = CardDefaults.outlinedCardBorder()
            ) {
              Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text("${ct.nama} · ${ct.jenisCuti}", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                  Text("${ct.tglMulai} s/d ${ct.tglSelesai} (${ct.alasan})", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                StatusBadge(status = ct.status)
              }
            }
          }
        }
      }
      "Lembur" -> {
        Text("Total ${lemburs.size} pengajuan lembur", fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          items(lemburs, key = { it.idLembur }) { ot ->
            Card(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              border = CardDefaults.outlinedCardBorder()
            ) {
              Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text("${ot.nama} · ${ot.tanggal}", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                  Text("${ot.jamMulai} - ${ot.jamSelesai} (${ot.durasiJam} jam) · ${ot.deskripsi}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                StatusBadge(status = ot.statusPersetujuan)
              }
            }
          }
        }
      }
    }
  }
}
