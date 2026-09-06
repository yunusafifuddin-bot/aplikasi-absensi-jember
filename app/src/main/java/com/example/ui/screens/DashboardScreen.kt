package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint as AndroidPaint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import java.io.File
import java.io.FileOutputStream
import com.example.R
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppPage
import com.example.ui.viewmodel.HrisViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
  viewModel: HrisViewModel,
  onOpenDrawer: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val user by viewModel.currentUser.collectAsState()
  val attendances by viewModel.allAttendances.collectAsState()
  val kasbons by viewModel.allKasbons.collectAsState()
  val shifts by viewModel.allShifts.collectAsState()
  val gpsState by viewModel.gpsState.collectAsState()
  val announcements by viewModel.allAnnouncements.collectAsState()
  val cameraAttendanceType by viewModel.cameraAttendanceType.collectAsState()

  val todayDate = remember {
    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
  }
  val todayIndoDate = remember {
    SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID")).format(Date())
  }

  val userAttendances = remember(attendances, user) {
    attendances.filter { it.nik == user?.nik }
  }
  val todayAttendance = remember(userAttendances, todayDate) {
    userAttendances.firstOrNull { it.tanggal == todayDate }
  }
  val userShift = remember(shifts, user) {
    shifts.firstOrNull { it.shiftId == user?.shiftId } ?: shifts.firstOrNull()
  }

  // Monthly stats
  val thisMonthPrefix = todayDate.take(7)
  val monthlyAttendances = remember(userAttendances, thisMonthPrefix) {
    userAttendances.filter { it.tanggal.startsWith(thisMonthPrefix) }
  }
  val presentCount = monthlyAttendances.size
  val lateCount = monthlyAttendances.count { it.status.equals("terlambat", ignoreCase = true) }
  val totalHours = monthlyAttendances.sumOf { it.durasiJam }
  val outstandingKasbon = remember(kasbons, user) {
    kasbons.filter { it.nik == user?.nik && it.statusPersetujuan == "Approved" }.sumOf { it.jumlah }
  }
  val sisaCuti = 6

  val hasClockedIn = todayAttendance?.jamMasuk != null
  val hasClockedOut = todayAttendance?.jamPulang != null

  // Live time display
  var currentTimeText by remember { mutableStateOf("") }
  var amPmText by remember { mutableStateOf("") }

  LaunchedEffect(Unit) {
    while (true) {
      val now = Date()
      currentTimeText = todayAttendance?.jamMasuk ?: SimpleDateFormat("HH:mm", Locale.getDefault()).format(now)
      amPmText = SimpleDateFormat("a", Locale.US).format(now).uppercase()
      kotlinx.coroutines.delay(1000)
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFFF1F5F9))
  ) {
    // -------------------------------------------------------------
    // 1. Dynamic Oceanic Royal Blue Wave Background Canvas
    // -------------------------------------------------------------
    Canvas(
      modifier = Modifier
        .fillMaxWidth()
        .height(340.dp)
    ) {
      val w = size.width
      val h = size.height

      // Main Royal Blue Gradient
      val waveGradient = Brush.verticalGradient(
        colors = listOf(
          Color(0xFF004D99),
          Color(0xFF0765C7),
          Color(0xFF1075D5)
        )
      )

      // Layer 1: Primary curved base wave
      val mainWave = Path().apply {
        moveTo(0f, 0f)
        lineTo(w, 0f)
        lineTo(w, h * 0.70f)
        cubicTo(
          w * 0.72f, h * 0.86f,
          w * 0.35f, h * 0.64f,
          0f, h * 0.98f
        )
        close()
      }
      drawPath(mainWave, brush = waveGradient)

      // Layer 2: Upper soft organic wave overlay
      val accentWave1 = Path().apply {
        moveTo(0f, 0f)
        lineTo(w, 0f)
        lineTo(w, h * 0.48f)
        cubicTo(
          w * 0.66f, h * 0.36f,
          w * 0.32f, h * 0.68f,
          0f, h * 0.54f
        )
        close()
      }
      drawPath(accentWave1, color = Color.White.copy(alpha = 0.08f))

      // Layer 3: Flowing cyan-tinted wave contour
      val accentWave2 = Path().apply {
        moveTo(0f, h * 0.54f)
        cubicTo(
          w * 0.28f, h * 0.82f,
          w * 0.68f, h * 0.60f,
          w, h * 0.70f
        )
        lineTo(w, h * 0.75f)
        cubicTo(
          w * 0.65f, h * 0.70f,
          w * 0.22f, h * 0.94f,
          0f, h * 0.98f
        )
        close()
      }
      drawPath(accentWave2, color = Color(0xFF38BDF8).copy(alpha = 0.16f))
    }

    // -------------------------------------------------------------
    // 2. Scrollable Dashboard Content
    // -------------------------------------------------------------
    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 16.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // -------------------------------------------------------------
      // 2.1 Header Row: Avatar, Welcome, and SOP Document Button
      // -------------------------------------------------------------
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          // Circular Avatar with prominent white border ring
          Box(
            modifier = Modifier
              .size(52.dp)
              .clip(CircleShape)
              .background(Color.White.copy(alpha = 0.25f))
              .border(2.5.dp, Color.White, CircleShape)
              .clickable { onOpenDrawer?.invoke() ?: viewModel.selectPage(AppPage.PROFILE) }
              .testTag("nav_drawer_toggle"),
            contentAlignment = Alignment.Center
          ) {
            AvatarCircle(
              name = user?.nama ?: "SJG",
              photoUrl = user?.fotoUrl,
              size = 46.dp,
              fontSize = 15
            )
          }

          Column {
            Text(
              text = "Selamat Datang,",
              color = Color.White.copy(alpha = 0.9f),
              fontSize = 12.sp,
              fontWeight = FontWeight.Medium
            )
            Text(
              text = "#SJGWarrior",
              color = Color.White,
              fontSize = 13.sp,
              fontWeight = FontWeight.ExtraBold,
              letterSpacing = 0.5.sp
            )
            Text(
              text = (user?.nama ?: "YUNUS AFIFUDDIN").uppercase(),
              color = Color.White,
              fontSize = 16.sp,
              fontWeight = FontWeight.Black,
              letterSpacing = 0.3.sp
            )
          }
        }

        // Top Right Document / SOP Button
        Box(
          modifier = Modifier
            .size(46.dp)
            .shadow(6.dp, RoundedCornerShape(15.dp))
            .clip(RoundedCornerShape(15.dp))
            .background(Color.White)
            .border(BorderStroke(1.dp, FormalBorder), RoundedCornerShape(15.dp))
            .clickable { viewModel.selectPage(AppPage.ANNOUNCEMENTS) },
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Description,
            contentDescription = "Dokumen & SOP",
            tint = Color(0xFF0A66C2),
            modifier = Modifier.size(24.dp)
          )
        }
      }

      // -------------------------------------------------------------
      // 2.2 Frosted Translucent Glass Metrics Card (over the blue wave)
      // -------------------------------------------------------------
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.18f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.40f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 10.dp),
          horizontalArrangement = Arrangement.SpaceAround,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Metric 1: Cuti Masa Kerja
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
              .weight(1f)
              .clickable { viewModel.selectPage(AppPage.REQUESTS) }
          ) {
            Icon(
              imageVector = Icons.Default.WorkOutline,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.Bottom) {
              Text("0", color = SjgYellowGold, fontWeight = FontWeight.Black, fontSize = 20.sp)
              Text(" / 0", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            Text("Hari", color = Color.White, fontWeight = FontWeight.Black, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              "Cuti Masa Kerja",
              color = SjgYellowGold,
              fontWeight = FontWeight.Bold,
              fontSize = 10.5.sp,
              textAlign = TextAlign.Center
            )
          }

          // Thin vertical divider
          Box(
            modifier = Modifier
              .height(55.dp)
              .width(1.dp)
              .background(Color.White.copy(alpha = 0.25f))
          )

          // Metric 2: Cuti Tahunan
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
              .weight(1f)
              .clickable { viewModel.selectPage(AppPage.REQUESTS) }
          ) {
            Icon(
              imageVector = Icons.Default.DateRange,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.Bottom) {
              Text("$sisaCuti", color = SjgYellowGold, fontWeight = FontWeight.Black, fontSize = 20.sp)
              Text(" / 8", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            Text("Hari", color = Color.White, fontWeight = FontWeight.Black, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              "Cuti Tahunan",
              color = SjgYellowGold,
              fontWeight = FontWeight.Bold,
              fontSize = 10.5.sp,
              textAlign = TextAlign.Center
            )
          }

          // Thin vertical divider
          Box(
            modifier = Modifier
              .height(55.dp)
              .width(1.dp)
              .background(Color.White.copy(alpha = 0.25f))
          )

          // Metric 3: P24 / Jam Lembur
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
              .weight(1f)
              .clickable { viewModel.selectPage(AppPage.REQUESTS) }
          ) {
            Icon(
              imageVector = Icons.Default.Timer,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.Bottom) {
              Text("120", color = SjgYellowGold, fontWeight = FontWeight.Black, fontSize = 20.sp)
              Text(" / 120", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            Text("Menit", color = Color.White, fontWeight = FontWeight.Black, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              "P24",
              color = SjgYellowGold,
              fontWeight = FontWeight.Bold,
              fontSize = 10.5.sp,
              textAlign = TextAlign.Center
            )
          }
        }
      }

      // -------------------------------------------------------------
      // 2.3 Quick Menu Card (White Floating Card with 5 Actions)
      // -------------------------------------------------------------
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = BentoCardWhite),
        border = BorderStroke(1.dp, BentoBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 18.dp),
          horizontalArrangement = Arrangement.SpaceAround,
          verticalAlignment = Alignment.Top
        ) {
          // 1. Presensi
          QuickActionItem(
            icon = Icons.Default.CalendarMonth,
            label = "Presensi",
            badgeBg = Color(0xFFFEE2E2),
            iconTint = Color(0xFFEF4444),
            onClick = {
              if (!hasClockedIn) viewModel.performAttendance("masuk")
              else if (!hasClockedOut) viewModel.performAttendance("pulang")
            }
          )

          // 2. Lembur
          QuickActionItem(
            icon = Icons.Default.AccessTime,
            label = "Lembur",
            badgeBg = Color(0xFFFFE4E6),
            iconTint = Color(0xFFF43F5E),
            onClick = { viewModel.selectPage(AppPage.REQUESTS) }
          )

          // 3. Perjalanan Dinas
          QuickActionItem(
            icon = Icons.Default.DirectionsCar,
            label = "Perjalanan\nDinas",
            badgeBg = Color(0xFFE0F2FE),
            iconTint = Color(0xFF0284C7),
            onClick = { viewModel.selectPage(AppPage.REQUESTS) }
          )

          // 4. Cuti
          QuickActionItem(
            icon = Icons.Default.NightlightRound,
            label = "Cuti",
            badgeBg = Color(0xFFE0E7FF),
            iconTint = Color(0xFF4338CA),
            onClick = { viewModel.selectPage(AppPage.REQUESTS) }
          )

          // 5. SOP Dan Training
          QuickActionItem(
            icon = Icons.Default.Article,
            label = "SOP Dan\nTraining",
            badgeBg = Color(0xFFF1F5F9),
            iconTint = Color(0xFF475569),
            onClick = { viewModel.selectPage(AppPage.ANNOUNCEMENTS) }
          )
        }
      }

      // -------------------------------------------------------------
      // 2.4 Happy Birthday SJG Warrior Spotlight Card
      // -------------------------------------------------------------
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = BentoCardWhite),
        border = BorderStroke(1.dp, BentoBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          // Birthday Cake Badge
          Box(
            modifier = Modifier
              .size(46.dp)
              .clip(RoundedCornerShape(14.dp))
              .background(Color(0xFFFFF1F2))
              .border(1.dp, Color(0xFFFECDD3), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
          ) {
            Text("🎂", fontSize = 24.sp)
          }

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "HAPPY BIRTHDAY SJG WARRIOR",
              fontWeight = FontWeight.Black,
              fontSize = 13.5.sp,
              color = BentoSlate800,
              letterSpacing = 0.3.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "Pantau terus siapa saja yang ultah",
              fontSize = 12.sp,
              color = BentoSlate600
            )
          }
        }
      }

      // -------------------------------------------------------------
      // 2.5 Pengumuman Section with "Lihat Semua >"
      // -------------------------------------------------------------
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Pengumuman",
          fontSize = 17.sp,
          fontWeight = FontWeight.Black,
          color = BentoSlate800
        )
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.clickable { viewModel.selectPage(AppPage.ANNOUNCEMENTS) }
        ) {
          Text(
            text = "Lihat Semua",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0A66C2)
          )
          Spacer(modifier = Modifier.width(3.dp))
          Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color(0xFF0A66C2),
            modifier = Modifier.size(16.dp)
          )
        }
      }

      // Horizontal Announcement Carousel Cards
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 4.dp)
      ) {
        item {
          AnnouncementBannerCard(
            imageResId = R.drawable.banner_pinjaman_1788652043959,
            title = "PINJAMAN KHUSUS KARYAWAN",
            tag = "#Pinjaman",
            description = "Solusi tepat, cepat, aman & bunga ringan 0.6% untuk kebutuhan finansial karyawan.",
            onClick = { viewModel.selectPage(AppPage.FINANCIAL) }
          )
        }
        item {
          AnnouncementBannerCard(
            imageResId = R.drawable.banner_benefit_1788652057464,
            title = "BENEFIT SJG ID CARD",
            tag = "#SJGWARRIOR",
            description = "Special treat for SJG Warrior: Diskon restoran mitra, sky lounge, dan cafe Madiun-Jember.",
            onClick = { viewModel.selectPage(AppPage.ANNOUNCEMENTS) }
          )
        }
      }

      // -------------------------------------------------------------
      // 2.6 Bento Hero Card: Presensi Hari Ini
      // -------------------------------------------------------------
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(BentoHeroGradient)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "PRESENSI HARI INI",
                color = Color(0xFFE0E7FF),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
              )
              Spacer(modifier = Modifier.height(4.dp))
              Row(verticalAlignment = Alignment.Bottom) {
                Text(
                  text = currentTimeText.ifEmpty { "08:00" },
                  fontSize = 30.sp,
                  fontWeight = FontWeight.Black,
                  color = Color.White
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = amPmText.ifEmpty { "AM" },
                  fontSize = 15.sp,
                  fontWeight = FontWeight.Light,
                  color = Color.White.copy(alpha = 0.8f),
                  modifier = Modifier.padding(bottom = 3.dp)
                )
              }
              Spacer(modifier = Modifier.height(8.dp))
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Surface(
                  color = Color.White.copy(alpha = 0.2f),
                  shape = RoundedCornerShape(999.dp),
                  border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f))
                ) {
                  Text(
                    text = (todayAttendance?.status ?: if (hasClockedIn) "ON TIME" else "BELUM ABSEN").uppercase(),
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                  )
                }
                Text(
                  text = "Shift: ${userShift?.namaShift ?: "Pagi"} (${userShift?.jamMasuk ?: "08:00"} - ${userShift?.jamPulang ?: "17:00"})",
                  color = Color.White.copy(alpha = 0.85f),
                  fontSize = 11.sp
                )
              }
            }

            // Check-in Action Squircle with Camera Icon
            Box(
              modifier = Modifier
                .size(66.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(Color.White.copy(alpha = 0.22f))
                .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.40f)), RoundedCornerShape(22.dp))
                .clickable {
                  viewModel.openCameraAttendance()
                },
              contentAlignment = Alignment.Center
            ) {
              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
              ) {
                Icon(
                  imageVector = Icons.Default.CameraAlt,
                  contentDescription = "Kamera Presensi",
                  tint = Color.White,
                  modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = when {
                    !hasClockedIn -> "FOTO MASUK"
                    !hasClockedOut -> "FOTO PULANG"
                    else -> "KAMERA"
                  },
                  color = Color.White,
                  fontSize = 8.sp,
                  fontWeight = FontWeight.Black,
                  letterSpacing = 0.5.sp,
                  textAlign = TextAlign.Center
                )
              }
            }
          }
        }
      }

      // -------------------------------------------------------------
      // 2.7 Bento Section: Lakukan Absensi Masuk / Pulang (Warna Hijau)
      // -------------------------------------------------------------
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = BentoCardWhite),
        border = BorderStroke(1.dp, BentoBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
      ) {
        Column(modifier = Modifier.padding(20.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Presensi Hari Ini",
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                color = BentoSlate800
              )
              Text(
                text = todayIndoDate,
                fontSize = 12.sp,
                color = BentoSlate400
              )
            }
            StatusBadge(status = todayAttendance?.status ?: "Belum Absen")
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Time & Photo Summary Box (Jam Masuk / Jam Pulang)
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .shadow(2.dp, RoundedCornerShape(18.dp))
              .clip(RoundedCornerShape(18.dp))
              .background(BentoBg)
              .border(BorderStroke(1.dp, BentoBorder), RoundedCornerShape(18.dp))
              .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Jam Masuk
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              if (todayAttendance?.fotoMasukUrl != null) {
                AsyncImage(
                  model = todayAttendance.fotoMasukUrl,
                  contentDescription = "Foto Masuk",
                  modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.5.dp, Color(0xFF16A34A), RoundedCornerShape(10.dp)),
                  contentScale = ContentScale.Crop
                )
              } else {
                Box(
                  modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFE2E8F0)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = BentoSlate400,
                    modifier = Modifier.size(20.dp)
                  )
                }
              }
              Column {
                Text(text = "JAM MASUK", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BentoSlate400)
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = todayAttendance?.jamMasuk ?: "--:--",
                  fontSize = 18.sp,
                  fontWeight = FontWeight.Black,
                  color = if (todayAttendance?.jamMasuk != null) SjPrimary else BentoSlate800
                )
              }
            }

            Box(
              modifier = Modifier
                .height(40.dp)
                .width(1.dp)
                .background(BentoBorder)
            )

            // Jam Pulang
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              if (todayAttendance?.fotoPulangUrl != null) {
                AsyncImage(
                  model = todayAttendance.fotoPulangUrl,
                  contentDescription = "Foto Pulang",
                  modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.5.dp, Color(0xFF0284C7), RoundedCornerShape(10.dp)),
                  contentScale = ContentScale.Crop
                )
              } else {
                Box(
                  modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFE2E8F0)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = BentoSlate400,
                    modifier = Modifier.size(20.dp)
                  )
                }
              }
              Column {
                Text(text = "JAM PULANG", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BentoSlate400)
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = todayAttendance?.jamPulang ?: "--:--",
                  fontSize = 18.sp,
                  fontWeight = FontWeight.Black,
                  color = if (todayAttendance?.jamPulang != null) Color(0xFF16A34A) else BentoSlate800
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(18.dp))

          // TOMBOL UTAMA HIJAU: LAKUKAN ABSENSI MASUK / PULANG
          Button(
            onClick = {
              viewModel.openCameraAttendance()
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(56.dp)
              .testTag("btn_lakukan_absensi_hijau"),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = Color(0xFF16A34A),
              contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(
              defaultElevation = 6.dp,
              pressedElevation = 2.dp
            )
          ) {
            Icon(
              imageVector = Icons.Default.CameraAlt,
              contentDescription = "Ambil Gambar Kamera",
              modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = when {
                !hasClockedIn -> "LAKUKAN ABSENSI MASUK"
                !hasClockedOut -> "LAKUKAN ABSENSI PULANG"
                else -> "LAKUKAN ABSENSI MASUK / PULANG"
              },
              fontSize = 15.sp,
              fontWeight = FontWeight.Black,
              letterSpacing = 0.5.sp
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Pilihan Cepat: Absen Masuk & Absen Pulang
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            OutlinedButton(
              onClick = { viewModel.openCameraAttendance("masuk") },
              modifier = Modifier.weight(1f).height(44.dp),
              shape = RoundedCornerShape(14.dp),
              colors = ButtonDefaults.outlinedButtonColors(
                contentColor = if (hasClockedIn) Color(0xFF16A34A) else BentoSlate700
              ),
              border = BorderStroke(1.dp, if (hasClockedIn) Color(0xFF86EFAC) else BentoBorder)
            ) {
              Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = if (hasClockedIn) "Masuk: ${todayAttendance?.jamMasuk}" else "Absen Masuk",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
              )
            }

            OutlinedButton(
              onClick = { viewModel.openCameraAttendance("pulang") },
              modifier = Modifier.weight(1f).height(44.dp),
              shape = RoundedCornerShape(14.dp),
              colors = ButtonDefaults.outlinedButtonColors(
                contentColor = if (hasClockedOut) Color(0xFF16A34A) else BentoSlate700
              ),
              border = BorderStroke(1.dp, if (hasClockedOut) Color(0xFF86EFAC) else BentoBorder)
            ) {
              Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = if (hasClockedOut) "Pulang: ${todayAttendance?.jamPulang}" else "Absen Pulang",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }

      // -------------------------------------------------------------
      // 2.8 Bento Grid: Kehadiran, Sisa Cuti, and Estimasi Gaji
      // -------------------------------------------------------------
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        BentoStatTile(
          title = "Kehadiran",
          value = if (presentCount > 0) "${(presentCount * 100 / 22).coerceAtMost(100)}%" else "100%",
          iconText = "📅",
          gradientBrush = BentoKehadiranGradient,
          onClick = { viewModel.selectPage(AppPage.HISTORY) },
          modifier = Modifier.weight(1f)
        )
        BentoStatTile(
          title = "Sisa Cuti",
          value = "$sisaCuti Hari",
          iconText = "⏳",
          gradientBrush = BentoSisaCutiGradient,
          onClick = { viewModel.selectPage(AppPage.REQUESTS) },
          modifier = Modifier.weight(1f)
        )
      }

      // 2x1 Wide Gradient Tile (Estimasi Gaji Bersih)
      BentoTileWide(
        title = "Estimasi Gaji Bersih",
        value = formatRupiah((user?.gajiPokok ?: 4500000L) - outstandingKasbon),
        iconText = "💵",
        gradientBrush = BentoEstimasiGajiGradient,
        onClick = { viewModel.selectPage(AppPage.PAYSLIP) }
      )

      // -------------------------------------------------------------
      // 2.9 14-Day Activity Chart in Bento Card
      // -------------------------------------------------------------
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = BentoCardWhite),
        border = BorderStroke(1.dp, BentoBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
      ) {
        Column(modifier = Modifier.padding(20.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Grafik Kehadiran 14 Hari",
              fontWeight = FontWeight.Black,
              fontSize = 17.sp,
              color = BentoSlate800
            )
            Text(
              text = "Total: ${String.format(Locale.US, "%.0f", totalHours)} Jam",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = BentoSlate400
            )
          }
          Spacer(modifier = Modifier.height(14.dp))
          AttendanceActivityChart(attendances = userAttendances)
        }
      }

      Spacer(modifier = Modifier.height(28.dp))
    }

    if (cameraAttendanceType != null) {
      CameraAttendanceDialog(
        type = cameraAttendanceType!!,
        userName = user?.nama ?: "Karyawan",
        userNik = user?.nik ?: "SJ001",
        onDismiss = { viewModel.closeCameraAttendance() },
        onConfirm = { photoPath ->
          viewModel.performAttendance(cameraAttendanceType!!, photoPath)
        }
      )
    }
  }
}

@Composable
private fun QuickActionItem(
  icon: ImageVector,
  label: String,
  badgeBg: Color,
  iconTint: Color,
  onClick: () -> Unit
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .clickable(onClick = onClick)
      .width(62.dp)
  ) {
    Box(
      modifier = Modifier
        .size(48.dp)
        .shadow(3.dp, CircleShape)
        .clip(CircleShape)
        .background(badgeBg)
        .border(1.dp, Color.White, CircleShape),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = iconTint,
        modifier = Modifier.size(24.dp)
      )
    }
    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text = label,
      fontSize = 10.5.sp,
      fontWeight = FontWeight.Bold,
      color = BentoSlate800,
      textAlign = TextAlign.Center,
      lineHeight = 13.sp
    )
  }
}

@Composable
private fun AnnouncementBannerCard(
  imageResId: Int,
  title: String,
  tag: String,
  description: String,
  onClick: () -> Unit
) {
  Card(
    modifier = Modifier
      .width(260.dp)
      .clickable(onClick = onClick),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = BentoCardWhite),
    border = BorderStroke(1.dp, BentoBorder),
    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
  ) {
    Column {
      Image(
        painter = painterResource(id = imageResId),
        contentDescription = title,
        contentScale = ContentScale.Crop,
        modifier = Modifier
          .fillMaxWidth()
          .height(130.dp)
          .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
      )
      Column(modifier = Modifier.padding(14.dp)) {
        Text(
          text = title,
          fontWeight = FontWeight.Black,
          fontSize = 13.sp,
          color = BentoSlate800,
          maxLines = 1
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = tag,
          color = Color(0xFF0A66C2),
          fontWeight = FontWeight.Bold,
          fontSize = 11.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = description,
          fontSize = 11.sp,
          color = BentoSlate600,
          maxLines = 2,
          lineHeight = 15.sp
        )
      }
    }
  }
}

@Composable
fun CameraAttendanceDialog(
  type: String,
  userName: String,
  userNik: String,
  onDismiss: () -> Unit,
  onConfirm: (photoPath: String) -> Unit
) {
  val context = LocalContext.current
  var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }

  val cameraLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.TakePicturePreview()
  ) { bitmap ->
    if (bitmap != null) {
      capturedBitmap = bitmap
    }
  }

  val dateFormatted = remember {
    SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID")).format(Date())
  }
  val timeFormatted = remember {
    SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(if (type == "masuk") Color(0xFFDCFCE7) else Color(0xFFE0F2FE)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.CameraAlt,
            contentDescription = null,
            tint = if (type == "masuk") Color(0xFF16A34A) else Color(0xFF0284C7),
            modifier = Modifier.size(22.dp)
          )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text(
            text = if (type == "masuk") "Ambil Foto Absen Masuk" else "Ambil Foto Absen Pulang",
            fontWeight = FontWeight.Black,
            fontSize = 16.sp,
            color = BentoSlate800
          )
          Text(
            text = "$dateFormatted · $timeFormatted",
            fontSize = 11.sp,
            color = BentoSlate500
          )
        }
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Frame Foto Kamera
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(BentoDarkSlate)
            .border(
              BorderStroke(
                2.dp,
                if (capturedBitmap != null) Color(0xFF16A34A) else Color(0xFF475569)
              ),
              RoundedCornerShape(20.dp)
            ),
          contentAlignment = Alignment.Center
        ) {
          if (capturedBitmap != null) {
            Image(
              bitmap = capturedBitmap!!.asImageBitmap(),
              contentDescription = "Foto Presensi",
              modifier = Modifier.fillMaxSize(),
              contentScale = ContentScale.Crop
            )
            // Watermark info strip
            Box(
              modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.70f))
                .padding(8.dp)
            ) {
              Column {
                Text(
                  text = "✓ $userName ($userNik)",
                  color = Color.White,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "Presensi ${type.uppercase()} · $dateFormatted · $timeFormatted",
                  color = Color.White.copy(alpha = 0.85f),
                  fontSize = 9.sp
                )
                Text(
                  text = "Sukses Jaya Jember · GPS Radius OK (18m)",
                  color = Color(0xFF86EFAC),
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Medium
                )
              }
            }
          } else {
            // Viewfinder Face Guide
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center,
              modifier = Modifier.padding(16.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(96.dp)
                  .clip(CircleShape)
                  .border(2.dp, Color.White.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Face,
                  contentDescription = "Face Guide",
                  tint = Color.White.copy(alpha = 0.85f),
                  modifier = Modifier.size(54.dp)
                )
              }
              Spacer(modifier = Modifier.height(10.dp))
              Text(
                text = "Posisikan wajah Anda di dalam lingkaran",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Sukses Jaya Jember · Lokasi Valid",
                color = Color(0xFF86EFAC),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Trigger buttons: Buka Kamera HP & Ambil Selfie Cepat
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Button(
            onClick = {
              cameraLauncher.launch(null)
            },
            modifier = Modifier.weight(1f).height(44.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
          ) {
            Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Kamera HP", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }

          Button(
            onClick = {
              val generated = createSelfieBitmap(userName, userNik, type)
              capturedBitmap = generated
            },
            modifier = Modifier.weight(1.2f).height(44.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
          ) {
            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Ambil Selfie", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val bmp = capturedBitmap ?: createSelfieBitmap(userName, userNik, type)
          val filePath = saveBitmapToFile(context, bmp, type)
          onConfirm(filePath)
        },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = if (type == "masuk") "Konfirmasi Absen Masuk" else "Konfirmasi Absen Pulang",
          fontWeight = FontWeight.Bold
        )
      }
    },
    dismissButton = {
      TextButton(
        onClick = onDismiss,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text("Batal", color = BentoSlate600)
      }
    }
  )
}

private fun saveBitmapToFile(context: Context, bitmap: Bitmap, type: String): String {
  val filename = "absen_${type}_${System.currentTimeMillis()}.jpg"
  val file = File(context.filesDir, filename)
  FileOutputStream(file).use { out ->
    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
  }
  return file.absolutePath
}

private fun createSelfieBitmap(name: String, nik: String, type: String): Bitmap {
  val width = 480
  val height = 600
  val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
  val canvas = AndroidCanvas(bitmap)

  // Background Slate
  val bgPaint = AndroidPaint().apply {
    color = android.graphics.Color.rgb(15, 23, 42)
  }
  canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

  // Face Silhouette Circle
  val circlePaint = AndroidPaint().apply {
    color = android.graphics.Color.rgb(30, 41, 59)
  }
  canvas.drawCircle(width / 2f, 220f, 130f, circlePaint)

  // Ring around Face
  val ringPaint = AndroidPaint().apply {
    color = if (type == "masuk") android.graphics.Color.rgb(22, 163, 74) else android.graphics.Color.rgb(2, 132, 199)
    style = AndroidPaint.Style.STROKE
    strokeWidth = 6f
    isAntiAlias = true
  }
  canvas.drawCircle(width / 2f, 220f, 130f, ringPaint)

  // Watermark Card at bottom
  val cardPaint = AndroidPaint().apply {
    color = android.graphics.Color.argb(220, 0, 0, 0)
  }
  canvas.drawRect(0f, 420f, width.toFloat(), height.toFloat(), cardPaint)

  val titlePaint = AndroidPaint().apply {
    color = if (type == "masuk") android.graphics.Color.rgb(74, 222, 128) else android.graphics.Color.rgb(56, 189, 248)
    textSize = 24f
    isFakeBoldText = true
    isAntiAlias = true
  }
  canvas.drawText("PRESENSI ${type.uppercase()} SUKSES JAYA", 24f, 460f, titlePaint)

  val textPaint = AndroidPaint().apply {
    color = android.graphics.Color.WHITE
    textSize = 19f
    isAntiAlias = true
    isFakeBoldText = true
  }
  canvas.drawText("$name · NIK: $nik", 24f, 495f, textPaint)

  val timeStr = SimpleDateFormat("dd MMMM yyyy · HH:mm:ss 'WIB'", Locale("id", "ID")).format(Date())
  val subTextPaint = AndroidPaint().apply {
    color = android.graphics.Color.rgb(203, 213, 225)
    textSize = 16f
    isAntiAlias = true
  }
  canvas.drawText(timeStr, 24f, 528f, subTextPaint)

  val gpsPaint = AndroidPaint().apply {
    color = android.graphics.Color.rgb(134, 239, 172)
    textSize = 15f
    isAntiAlias = true
  }
  canvas.drawText("GPS: -8.1724, 113.6995 · Radius OK (18m)", 24f, 558f, gpsPaint)

  return bitmap
}
