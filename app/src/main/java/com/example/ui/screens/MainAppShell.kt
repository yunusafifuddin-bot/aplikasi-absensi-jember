package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AvatarCircle
import com.example.ui.viewmodel.AppPage
import com.example.ui.viewmodel.HrisViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppShell(viewModel: HrisViewModel, modifier: Modifier = Modifier) {
  val user by viewModel.currentUser.collectAsState()
  val page by viewModel.currentPage.collectAsState()
  val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
  val scope = rememberCoroutineScope()
  val snackbarHostState = remember { SnackbarHostState() }
  val isAdmin = user?.role in listOf("ADMIN", "HR")

  LaunchedEffect(Unit) {
    viewModel.snackbarMessage.collect { snackbarHostState.showSnackbar(it) }
  }

  fun navigate(target: AppPage) {
    viewModel.selectPage(target)
    scope.launch { drawerState.close() }
  }

  ModalNavigationDrawer(
    drawerState = drawerState,
    drawerContent = {
      ModalDrawerSheet(modifier = Modifier.width(290.dp)) {
        Column(
          modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 10.dp)
          ) {
            AvatarCircle(user?.nama ?: "SJ", size = 44.dp)
            Spacer(Modifier.width(10.dp))
            Column {
              Text("SUKSES JAYA", fontWeight = FontWeight.Black, fontSize = 16.sp)
              Text("HRIS JEMBER", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
            }
          }
          Text(user?.nama ?: "Karyawan", fontWeight = FontWeight.Bold, fontSize = 14.sp)
          Text(user?.jabatan ?: "", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Spacer(Modifier.height(18.dp))
          Text("MENU", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Spacer(Modifier.height(5.dp))

          DrawerItem("Dashboard", Icons.Default.Dashboard, page == AppPage.HOME) { navigate(AppPage.HOME) }
          DrawerItem("Riwayat Absensi", Icons.Default.History, page == AppPage.HISTORY) { navigate(AppPage.HISTORY) }
          DrawerItem("Kalender Kehadiran", Icons.Default.CalendarMonth, page == AppPage.CALENDAR) { navigate(AppPage.CALENDAR) }
          DrawerItem("Kasbon", Icons.Default.AccountBalanceWallet, page == AppPage.FINANCIAL) { navigate(AppPage.FINANCIAL) }
          DrawerItem("Lembur", Icons.Default.Timelapse, page == AppPage.REQUESTS) { navigate(AppPage.REQUESTS) }
          DrawerItem("Slip Gaji", Icons.Default.ReceiptLong, page == AppPage.PAYSLIP) { navigate(AppPage.PAYSLIP) }
          DrawerItem("Profil & BPJS", Icons.Default.Person, page == AppPage.PROFILE) { navigate(AppPage.PROFILE) }

          if (isAdmin) {
            Spacer(Modifier.height(14.dp))
            Text("ADMIN / HR", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            DrawerItem("HR Dashboard", Icons.Default.AdminPanelSettings, page == AppPage.ADMIN) { navigate(AppPage.ADMIN) }
            DrawerItem("Karyawan", Icons.Default.Group, page == AppPage.EMPLOYEES) { navigate(AppPage.EMPLOYEES) }
            DrawerItem("Shift Kerja", Icons.Default.WorkHistory, page == AppPage.SHIFTS) { navigate(AppPage.SHIFTS) }
            DrawerItem("Payroll", Icons.Default.Payments, page == AppPage.PAYROLL_ADMIN) { navigate(AppPage.PAYROLL_ADMIN) }
            DrawerItem("Laporan", Icons.Default.Assessment, page == AppPage.REPORTS) { navigate(AppPage.REPORTS) }
            DrawerItem("Pengumuman", Icons.Default.Campaign, page == AppPage.ANNOUNCEMENTS) { navigate(AppPage.ANNOUNCEMENTS) }
          }

          Spacer(Modifier.height(16.dp))
          DrawerItem("Sinkronkan Google Sheets", Icons.Default.CloudSync, false) {
            viewModel.syncDatabase(true)
            scope.launch { drawerState.close() }
          }
          DrawerItem("Keluar", Icons.Default.Logout, false, MaterialTheme.colorScheme.error) {
            viewModel.logout()
            scope.launch { drawerState.close() }
          }
        }
      }
    },
    content = {
      Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
          if (page != AppPage.HOME) {
            TopAppBar(
              title = { Text(page.title, fontWeight = FontWeight.Black) },
              navigationIcon = {
                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                  Icon(Icons.Default.Menu, contentDescription = "Menu")
                }
              }
            )
          }
        },
        bottomBar = {
          NavigationBar {
            NavigationBarItem(
              selected = page == AppPage.HOME,
              onClick = { navigate(AppPage.HOME) },
              icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
              label = { Text("Home", fontSize = 10.sp) }
            )
            NavigationBarItem(
              selected = page == AppPage.HISTORY,
              onClick = { navigate(AppPage.HISTORY) },
              icon = { Icon(Icons.Default.History, contentDescription = null) },
              label = { Text("History", fontSize = 10.sp) }
            )
            NavigationBarItem(
              selected = page == AppPage.REQUESTS,
              onClick = { navigate(AppPage.REQUESTS) },
              icon = { Icon(Icons.Default.Timelapse, contentDescription = null) },
              label = { Text("Lembur", fontSize = 10.sp) }
            )
            NavigationBarItem(
              selected = page == AppPage.PROFILE,
              onClick = { navigate(AppPage.PROFILE) },
              icon = { Icon(Icons.Default.Person, contentDescription = null) },
              label = { Text("Profil", fontSize = 10.sp) }
            )
          }
        }
      ) { paddingValues ->
        Box(
          modifier = Modifier.fillMaxSize().padding(paddingValues).background(Color(0xFFF1F5F9))
        ) {
          when (page) {
            AppPage.HOME -> DashboardScreen(viewModel, onOpenDrawer = { scope.launch { drawerState.open() } })
            AppPage.HISTORY -> AttendanceHistoryScreen(viewModel)
            AppPage.CALENDAR -> AttendanceCalendarScreen(viewModel)
            AppPage.FINANCIAL -> FinancialKasbonScreen(viewModel)
            AppPage.REQUESTS -> RequestsScreen(viewModel)
            AppPage.PAYSLIP -> PayslipScreen(viewModel)
            AppPage.PROFILE -> ProfileBpjsScreen(viewModel)
            AppPage.ADMIN -> AdminDashboardScreen(viewModel)
            AppPage.EMPLOYEES -> EmployeeManagementScreen(viewModel)
            AppPage.SHIFTS -> ShiftManagementScreen(viewModel)
            AppPage.PAYROLL_ADMIN -> PayrollAdminScreen(viewModel)
            AppPage.APPROVALS -> AdminDashboardScreen(viewModel)
            AppPage.REPORTS -> ReportsScreen(viewModel)
            AppPage.ANNOUNCEMENTS -> AnnouncementsScreen(viewModel)
          }
        }
      }
    }
  )
}

@Composable
private fun DrawerItem(
  title: String,
  icon: ImageVector,
  selected: Boolean,
  textColor: Color? = null,
  onClick: () -> Unit
) {
  val color = textColor ?: if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
  Surface(
    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp).clickable(onClick = onClick),
    color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = .10f) else Color.Transparent,
    shape = RoundedCornerShape(12.dp)
  ) {
    Row(Modifier.padding(horizontal = 12.dp, vertical = 11.dp), verticalAlignment = Alignment.CenterVertically) {
      Icon(icon, contentDescription = title, modifier = Modifier.size(20.dp), tint = color)
      Spacer(Modifier.width(12.dp))
      Text(title, color = color, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, fontSize = 13.sp)
    }
  }
}
