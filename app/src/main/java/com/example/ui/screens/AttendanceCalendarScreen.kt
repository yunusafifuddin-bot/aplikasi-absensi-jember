package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AttendanceEntity
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.HrisViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AttendanceCalendarScreen(
  viewModel: HrisViewModel,
  modifier: Modifier = Modifier
) {
  val user by viewModel.currentUser.collectAsState()
  val attendances by viewModel.allAttendances.collectAsState()
  val cutis by viewModel.allCutis.collectAsState()
  val selectedMonth by viewModel.selectedMonth.collectAsState()

  var selectedDateStr by remember { mutableStateOf<String?>(null) }

  val userAttendances = remember(attendances, user) {
    attendances.filter { it.nik == user?.nik }
  }
  val userCutis = remember(cutis, user) {
    cutis.filter { it.nik == user?.nik && it.status.equals("approved", ignoreCase = true) }
  }

  // Attendance map by date
  val attMap = remember(userAttendances) {
    userAttendances.associateBy { it.tanggal }
  }

  // Parse Year and Month (e.g. "2026-09")
  val (year, month) = remember(selectedMonth) {
    val parts = selectedMonth.split("-")
    val y = parts.getOrNull(0)?.toIntOrNull() ?: 2026
    val m = parts.getOrNull(1)?.toIntOrNull() ?: 9
    y to m
  }

  val calendar = remember(year, month) {
    Calendar.getInstance().apply {
      set(Calendar.YEAR, year)
      set(Calendar.MONTH, month - 1)
      set(Calendar.DAY_OF_MONTH, 1)
    }
  }

  val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1 // 0 for Sunday
  val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
  val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

  val selectedAtt = selectedDateStr?.let { attMap[it] }

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(16.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "Kalender Kehadiran",
          fontWeight = FontWeight.Black,
          fontSize = 22.sp
        )
        Text(
          text = "Visualisasi kehadiran per bulan",
          fontSize = 12.5.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(
          onClick = {
            val newM = if (month == 1) 12 else month - 1
            val newY = if (month == 1) year - 1 else year
            viewModel.setSelectedMonth(String.format(Locale.US, "%04d-%02d", newY, newM))
          }
        ) {
          Icon(Icons.Default.ChevronLeft, contentDescription = "Bulan Lalu")
        }
        Text(
          text = String.format(Locale.US, "%02d / %04d", month, year),
          fontWeight = FontWeight.Bold,
          fontSize = 14.sp
        )
        IconButton(
          onClick = {
            val newM = if (month == 12) 1 else month + 1
            val newY = if (month == 12) year + 1 else year
            viewModel.setSelectedMonth(String.format(Locale.US, "%04d-%02d", newY, newM))
          }
        ) {
          Icon(Icons.Default.ChevronRight, contentDescription = "Bulan Depan")
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Calendar Card
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(18.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = CardDefaults.outlinedCardBorder()
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        // Day of Week Header
        val dows = listOf("Min", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab")
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
          dows.forEach { d ->
            Text(
              text = d,
              modifier = Modifier.weight(1f),
              textAlign = TextAlign.Center,
              fontWeight = FontWeight.Bold,
              fontSize = 11.5.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Calendar Grid
        val totalCells = firstDayOfWeek + daysInMonth
        val rows = (totalCells + 6) / 7

        for (r in 0 until rows) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround
          ) {
            for (c in 0 until 7) {
              val cellIndex = r * 7 + c
              val dayNumber = cellIndex - firstDayOfWeek + 1

              if (dayNumber in 1..daysInMonth) {
                val dayDateStr = String.format(Locale.US, "%04d-%02d-%02d", year, month, dayNumber)
                val att = attMap[dayDateStr]
                val onLeave = userCutis.any { dayDateStr >= it.tglMulai && dayDateStr <= it.tglSelesai }
                val isToday = dayDateStr == todayStr
                val isSelected = dayDateStr == selectedDateStr

                val (bgColor, textColor) = when {
                  onLeave -> SjInfoLight to SjInfo
                  att != null && att.status.equals("terlambat", true) -> SjWarningLight to SjWarning
                  att != null && att.jamMasuk != null -> SjSuccessLight to SjSuccess
                  else -> Color(0xFFF7F8FC) to Color(0xFF64748B)
                }

                Box(
                  modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .padding(2.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(bgColor)
                    .then(
                      if (isToday) Modifier.border(2.dp, SjPrimary, RoundedCornerShape(10.dp))
                      else if (isSelected) Modifier.border(2.dp, SjAccent, RoundedCornerShape(10.dp))
                      else Modifier
                    )
                    .clickable { selectedDateStr = dayDateStr },
                  contentAlignment = Alignment.Center
                ) {
                  Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                      text = "$dayNumber",
                      fontWeight = FontWeight.Bold,
                      fontSize = 12.sp,
                      color = textColor
                    )
                    if (att?.jamMasuk != null) {
                      Text(
                        text = att.jamMasuk.take(5),
                        fontSize = 8.sp,
                        color = textColor,
                        fontWeight = FontWeight.SemiBold
                      )
                    } else if (onLeave) {
                      Text(
                        text = "Cuti",
                        fontSize = 8.sp,
                        color = textColor,
                        fontWeight = FontWeight.Bold
                      )
                    }
                  }
                }
              } else {
                Spacer(modifier = Modifier.weight(1f).aspectRatio(1f).padding(2.dp))
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Legend
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceAround,
          verticalAlignment = Alignment.CenterVertically
        ) {
          LegendItem(color = SjSuccess, label = "Tepat Waktu")
          LegendItem(color = SjWarning, label = "Terlambat")
          LegendItem(color = SjInfo, label = "Cuti/Izin")
          LegendItem(color = Color(0xFFE2E8F0), label = "Tanpa Data")
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Selected Day Detail Card
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = CardDefaults.outlinedCardBorder()
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Text(
          text = if (selectedDateStr != null) "Detail Tanggal: $selectedDateStr" else "Pilih Tanggal di Kalender",
          fontWeight = FontWeight.Bold,
          fontSize = 15.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (selectedAtt != null) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text("Jam Masuk: ${selectedAtt.jamMasuk ?: "-"}", fontSize = 13.sp)
              Text("Jam Pulang: ${selectedAtt.jamPulang ?: "-"}", fontSize = 13.sp)
              Text("Durasi Kerja: ${selectedAtt.durasiJam} Jam", fontSize = 13.sp)
              Text("Verifikasi GPS: ${selectedAtt.jarakMeter} meter", fontSize = 13.sp)
            }
            StatusBadge(status = selectedAtt.status)
          }
        } else if (selectedDateStr != null) {
          Text(
            text = "Tidak ada rekaman presensi masuk/pulang pada tanggal ini.",
            fontSize = 12.5.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        } else {
          Text(
            text = "Ketuk salah satu kotak tanggal di kalender di atas untuk memeriksa status kehadiran lengkap.",
            fontSize = 12.5.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }
  }
}

@Composable
private fun LegendItem(color: Color, label: String) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Box(
      modifier = Modifier
        .size(10.dp)
        .clip(RoundedCornerShape(3.dp))
        .background(color)
    )
    Spacer(modifier = Modifier.width(4.dp))
    Text(text = label, fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
  }
}
