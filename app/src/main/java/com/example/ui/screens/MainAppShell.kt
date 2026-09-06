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
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AvatarCircle
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppPage
import com.example.ui.viewmodel.HrisViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppShell(
  viewModel: HrisViewModel,
  modifier: Modifier = Modifier
) {
  val currentUser by viewModel.currentUser.collectAsState()
  val currentPage by viewModel.currentPage.collectAsState()
  val gpsState by viewModel.gpsState.collectAsState()
  val employees by viewModel.allEmployees.collectAsState()
  val attendances by viewModel.allAttendances.collectAsState()

  val todayDate = remember {
    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
  }
  val todayAttendance = remember(attendances, currentUser, todayDate) {
    attendances.firstOrNull { it.nik == currentUser?.nik && it.tanggal == todayDate }
  }
  val hasClockedIn = todayAttendance?.jamMasuk != null
  val hasClockedOut = todayAttendance?.jamPulang != null

  val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
  val scope = rememberCoroutineScope()
  val snackbarHostState = remember { SnackbarHostState() }

  // Listen to snackbar messages
  LaunchedEffect(Unit) {
    viewModel.snackbarMessage.collect { msg ->
      snackbarHostState.showSnackbar(msg)
    }
  }

  val isHrOrAdmin = currentUser?.role in listOf("HR", "ADMIN")
  var showUserDropdown by remember { mutableStateOf(false) }

  ModalNavigationDrawer(
    drawerState = drawerState,
    drawerContent = {
      ModalDrawerSheet(
        drawerContainerColor = Color.White,
        drawerContentColor = BentoSlate800,
        modifier = Modifier.width(300.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
        ) {
          // Brand Header
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 8.dp)
          ) {
            Box(
              modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                  Brush.linearGradient(
                    colors = listOf(SjPrimary, SjAccent)
                  )
                ),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "SJ",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 20.sp
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "SUKSES JAYA",
                color = BentoSlate800,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp,
                letterSpacing = 0.5.sp
              )
              Text(
                text = "HRIS ENTERPRISE · JEMBER",
                color = SjPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // User Card Snippet in Sidebar
          Surface(
            color = Color(0xFFF1F5F9),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, FormalBorder),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              AvatarCircle(
                name = currentUser?.nama ?: "SJ",
                photoUrl = currentUser?.fotoUrl,
                size = 40.dp,
                fontSize = 14
              )
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = currentUser?.nama ?: "-",
                  color = BentoSlate800,
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.5.sp,
                  maxLines = 1
                )
                Text(
                  text = "${currentUser?.jabatan} · ${currentUser?.role}",
                  color = BentoSlate600,
                  fontSize = 11.sp,
                  maxLines = 1
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(18.dp))

          Text(
            text = "MENU UTAMA",
            color = BentoSlate400,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 0.8.sp,
            modifier = Modifier.padding(start = 8.dp, bottom = 6.dp)
          )

          SidebarNavItem(
            title = "Dashboard",
            icon = Icons.Default.Dashboard,
            selected = currentPage == AppPage.HOME,
            onClick = {
              viewModel.selectPage(AppPage.HOME)
              scope.launch { drawerState.close() }
            }
          )
          SidebarNavItem(
            title = "Riwayat Absensi",
            icon = Icons.Default.History,
            selected = currentPage == AppPage.HISTORY,
            onClick = {
              viewModel.selectPage(AppPage.HISTORY)
              scope.launch { drawerState.close() }
            }
          )
          SidebarNavItem(
            title = "Kalender Kehadiran",
            icon = Icons.Default.CalendarMonth,
            selected = currentPage == AppPage.CALENDAR,
            onClick = {
              viewModel.selectPage(AppPage.CALENDAR)
              scope.launch { drawerState.close() }
            }
          )
          SidebarNavItem(
            title = "Financial / Kasbon",
            icon = Icons.Default.AccountBalanceWallet,
            selected = currentPage == AppPage.FINANCIAL,
            onClick = {
              viewModel.selectPage(AppPage.FINANCIAL)
              scope.launch { drawerState.close() }
            }
          )
          SidebarNavItem(
            title = "Cuti & Lembur",
            icon = Icons.Default.EventNote,
            selected = currentPage == AppPage.REQUESTS,
            onClick = {
              viewModel.selectPage(AppPage.REQUESTS)
              scope.launch { drawerState.close() }
            }
          )
          SidebarNavItem(
            title = "Slip Gaji",
            icon = Icons.Default.ReceiptLong,
            selected = currentPage == AppPage.PAYSLIP,
            onClick = {
              viewModel.selectPage(AppPage.PAYSLIP)
              scope.launch { drawerState.close() }
            }
          )
          SidebarNavItem(
            title = "Profil & BPJS",
            icon = Icons.Default.Person,
            selected = currentPage == AppPage.PROFILE,
            onClick = {
              viewModel.selectPage(AppPage.PROFILE)
              scope.launch { drawerState.close() }
            }
          )

          if (isHrOrAdmin) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
              text = "HR & OPERASIONAL",
              color = BentoSlate400,
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp,
              letterSpacing = 0.8.sp,
              modifier = Modifier.padding(start = 8.dp, bottom = 6.dp)
            )

            SidebarNavItem(
              title = "HR Dashboard",
              icon = Icons.Default.AdminPanelSettings,
              selected = currentPage == AppPage.ADMIN,
              onClick = {
                viewModel.selectPage(AppPage.ADMIN)
                scope.launch { drawerState.close() }
              }
            )
            SidebarNavItem(
              title = "Karyawan",
              icon = Icons.Default.Group,
              selected = currentPage == AppPage.EMPLOYEES,
              onClick = {
                viewModel.selectPage(AppPage.EMPLOYEES)
                scope.launch { drawerState.close() }
              }
            )
            SidebarNavItem(
              title = "Shift Kerja",
              icon = Icons.Default.WorkHistory,
              selected = currentPage == AppPage.SHIFTS,
              onClick = {
                viewModel.selectPage(AppPage.SHIFTS)
                scope.launch { drawerState.close() }
              }
            )
            SidebarNavItem(
              title = "Payroll",
              icon = Icons.Default.Payments,
              selected = currentPage == AppPage.PAYROLL_ADMIN,
              onClick = {
                viewModel.selectPage(AppPage.PAYROLL_ADMIN)
                scope.launch { drawerState.close() }
              }
            )
            SidebarNavItem(
              title = "Laporan",
              icon = Icons.Default.Assessment,
              selected = currentPage == AppPage.REPORTS,
              onClick = {
                viewModel.selectPage(AppPage.REPORTS)
                scope.launch { drawerState.close() }
              }
            )
            SidebarNavItem(
              title = "Pengumuman",
              icon = Icons.Default.Campaign,
              selected = currentPage == AppPage.ANNOUNCEMENTS,
              onClick = {
                viewModel.selectPage(AppPage.ANNOUNCEMENTS)
                scope.launch { drawerState.close() }
              }
            )
          }

          Spacer(modifier = Modifier.height(24.dp))
          Divider(color = FormalBorder)
          Spacer(modifier = Modifier.height(8.dp))

          SidebarNavItem(
            title = "Keluar Akun",
            icon = Icons.Default.Logout,
            selected = false,
            textColor = SjDanger,
            onClick = {
              scope.launch { drawerState.close() }
              viewModel.logout()
            }
          )
        }
      }
    }
  ) {
    Scaffold(
      modifier = modifier.fillMaxSize(),
      snackbarHost = { SnackbarHost(snackbarHostState) },
      topBar = {
        if (currentPage != AppPage.HOME) {
          TopAppBar(
            title = {
              Column {
                Text(
                  text = currentPage.title,
                  fontWeight = FontWeight.Black,
                  fontSize = 18.sp
                )
                Text(
                  text = "Sukses Jaya Jember · HRIS V3",
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            },
            navigationIcon = {
              IconButton(
                onClick = { scope.launch { drawerState.open() } },
                modifier = Modifier.testTag("nav_drawer_toggle")
              ) {
                Icon(Icons.Default.Menu, contentDescription = "Menu")
              }
            },
            actions = {
              // Geofence status pill in TopAppBar
              Surface(
                color = if (gpsState.isWithinGeofence) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                shape = RoundedCornerShape(99.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (gpsState.isWithinGeofence) Color(0xFF86EFAC) else Color(0xFFFCA5A5)),
                shadowElevation = 1.dp,
                modifier = Modifier
                  .clickable { viewModel.toggleGpsTestLocation(!gpsState.isWithinGeofence) }
                  .padding(end = 8.dp)
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                  Box(
                    modifier = Modifier
                      .size(6.dp)
                      .clip(CircleShape)
                      .background(if (gpsState.isWithinGeofence) SjSuccess else SjDanger)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "${gpsState.distanceToOfficeMeters}m",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (gpsState.isWithinGeofence) SjSuccess else SjDanger
                  )
                }
              }

              // User Avatar with menu
              Box {
                IconButton(onClick = { showUserDropdown = true }) {
                  AvatarCircle(name = currentUser?.nama ?: "SJ", photoUrl = currentUser?.fotoUrl, size = 32.dp, fontSize = 12)
                }
                DropdownMenu(
                  expanded = showUserDropdown,
                  onDismissRequest = { showUserDropdown = false }
                ) {
                  DropdownMenuItem(
                    text = { Text("Profil & BPJS") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    onClick = {
                      showUserDropdown = false
                      viewModel.selectPage(AppPage.PROFILE)
                    }
                  )
                  DropdownMenuItem(
                    text = { Text("Slip Gaji") },
                    leadingIcon = { Icon(Icons.Default.ReceiptLong, contentDescription = null) },
                    onClick = {
                      showUserDropdown = false
                      viewModel.selectPage(AppPage.PAYSLIP)
                    }
                  )
                  Divider()
                  DropdownMenuItem(
                    text = { Text("Keluar", color = MaterialTheme.colorScheme.error) },
                    leadingIcon = { Icon(Icons.Default.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                    onClick = {
                      showUserDropdown = false
                      viewModel.logout()
                    }
                  )
                }
              }
            },
            colors = TopAppBarDefaults.topAppBarColors(
              containerColor = MaterialTheme.colorScheme.surface
            )
          )
        }
      },
      bottomBar = {
        Surface(
          color = BentoCardWhite,
          modifier = Modifier.fillMaxWidth(),
          border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder),
          shadowElevation = 8.dp
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .navigationBarsPadding()
              .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Dashboard (Home)
            BentoNavButton(
              title = "Dashboard",
              icon = Icons.Default.Dashboard,
              selected = currentPage == AppPage.HOME,
              onClick = { viewModel.selectPage(AppPage.HOME) }
            )

            // History
            BentoNavButton(
              title = "Riwayat",
              icon = Icons.Default.History,
              selected = currentPage == AppPage.HISTORY,
              onClick = { viewModel.selectPage(AppPage.HISTORY) }
            )

            // Center Elevated Circular Green Action: KAMERA PRESENSI MASUK / PULANG
            Box(
              modifier = Modifier
                .offset(y = (-14).dp)
                .size(66.dp)
                .shadow(8.dp, CircleShape)
                .clip(CircleShape)
                .background(Color(0xFF16A34A))
                .border(3.dp, Color.White, CircleShape)
                .clickable {
                  if (currentPage != AppPage.HOME) {
                    viewModel.selectPage(AppPage.HOME)
                  }
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
                  modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = when {
                    !hasClockedIn -> "FOTO\nMASUK"
                    !hasClockedOut -> "FOTO\nPULANG"
                    else -> "KAMERA\nABSEN"
                  },
                  color = Color.White,
                  fontSize = 8.sp,
                  fontWeight = FontWeight.Black,
                  textAlign = TextAlign.Center,
                  lineHeight = 9.sp
                )
              }
            }

            // Requests / Cuti
            BentoNavButton(
              title = "Pengajuan",
              icon = Icons.Default.Assignment,
              selected = currentPage == AppPage.REQUESTS,
              onClick = { viewModel.selectPage(AppPage.REQUESTS) }
            )

            // Profile
            BentoNavButton(
              title = "Profil",
              icon = Icons.Default.Person,
              selected = currentPage == AppPage.PROFILE,
              onClick = { viewModel.selectPage(AppPage.PROFILE) }
            )
          }
        }
      }
    ) { innerPadding ->
      Box(
        modifier = Modifier
          .fillMaxSize()
          .then(
            if (currentPage == AppPage.HOME) {
              Modifier.padding(bottom = innerPadding.calculateBottomPadding())
            } else {
              Modifier.padding(innerPadding)
            }
          )
          .background(Color(0xFFF1F5F9))
      ) {
        when (currentPage) {
          AppPage.HOME -> DashboardScreen(
            viewModel = viewModel,
            onOpenDrawer = { scope.launch { drawerState.open() } }
          )
          AppPage.HISTORY -> AttendanceHistoryScreen(viewModel = viewModel)
          AppPage.CALENDAR -> AttendanceCalendarScreen(viewModel = viewModel)
          AppPage.FINANCIAL -> FinancialKasbonScreen(viewModel = viewModel)
          AppPage.REQUESTS -> RequestsScreen(viewModel = viewModel)
          AppPage.PAYSLIP -> PayslipScreen(viewModel = viewModel)
          AppPage.PROFILE -> ProfileBpjsScreen(viewModel = viewModel)
          AppPage.ADMIN -> AdminDashboardScreen(viewModel = viewModel)
          AppPage.EMPLOYEES -> EmployeeManagementScreen(viewModel = viewModel)
          AppPage.SHIFTS -> ShiftManagementScreen(viewModel = viewModel)
          AppPage.PAYROLL_ADMIN -> PayrollAdminScreen(viewModel = viewModel)
          AppPage.APPROVALS -> AdminDashboardScreen(viewModel = viewModel)
          AppPage.REPORTS -> ReportsScreen(viewModel = viewModel)
          AppPage.ANNOUNCEMENTS -> AnnouncementsScreen(viewModel = viewModel)
        }
      }
    }
  }
}

@Composable
private fun SidebarNavItem(
  title: String,
  icon: ImageVector,
  selected: Boolean,
  onClick: () -> Unit,
  textColor: Color? = null
) {
  val bg = if (selected) SjPrimary.copy(alpha = 0.12f) else Color.Transparent
  val contentColor = when {
    selected -> SjPrimary
    textColor != null -> textColor
    else -> BentoSlate600
  }

  Surface(
    color = bg,
    shape = RoundedCornerShape(12.dp),
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 2.dp)
      .clickable { onClick() }
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
      Icon(
        imageVector = icon,
        contentDescription = title,
        tint = contentColor,
        modifier = Modifier.size(20.dp)
      )
      Spacer(modifier = Modifier.width(12.dp))
      Text(
        text = title,
        color = contentColor,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
        fontSize = 13.5.sp
      )
    }
  }
}

@Composable
private fun BentoNavButton(
  title: String,
  icon: ImageVector,
  selected: Boolean,
  onClick: () -> Unit
) {
  val color = if (selected) SjPrimary else BentoSlate400
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(3.dp),
    modifier = Modifier
      .clip(RoundedCornerShape(12.dp))
      .clickable { onClick() }
      .padding(horizontal = 8.dp, vertical = 4.dp)
  ) {
    Icon(
      imageVector = icon,
      contentDescription = title,
      tint = color,
      modifier = Modifier.size(22.dp)
    )
    Text(
      text = title,
      color = color,
      fontSize = 9.sp,
      fontWeight = FontWeight.Black,
      letterSpacing = 0.5.sp
    )
  }
}
