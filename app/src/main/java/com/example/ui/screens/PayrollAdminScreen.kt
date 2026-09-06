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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.PayrollEntity
import com.example.ui.components.StatusBadge
import com.example.ui.components.formatRupiah
import com.example.ui.theme.SjPrimary
import com.example.ui.theme.SjSuccess
import com.example.ui.viewmodel.HrisViewModel

@Composable
fun PayrollAdminScreen(
  viewModel: HrisViewModel,
  modifier: Modifier = Modifier
) {
  val payrolls by viewModel.allPayrolls.collectAsState()
  val selectedMonth by viewModel.selectedMonth.collectAsState()

  val filteredPayrolls = remember(payrolls, selectedMonth) {
    payrolls.filter { it.periode == selectedMonth }
  }

  var selectedSlip by remember { mutableStateOf<PayrollEntity?>(null) }
  var showGenerateDialog by remember { mutableStateOf(false) }

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
          text = "Payroll Perusahaan",
          fontWeight = FontWeight.Black,
          fontSize = 22.sp
        )
        Text(
          text = "Generate penggajian, potong kasbon & tanda bayar",
          fontSize = 12.5.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Button(
        onClick = { showGenerateDialog = true },
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = SjPrimary),
        modifier = Modifier.testTag("btn_generate_payroll")
      ) {
        Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Generate")
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Periode switcher
    Row(verticalAlignment = Alignment.CenterVertically) {
      Text("Periode: ", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
      FilterChip(
        selected = selectedMonth == "2026-09",
        onClick = { viewModel.setSelectedMonth("2026-09") },
        label = { Text("September 2026") }
      )
      Spacer(modifier = Modifier.width(6.dp))
      FilterChip(
        selected = selectedMonth == "2026-08",
        onClick = { viewModel.setSelectedMonth("2026-08") },
        label = { Text("Agustus 2026") }
      )
    }

    Spacer(modifier = Modifier.height(14.dp))

    if (filteredPayrolls.isEmpty()) {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text("Belum ada payroll untuk periode $selectedMonth.", color = MaterialTheme.colorScheme.onSurfaceVariant)
          Spacer(modifier = Modifier.height(8.dp))
          Button(onClick = { viewModel.generatePayroll(selectedMonth) }) {
            Text("Generate Payroll $selectedMonth Sekarang")
          }
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        items(filteredPayrolls, key = { it.idPayroll }) { p ->
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder()
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(p.nama, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                  Text("${p.nik} · ${p.jabatan}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                StatusBadge(status = p.status)
              }

              Spacer(modifier = Modifier.height(10.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text("Gaji Bersih:", fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(formatRupiah(p.gajiBersih), fontWeight = FontWeight.Black, fontSize = 16.sp, color = SjPrimary)
              }

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text("Potongan Kasbon:", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(formatRupiah(p.potonganKasbon), fontSize = 12.sp, color = Color(0xFFDC2626))
              }

              Spacer(modifier = Modifier.height(10.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
              ) {
                OutlinedButton(
                  onClick = { selectedSlip = p },
                  shape = RoundedCornerShape(8.dp),
                  modifier = Modifier.height(36.dp)
                ) {
                  Text("Rincian", fontSize = 12.sp)
                }

                if (p.status.equals("draft", ignoreCase = true)) {
                  Spacer(modifier = Modifier.width(8.dp))
                  Button(
                    onClick = { viewModel.markPayrollPaid(p.idPayroll) },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SjSuccess),
                    modifier = Modifier.height(36.dp)
                  ) {
                    Text("Tandai Dibayar", fontSize = 12.sp)
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  // Dialog Generate Payroll
  if (showGenerateDialog) {
    AlertDialog(
      onDismissRequest = { showGenerateDialog = false },
      title = { Text("Generate Payroll Otomatis", fontWeight = FontWeight.Bold) },
      text = {
        Text("Apakah Anda ingin men-generate / kalkulasi ulang slip gaji seluruh karyawan untuk periode $selectedMonth? Gaji pokok, tunjangan, lembur, dan potongan kasbon akan dihitung otomatis.")
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.generatePayroll(selectedMonth)
            showGenerateDialog = false
          }
        ) {
          Text("Ya, Generate Sekarang")
        }
      },
      dismissButton = {
        TextButton(onClick = { showGenerateDialog = false }) {
          Text("Batal")
        }
      }
    )
  }

  // View Slip Dialog
  if (selectedSlip != null) {
    val slip = selectedSlip!!
    AlertDialog(
      onDismissRequest = { selectedSlip = null },
      title = { Text("Rincian Slip: ${slip.nama}") },
      text = {
        Column {
          Text("Periode: ${slip.periode}  •  Status: ${slip.status}", fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(8.dp))
          Text("• Gaji Pokok: ${formatRupiah(slip.gajiPokok)}")
          Text("• Tunjangan: ${formatRupiah(slip.tunjangan)}")
          Text("• Uang Lembur: ${formatRupiah(slip.uangLembur)}")
          Text("• Potongan Kasbon: -${formatRupiah(slip.potonganKasbon)}")
          Text("• Potongan BPJS: -${formatRupiah(slip.potonganLain)}")
          Spacer(modifier = Modifier.height(8.dp))
          Text("Total Diterima: ${formatRupiah(slip.gajiBersih)}", fontWeight = FontWeight.Black, fontSize = 16.sp, color = SjPrimary)
        }
      },
      confirmButton = {
        Button(onClick = { selectedSlip = null }) {
          Text("Tutup")
        }
      }
    )
  }
}
