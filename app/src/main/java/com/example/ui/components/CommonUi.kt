package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AttendanceEntity
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

fun formatRupiah(amount: Long): String {
  val format = NumberFormat.getNumberInstance(Locale("id", "ID"))
  return "Rp " + format.format(amount)
}

@Composable
fun StatusBadge(status: String, modifier: Modifier = Modifier) {
  val s = status.trim().lowercase()
  val (bgColor, textColor) = when {
    s in listOf("tepat waktu", "hadir", "aktif", "approved", "paid", "dibayar", "disetujui") ->
      SjSuccessLight to SjSuccess
    s in listOf("terlambat", "pending", "draft", "menunggu") ->
      SjWarningLight to SjWarning
    s in listOf("rejected", "nonaktif", "ditolak", "alpha") ->
      SjDangerLight to SjDanger
    s in listOf("cuti", "izin", "sakit") ->
      SjInfoLight to SjInfo
    else ->
      Color(0xFFF1F2F6) to Color(0xFF4B5563)
  }

  Surface(
    color = bgColor,
    shape = RoundedCornerShape(999.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, textColor.copy(alpha = 0.25f)),
    shadowElevation = 1.dp,
    modifier = modifier
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
      Box(
        modifier = Modifier
          .size(6.dp)
          .clip(CircleShape)
          .background(textColor)
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = status,
        color = textColor,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp
      )
    }
  }
}

@Composable
fun StatCard(
  title: String,
  value: String,
  icon: ImageVector,
  iconBgBrush: Brush,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(containerColor = BentoCardWhite),
    border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder),
    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      Box(
        modifier = Modifier
          .size(42.dp)
          .clip(RoundedCornerShape(16.dp))
          .background(iconBgBrush),
        contentAlignment = Alignment.Center
      ) {
        Icon(imageVector = icon, contentDescription = title, tint = Color.White, modifier = Modifier.size(20.dp))
      }
      Spacer(modifier = Modifier.height(14.dp))
      Column {
        Text(
          text = title.uppercase(),
          fontWeight = FontWeight.Bold,
          fontSize = 10.sp,
          letterSpacing = 0.5.sp,
          color = BentoSlate400
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = value,
          fontWeight = FontWeight.Black,
          fontSize = 20.sp,
          color = BentoSlate800
        )
      }
    }
  }
}

@Composable
fun BentoStatTile(
  title: String,
  value: String,
  iconText: String,
  bgColor: Color = BentoEmeraldBg,
  iconColor: Color = BentoEmerald,
  gradientBrush: Brush? = null,
  onClick: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val isGradient = gradientBrush != null
  val actualTitleColor = if (isGradient) Color.White.copy(alpha = 0.88f) else BentoSlate400
  val actualValueColor = if (isGradient) Color.White else BentoSlate800

  Card(
    modifier = modifier
      .fillMaxWidth()
      .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
    shape = RoundedCornerShape(26.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isGradient) Color.Transparent else BentoCardWhite
    ),
    border = androidx.compose.foundation.BorderStroke(
      1.dp,
      if (isGradient) Color.White.copy(alpha = 0.35f) else BentoBorder
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = if (isGradient) 4.dp else 3.dp)
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .then(if (isGradient) Modifier.background(gradientBrush!!) else Modifier)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp)
          .heightIn(min = 108.dp),
        verticalArrangement = Arrangement.SpaceBetween
      ) {
        Box(
          modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (isGradient) Color.White.copy(alpha = 0.22f) else bgColor)
            .then(
              if (isGradient) Modifier.border(
                1.dp,
                Color.White.copy(alpha = 0.35f),
                RoundedCornerShape(14.dp)
              ) else Modifier
            ),
          contentAlignment = Alignment.Center
        ) {
          Text(text = iconText, fontSize = 18.sp)
        }
        Column {
          Text(
            text = title.uppercase(),
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            letterSpacing = 0.5.sp,
            color = actualTitleColor
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = value,
            fontWeight = FontWeight.Black,
            fontSize = 18.sp,
            color = actualValueColor
          )
        }
      }
    }
  }
}

@Composable
fun BentoTileWide(
  title: String,
  value: String,
  iconText: String,
  iconBgColor: Color = BentoIndigoBg,
  gradientBrush: Brush? = null,
  onClick: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val isGradient = gradientBrush != null
  val actualTitleColor = if (isGradient) Color.White.copy(alpha = 0.88f) else BentoSlate400
  val actualValueColor = if (isGradient) Color.White else BentoSlate800
  val arrowColor = if (isGradient) Color.White.copy(alpha = 0.85f) else BentoSlate400.copy(alpha = 0.6f)

  Card(
    modifier = modifier
      .fillMaxWidth()
      .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
    shape = RoundedCornerShape(26.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isGradient) Color.Transparent else BentoCardWhite
    ),
    border = androidx.compose.foundation.BorderStroke(
      1.dp,
      if (isGradient) Color.White.copy(alpha = 0.35f) else BentoBorder
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = if (isGradient) 4.dp else 3.dp)
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .then(if (isGradient) Modifier.background(gradientBrush!!) else Modifier)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(48.dp)
              .clip(RoundedCornerShape(16.dp))
              .background(if (isGradient) Color.White.copy(alpha = 0.22f) else iconBgColor)
              .then(
                if (isGradient) Modifier.border(
                  1.dp,
                  Color.White.copy(alpha = 0.35f),
                  RoundedCornerShape(16.dp)
                ) else Modifier
              ),
            contentAlignment = Alignment.Center
          ) {
            Text(text = iconText, fontSize = 22.sp)
          }
          Spacer(modifier = Modifier.width(14.dp))
          Column {
            Text(
              text = title.uppercase(),
              fontWeight = FontWeight.Bold,
              fontSize = 10.sp,
              letterSpacing = 0.5.sp,
              color = actualTitleColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = value,
              fontWeight = FontWeight.Black,
              fontSize = 19.sp,
              color = actualValueColor
            )
          }
        }
        Icon(
          imageVector = Icons.Default.ArrowForward,
          contentDescription = null,
          tint = arrowColor,
          modifier = Modifier.size(18.dp)
        )
      }
    }
  }
}

@Composable
fun BentoDarkSlateCard(
  category: String,
  headline: String,
  iconText: String = "📢",
  onClick: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
    shape = RoundedCornerShape(26.dp),
    colors = CardDefaults.cardColors(containerColor = BentoCardWhite),
    border = androidx.compose.foundation.BorderStroke(1.dp, FormalBorder),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .padding(20.dp)
    ) {
      // Elegant watermark circle
      Canvas(
        modifier = Modifier
          .size(110.dp)
          .align(Alignment.TopEnd)
      ) {
        drawCircle(
          color = SjPrimary.copy(alpha = 0.04f),
          radius = size.width / 1.5f,
          center = Offset(size.width * 0.8f, -size.height * 0.2f)
        )
      }

      Column {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Box(
            modifier = Modifier
              .size(30.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(BentoIndigoBg),
            contentAlignment = Alignment.Center
          ) {
            Text(text = iconText, fontSize = 15.sp)
          }
          Text(
            text = category.uppercase(),
            color = SjPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.2.sp
          )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
          text = headline,
          color = BentoSlate800,
          fontSize = 14.sp,
          fontWeight = FontWeight.SemiBold,
          lineHeight = 20.sp,
          maxLines = 3
        )
      }
    }
  }
}

@Composable
fun AvatarCircle(
  name: String,
  photoUrl: String? = null,
  size: Dp = 42.dp,
  fontSize: Int = 14,
  modifier: Modifier = Modifier
) {
  val initials = name.split(" ")
    .filter { it.isNotBlank() }
    .take(2)
    .map { it.first().uppercase() }
    .joinToString("")
    .ifEmpty { "SJ" }

  Box(
    modifier = modifier
      .size(size)
      .clip(CircleShape)
      .background(
        Brush.linearGradient(
          colors = listOf(SjPrimary, SjAccent)
        )
      ),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = initials,
      color = Color.White,
      fontWeight = FontWeight.Bold,
      fontSize = fontSize.sp
    )
  }
}

@Composable
fun GeofenceRadar(
  distanceMeters: Int,
  accuracyMeters: Float,
  isWithin: Boolean,
  onToggleTestDistance: (Boolean) -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = if (isWithin) Color(0xFFF0FDF4) else Color(0xFFFEF2F2)),
    border = androidx.compose.foundation.BorderStroke(1.dp, if (isWithin) Color(0xFF86EFAC) else Color(0xFFFCA5A5)),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = if (isWithin) Icons.Default.GpsFixed else Icons.Default.GpsNotFixed,
            contentDescription = "GPS Status",
            tint = if (isWithin) SjSuccess else SjDanger,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (isWithin) "GPS VALID (Radius Kantor)" else "DI LUAR RADIUS (GPS DITOLAK)",
            fontWeight = FontWeight.Bold,
            fontSize = 12.5.sp,
            color = if (isWithin) SjSuccess else SjDanger
          )
        }
        Text(
          text = "±${accuracyMeters.toInt()}m",
          fontSize = 11.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Progress bar towards office (100m radius limit)
      val progress = (1f - (distanceMeters / 100f)).coerceIn(0f, 1f)
      LinearProgressIndicator(
        progress = { if (isWithin) progress else 0.05f },
        modifier = Modifier
          .fillMaxWidth()
          .height(8.dp)
          .clip(RoundedCornerShape(99.dp)),
        color = if (isWithin) SjSuccess else SjDanger,
        trackColor = Color(0xFFE2E8F0)
      )

      Spacer(modifier = Modifier.height(8.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Jarak kantor: ${distanceMeters}m / 100m geofence",
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium,
          color = MaterialTheme.colorScheme.onSurface
        )

        // Quick simulation helper for emulator/browser testing
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "Simulasi GPS:",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.width(4.dp))
          FilterChip(
            selected = isWithin,
            onClick = { onToggleTestDistance(true) },
            label = { Text("18m (Valid)", fontSize = 10.sp) },
            modifier = Modifier.height(28.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          FilterChip(
            selected = !isWithin,
            onClick = { onToggleTestDistance(false) },
            label = { Text("350m", fontSize = 10.sp) },
            modifier = Modifier.height(28.dp)
          )
        }
      }
    }
  }
}

@Composable
fun AttendanceActivityChart(
  attendances: List<AttendanceEntity>,
  modifier: Modifier = Modifier
) {
  val recent = attendances.take(14).reversed()
  if (recent.isEmpty()) {
    Box(
      modifier = modifier
        .fillMaxWidth()
        .height(180.dp),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = "Belum ada riwayat aktivitas presensi",
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 13.sp
      )
    }
    return
  }

  val onTimeColor = SjPrimary
  val lateColor = SjWarning
  val gridColor = Color(0xFFE2E8F0)

  Column(modifier = modifier) {
    Canvas(
      modifier = Modifier
        .fillMaxWidth()
        .height(180.dp)
        .padding(horizontal = 8.dp, vertical = 12.dp)
    ) {
      val w = size.width
      val h = size.height
      val barCount = recent.size
      val slotWidth = w / barCount
      val barWidth = (slotWidth * 0.55f).coerceAtMost(24.dp.toPx())
      val maxHours = 12f

      // Draw grid lines
      for (i in 0..3) {
        val y = h - (h / 3f) * i
        drawLine(
          color = gridColor,
          start = Offset(0f, y),
          end = Offset(w, y),
          strokeWidth = 1.dp.toPx()
        )
      }

      // Draw bars
      recent.forEachIndexed { index, att ->
        val xCenter = index * slotWidth + (slotWidth / 2f)
        val hours = att.durasiJam.toFloat().coerceIn(0f, maxHours)
        val barHeight = (hours / maxHours) * (h - 20.dp.toPx())
        val topY = h - barHeight
        val isLate = att.status.equals("terlambat", ignoreCase = true)
        val color = if (isLate) lateColor else onTimeColor

        drawRoundRect(
          color = color,
          topLeft = Offset(xCenter - barWidth / 2f, topY),
          size = Size(barWidth, barHeight),
          cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx())
        )
      }
    }

    // Days label
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      recent.forEach { att ->
        val dayNumber = try {
          att.tanggal.split("-").last()
        } catch (_: Exception) {
          "•"
        }
        Text(
          text = dayNumber,
          fontSize = 10.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontWeight = FontWeight.Bold,
          textAlign = TextAlign.Center
        )
      }
    }

    Spacer(modifier = Modifier.height(10.dp))
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(onTimeColor))
      Spacer(modifier = Modifier.width(6.dp))
      Text("Tepat Waktu", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
      Spacer(modifier = Modifier.width(16.dp))
      Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(lateColor))
      Spacer(modifier = Modifier.width(6.dp))
      Text("Terlambat", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
  }
}

@Composable
fun CompanyAnalyticsDonut(
  presentOnTime: Int,
  late: Int,
  absentOrPending: Int,
  modifier: Modifier = Modifier
) {
  val total = (presentOnTime + late + absentOrPending).coerceAtLeast(1)
  val onTimeColor = SjSuccess
  val lateColor = SjWarning
  val absentColor = Color(0xFFCBD5E1)

  Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
    Box(
      modifier = Modifier.size(140.dp),
      contentAlignment = Alignment.Center
    ) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val stroke = Stroke(width = 24.dp.toPx(), cap = StrokeCap.Round)
        val diameter = size.minDimension - stroke.width
        val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
        val arcSize = Size(diameter, diameter)

        val onTimeSweep = (presentOnTime.toFloat() / total) * 360f
        val lateSweep = (late.toFloat() / total) * 360f
        val absentSweep = (absentOrPending.toFloat() / total) * 360f

        var currentAngle = -90f
        if (onTimeSweep > 0) {
          drawArc(onTimeColor, currentAngle, onTimeSweep - 2f, false, topLeft, arcSize, style = stroke)
          currentAngle += onTimeSweep
        }
        if (lateSweep > 0) {
          drawArc(lateColor, currentAngle, lateSweep - 2f, false, topLeft, arcSize, style = stroke)
          currentAngle += lateSweep
        }
        if (absentSweep > 0) {
          drawArc(absentColor, currentAngle, absentSweep - 2f, false, topLeft, arcSize, style = stroke)
        }
      }
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
          text = "${presentOnTime + late}",
          fontWeight = FontWeight.Black,
          fontSize = 24.sp
        )
        Text(
          text = "Hadir",
          fontSize = 11.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    Spacer(modifier = Modifier.height(14.dp))
    Row(
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(onTimeColor))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Tepat: $presentOnTime", fontSize = 11.sp)
      }
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(lateColor))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Telat: $late", fontSize = 11.sp)
      }
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(absentColor))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Belum: $absentOrPending", fontSize = 11.sp)
      }
    }
  }
}
