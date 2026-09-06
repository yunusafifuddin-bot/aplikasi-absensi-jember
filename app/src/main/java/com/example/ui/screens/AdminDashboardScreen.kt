package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.HrisViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AdminDashboardScreen(
  viewModel: HrisViewModel,
  modifier: Modifier = Modifier
) {
  val employees by viewModel.allEmployees.collectAsState()
  val attendances by viewModel.allAttendances.collectAsState()
  val kasbons by viewModel.allKasbons.collectAsState()
  val cutis by viewModel.allCutis.collectAsState()
  val lemburs by viewModel.allLemburs.collectAsState()

  val todayDate = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
  val todayAttendances = remember(attendances, todayDate) {
    attendances.filter { it.tanggal == todayDate }
  }

  val totalEmp = employees.size
  val presentToday = todayAttendances.size
  val lateToday = todayAttendances.count { it.status.equals("terlambat", ignoreCase = true) }
  val onTimeToday = presentToday - lateToday

  val pendingKasbon = kasbons.filter { it.statusPersetujuan == "Pending" }
  val pendingCuti = cutis.filter { it.status == "Pending" }
  val pendingLembur = lemburs.filter { it.statusPersetujuan == "Pending" }
  val totalPending = pendingKasbon.size + pendingCuti.size + pendingLembur.size

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(16.dp)
  ) {
    Text(
      text = "HR Dashboard",
      fontWeight = FontWeight.Black,
      fontSize = 22.sp
    )
    Text(
      text = "Monitoring operasional, approval, dan presensi perusahaan",
      fontSize = 12.5.sp,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(16.dp))

    // 4 Key Stat Cards
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
      StatCard(
        title = "Total Karyawan",
        value = "$totalEmp",
        icon = Icons.Default.Groups,
        iconBgBrush = Brush.linearGradient(listOf(SjPrimary, Color(0xFF6366F1))),
        modifier = Modifier.weight(1f)
      )
      StatCard(
        title = "Hadir Hari Ini",
        value = "$presentToday",
        icon = Icons.Default.CheckCircle,
        iconBgBrush = Brush.linearGradient(listOf(SjSuccess, Color(0xFF22C55E))),
        modifier = Modifier.weight(1f)
      )
    }

    Spacer(modifier = Modifier.height(10.dp))

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
      StatCard(
        title = "Terlambat Hari Ini",
        value = "$lateToday",
        icon = Icons.Default.AccessTime,
        iconBgBrush = Brush.linearGradient(listOf(SjWarning, Color(0xFFF59E0B))),
        modifier = Modifier.weight(1f)
      )
      StatCard(
        title = "Pending Approval",
        value = "$totalPending",
        icon = Icons.Default.NotificationsActive,
        iconBgBrush = Brush.linearGradient(listOf(SjDanger, Color(0xFFEF4444))),
        modifier = Modifier.weight(1f)
      )
    }

    Spacer(modifier = Modifier.height(18.dp))

    // Composition Today Card
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(18.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = CardDefaults.outlinedCardBorder()
    ) {
      Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Komposisi Kehadiran Hari Ini", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(12.dp))
        CompanyAnalyticsDonut(
          presentOnTime = onTimeToday,
          late = lateToday,
          absentOrPending = (totalEmp - presentToday).coerceAtLeast(0)
        )
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Pending Approvals Section
    Text("Persetujuan Pending ($totalPending)", fontWeight = FontWeight.Black, fontSize = 17.sp)
    Spacer(modifier = Modifier.height(10.dp))

    if (totalPending == 0) {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
      ) {
        Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
          Text("Tidak ada pengajuan yang menunggu persetujuan.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
    } else {
      // Kasbon Pending
      pendingKasbon.forEach { kb ->
        ApprovalItemCard(
          badgeLabel = "KASBON",
          title = "${kb.nama} (${kb.nik})",
          subtitle = "${formatRupiah(kb.jumlah)} · ${kb.keterangan}",
          onApprove = { viewModel.approveApproval("kasbon", kb.idKasbon) },
          onReject = { viewModel.rejectApproval("kasbon", kb.idKasbon) }
        )
      }

      // Cuti Pending
      pendingCuti.forEach { ct ->
        ApprovalItemCard(
          badgeLabel = "CUTI",
          title = "${ct.nama} (${ct.nik})",
          subtitle = "${ct.jenisCuti}: ${ct.tglMulai} s/d ${ct.tglSelesai} (${ct.alasan})",
          onApprove = { viewModel.approveApproval("cuti", ct.idCuti) },
          onReject = { viewModel.rejectApproval("cuti", ct.idCuti) }
        )
      }

      // Lembur Pending
      pendingLembur.forEach { ot ->
        ApprovalItemCard(
          badgeLabel = "LEMBUR",
          title = "${ot.nama} (${ot.nik})",
          subtitle = "${ot.tanggal} (${ot.jamMulai} - ${ot.jamSelesai}, ${ot.durasiJam} jam): ${ot.deskripsi}",
          onApprove = { viewModel.approveApproval("lembur", ot.idLembur) },
          onReject = { viewModel.rejectApproval("lembur", ot.idLembur) }
        )
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Top Keterlambatan Bulan Ini
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(18.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = CardDefaults.outlinedCardBorder()
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Text("Top Keterlambatan Bulan Ini", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(8.dp))

        val thisMonthAttendances = attendances.filter { it.tanggal.startsWith(todayDate.take(7)) }
        val lateByNik = thisMonthAttendances
          .filter { it.status.equals("terlambat", ignoreCase = true) }
          .groupBy { it.nik }
          .mapValues { it.value.size }
          .toList()
          .sortedByDescending { it.second }

        if (lateByNik.isEmpty()) {
          Text("Tidak ada catatan keterlambatan bulan ini.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.5.sp)
        } else {
          lateByNik.take(3).forEachIndexed { index, (nik, count) ->
            val empName = employees.firstOrNull { it.nik == nik }?.nama ?: nik
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text("${index + 1}. $empName ($nik)", fontSize = 13.sp)
              Surface(
                color = SjDangerLight,
                shape = RoundedCornerShape(99.dp)
              ) {
                Text(
                  text = "${count}x terlambat",
                  color = SjDanger,
                  fontWeight = FontWeight.Bold,
                  fontSize = 11.sp,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
              }
            }
            if (index < lateByNik.take(3).size - 1) {
              Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(30.dp))
  }
}

@Composable
private fun ApprovalItemCard(
  badgeLabel: String,
  title: String,
  subtitle: String,
  onApprove: () -> Unit,
  onReject: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
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
        Text(title, fontWeight = FontWeight.Bold, fontSize = 14.5.sp)
        StatusBadge(status = badgeLabel)
      }
      Spacer(modifier = Modifier.height(4.dp))
      Text(subtitle, fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
      ) {
        OutlinedButton(
          onClick = onReject,
          shape = RoundedCornerShape(8.dp),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
          modifier = Modifier.height(36.dp)
        ) {
          Text("Reject", fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(
          onClick = onApprove,
          shape = RoundedCornerShape(8.dp),
          colors = ButtonDefaults.buttonColors(containerColor = SjPrimary),
          modifier = Modifier.height(36.dp)
        ) {
          Text("Approve", fontSize = 12.sp)
        }
      }
    }
  }
}
