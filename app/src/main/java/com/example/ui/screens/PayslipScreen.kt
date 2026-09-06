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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.PayrollEntity
import com.example.ui.components.StatusBadge
import com.example.ui.components.formatRupiah
import com.example.ui.theme.SjPrimary
import com.example.ui.viewmodel.HrisViewModel

@Composable
fun PayslipScreen(
  viewModel: HrisViewModel,
  modifier: Modifier = Modifier
) {
  val user by viewModel.currentUser.collectAsState()
  val payrolls by viewModel.allPayrolls.collectAsState()

  val userPayrolls = remember(payrolls, user) {
    payrolls.filter { it.nik == user?.nik }
  }

  var selectedSlip by remember { mutableStateOf<PayrollEntity?>(null) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(16.dp)
  ) {
    Text(
      text = "Slip Gaji",
      fontWeight = FontWeight.Black,
      fontSize = 22.sp
    )
    Text(
      text = "Riwayat penggajian dan rincian slip gaji resmi Anda",
      fontSize = 12.5.sp,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Gradient Estimasi Gaji Feature Card
    val latestPayroll = userPayrolls.firstOrNull()
    val estimasiGaji = latestPayroll?.gajiBersih ?: (user?.gajiPokok ?: 4500000L)
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(26.dp),
      colors = CardDefaults.cardColors(containerColor = Color.Transparent),
      border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
      elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .background(com.example.ui.theme.BentoEstimasiGajiGradient)
          .padding(20.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "ESTIMASI GAJI BERSIH (TAKE HOME PAY)",
              fontSize = 10.5.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFFE0E7FF),
              letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = formatRupiah(estimasiGaji),
              fontSize = 24.sp,
              fontWeight = FontWeight.Black,
              color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "Periode: ${latestPayroll?.periode ?: "September 2026"} · Transfer Rekening",
              fontSize = 11.5.sp,
              color = Color.White.copy(alpha = 0.88f)
            )
          }
          Box(
            modifier = Modifier
              .size(52.dp)
              .clip(RoundedCornerShape(18.dp))
              .background(Color.White.copy(alpha = 0.22f))
              .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
          ) {
            Text(text = "💳", fontSize = 24.sp)
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(18.dp))

    if (userPayrolls.isEmpty()) {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Belum ada slip gaji yang diterbitkan.", color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        items(userPayrolls, key = { it.idPayroll }) { p ->
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
                Text(
                  text = "Periode ${p.periode}",
                  fontWeight = FontWeight.Bold,
                  fontSize = 16.sp
                )
                StatusBadge(status = p.status)
              }

              Spacer(modifier = Modifier.height(12.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Column {
                  Text("Gaji Pokok", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                  Text(formatRupiah(p.gajiPokok), fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp)
                }
                Column {
                  Text("Tunjangan", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                  Text(formatRupiah(p.tunjangan), fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                  Text("Lembur", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                  Text(formatRupiah(p.uangLembur), fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp)
                }
              }

              Spacer(modifier = Modifier.height(8.dp))
              Divider(color = MaterialTheme.colorScheme.outlineVariant)
              Spacer(modifier = Modifier.height(8.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(
                    text = "Gaji Bersih (Take Home Pay)",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                  Text(
                    text = formatRupiah(p.gajiBersih),
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                  )
                }

                Button(
                  onClick = { selectedSlip = p },
                  shape = RoundedCornerShape(10.dp),
                  colors = ButtonDefaults.buttonColors(containerColor = SjPrimary)
                ) {
                  Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Lihat Slip")
                }
              }
            }
          }
        }
      }
    }
  }

  // Slip Gaji Detail Sheet / Dialog
  if (selectedSlip != null) {
    val slip = selectedSlip!!
    AlertDialog(
      onDismissRequest = { selectedSlip = null },
      title = {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text("SUKSES JAYA JEMBER", fontWeight = FontWeight.Black, fontSize = 16.sp)
            Text("Slip Gaji Karyawan · Periode ${slip.periode}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
          StatusBadge(status = slip.status)
        }
      },
      text = {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(14.dp)
        ) {
          Text("NIK: ${slip.nik}  •  Nama: ${slip.nama}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
          Text("Jabatan: ${slip.jabatan}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

          Spacer(modifier = Modifier.height(10.dp))
          Text("A. PENDAPATAN", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SjPrimary)
          Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
            Text("• Gaji Pokok", fontSize = 12.sp)
            Text(formatRupiah(slip.gajiPokok), fontSize = 12.sp)
          }
          Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
            Text("• Tunjangan Jabatan", fontSize = 12.sp)
            Text(formatRupiah(slip.tunjangan), fontSize = 12.sp)
          }
          Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
            Text("• Uang Lembur (Approved)", fontSize = 12.sp)
            Text(formatRupiah(slip.uangLembur), fontSize = 12.sp)
          }

          Spacer(modifier = Modifier.height(8.dp))
          Text("B. POTONGAN", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFDC2626))
          Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
            Text("• Potongan Kasbon", fontSize = 12.sp)
            Text("- " + formatRupiah(slip.potonganKasbon), fontSize = 12.sp)
          }
          Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
            Text("• BPJS Kes & Ketenagakerjaan", fontSize = 12.sp)
            Text("- " + formatRupiah(slip.potonganLain), fontSize = 12.sp)
          }

          Spacer(modifier = Modifier.height(10.dp))
          Divider()
          Spacer(modifier = Modifier.height(10.dp))

          Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Text("TOTAL DITERIMA:", fontWeight = FontWeight.Black, fontSize = 13.sp)
            Text(formatRupiah(slip.gajiBersih), fontWeight = FontWeight.Black, fontSize = 17.sp, color = SjPrimary)
          }
          if (slip.tanggalBayar != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text("Ditransfer pada: ${slip.tanggalBayar}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.emitMessage("Slip Gaji periode ${slip.periode} berhasil diunduh ke memori.")
            selectedSlip = null
          }
        ) {
          Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Unduh PDF")
        }
      },
      dismissButton = {
        TextButton(onClick = { selectedSlip = null }) {
          Text("Tutup")
        }
      }
    )
  }
}
