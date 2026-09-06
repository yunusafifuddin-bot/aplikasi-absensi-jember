package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AvatarCircle
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.HrisViewModel

@Composable
fun ProfileBpjsScreen(
  viewModel: HrisViewModel,
  modifier: Modifier = Modifier
) {
  val user by viewModel.currentUser.collectAsState()
  val employees by viewModel.allEmployees.collectAsState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(16.dp)
  ) {
    Text(
      text = "Profil & BPJS",
      fontWeight = FontWeight.Black,
      fontSize = 22.sp
    )
    Text(
      text = "Informasi kepegawaian dan kepesertaan jaminan sosial",
      fontSize = 12.5.sp,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Profile Header Card
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(26.dp),
      colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.BentoCardWhite),
      border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.BentoBorder)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        AvatarCircle(
          name = user?.nama ?: "SJ",
          photoUrl = user?.fotoUrl,
          size = 72.dp,
          fontSize = 24
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column {
          Text(
            text = user?.nama ?: "-",
            fontWeight = FontWeight.Black,
            fontSize = 18.sp,
            color = com.example.ui.theme.BentoSlate800
          )
          Text(
            text = "${user?.jabatan}  •  ${user?.nik}",
            fontSize = 13.sp,
            color = com.example.ui.theme.BentoSlate400
          )
          Spacer(modifier = Modifier.height(6.dp))
          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            StatusBadge(status = user?.role ?: "USER")
            StatusBadge(status = user?.status ?: "Aktif")
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(18.dp))

    // Data Karyawan Details Card
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(26.dp),
      colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.BentoCardWhite),
      border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.BentoBorder)
    ) {
      Column(modifier = Modifier.padding(20.dp)) {
        Text("Data Kepegawaian", fontWeight = FontWeight.Black, fontSize = 16.sp, color = com.example.ui.theme.BentoSlate800)
        Spacer(modifier = Modifier.height(14.dp))

        ProfileRow("NIK Karyawan", user?.nik ?: "-")
        ProfileRow("Nama Lengkap", user?.nama ?: "-")
        ProfileRow("Jabatan", user?.jabatan ?: "-")
        ProfileRow("Hak Akses (Role)", user?.role ?: "-")
        ProfileRow("Email Perusahaan", user?.email ?: "-")
        ProfileRow("No. Handphone", user?.phone ?: "-")
        ProfileRow("Nomor Rekening Gaji", user?.noRekening ?: "-")
      }
    }

    Spacer(modifier = Modifier.height(18.dp))

    // BPJS Section
    Text("Kepesertaan BPJS", fontWeight = FontWeight.Black, fontSize = 16.sp, color = com.example.ui.theme.BentoSlate800)
    Spacer(modifier = Modifier.height(10.dp))

    // BPJS Kesehatan Card
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(22.dp),
      colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.BentoEmeraldBg),
      border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.BentoEmerald.copy(alpha = 0.2f))
    ) {
      Column(modifier = Modifier.padding(18.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.HealthAndSafety, contentDescription = null, tint = SjSuccess)
            Spacer(modifier = Modifier.width(8.dp))
            Text("BPJS Kesehatan", fontWeight = FontWeight.Bold, fontSize = 15.sp)
          }
          StatusBadge(status = user?.statusKes ?: "Aktif")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("Nomor Kartu: ${user?.noBpjsKes ?: "000182736451"}", fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp)
        Text("Faskes Tingkat 1: ${user?.faskes ?: "Klinik Jember Sehat"}", fontSize = 12.sp, color = Color(0xFF15803D))
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // BPJS Ketenagakerjaan Card
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(22.dp),
      colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.BentoIndigoBg),
      border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.BentoIndigo.copy(alpha = 0.2f))
    ) {
      Column(modifier = Modifier.padding(18.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFF2563EB))
            Spacer(modifier = Modifier.width(8.dp))
            Text("BPJS Ketenagakerjaan", fontWeight = FontWeight.Bold, fontSize = 15.sp)
          }
          StatusBadge(status = user?.statusTk ?: "Aktif")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("Nomor Kartu: ${user?.noBpjsTk ?: "19283746501"}", fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp)
        Text("Program: JHT (Jaminan Hari Tua), JKK, JKM & JP", fontSize = 12.sp, color = Color(0xFF1D4ED8))
      }
    }

    Spacer(modifier = Modifier.height(30.dp))
  }
}

@Composable
private fun ProfileRow(label: String, value: String) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 6.dp),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(text = label, fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
  }
  Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
}
