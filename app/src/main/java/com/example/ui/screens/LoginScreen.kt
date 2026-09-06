package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.HrisViewModel

@Composable
fun LoginScreen(
  viewModel: HrisViewModel,
  modifier: Modifier = Modifier
) {
  var nik by remember { mutableStateOf("SJ001") }
  var password by remember { mutableStateOf("123") }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  var isLoading by remember { mutableStateOf(false) }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(FormalGradient)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(20.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      // Branding Header
      Box(
        modifier = Modifier
          .size(68.dp)
          .clip(RoundedCornerShape(22.dp))
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
          fontSize = 28.sp
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = "SUKSES JAYA JEMBER",
        color = BentoSlate800,
        fontWeight = FontWeight.Black,
        fontSize = 22.sp,
        letterSpacing = 1.2.sp
      )

      Text(
        text = "Sistem Manajemen SDM & Presensi Enterprise · V3",
        color = BentoSlate600,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium
      )

      Spacer(modifier = Modifier.height(22.dp))

      // Card Features & Stats in Elegant Formal Pill
      Surface(
        color = Color.White,
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, FormalBorder),
        shadowElevation = 2.dp,
        modifier = Modifier
          .fillMaxWidth()
          .widthIn(max = 440.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 12.dp),
          horizontalArrangement = Arrangement.SpaceEvenly,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "5", color = BentoSlate800, fontWeight = FontWeight.Black, fontSize = 17.sp)
            Text(text = "Karyawan", color = BentoSlate400, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
          }
          Box(modifier = Modifier.height(24.dp).width(1.dp).background(FormalBorder))
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "100m", color = BentoSlate800, fontWeight = FontWeight.Black, fontSize = 17.sp)
            Text(text = "Geofence", color = BentoSlate400, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
          }
          Box(modifier = Modifier.height(24.dp).width(1.dp).background(FormalBorder))
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "24/7", color = BentoSlate800, fontWeight = FontWeight.Black, fontSize = 17.sp)
            Text(text = "Cloud HR", color = BentoSlate400, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Login Box Card
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .widthIn(max = 440.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, FormalBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
      ) {
        Column(modifier = Modifier.padding(26.dp)) {
          Text(
            text = "Selamat Datang",
            fontWeight = FontWeight.Black,
            fontSize = 24.sp,
            color = BentoSlate800
          )
          Text(
            text = "Masuk ke portal HRIS resmi Anda",
            fontSize = 13.sp,
            color = BentoSlate600
          )

          Spacer(modifier = Modifier.height(20.dp))

          OutlinedTextField(
            value = nik,
            onValueChange = { nik = it },
            label = { Text("NIK Karyawan") },
            placeholder = { Text("Contoh: SJ001") },
            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("login_nik_input"),
            shape = RoundedCornerShape(12.dp)
          )

          Spacer(modifier = Modifier.height(14.dp))

          OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("login_password_input"),
            shape = RoundedCornerShape(12.dp)
          )

          if (errorMessage != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
              color = SjDangerLight,
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = errorMessage ?: "",
                color = SjDanger,
                fontSize = 12.sp,
                modifier = Modifier.padding(10.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(22.dp))

          Button(
            onClick = {
              isLoading = true
              errorMessage = null
              viewModel.login(
                nik = nik,
                pass = password,
                onSuccess = { isLoading = false },
                onError = { err ->
                  isLoading = false
                  errorMessage = err
                }
              )
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp)
              .testTag("login_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SjPrimary),
            enabled = !isLoading
          ) {
            if (isLoading) {
              CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Masuk ke Aplikasi", fontWeight = FontWeight.Bold, fontSize = 15.sp)
              }
            }
          }

          Spacer(modifier = Modifier.height(20.dp))

          Text(
            text = "Akun Demo Siap Pakai:",
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            SuggestionChip(
              onClick = {
                nik = "SJ001"
                password = "123"
              },
              label = { Text("Karyawan (SJ001)", fontSize = 11.sp) },
              modifier = Modifier.weight(1f)
            )
            SuggestionChip(
              onClick = {
                nik = "SJ002"
                password = "123"
              },
              label = { Text("HR (SJ002)", fontSize = 11.sp) },
              modifier = Modifier.weight(1f)
            )
            SuggestionChip(
              onClick = {
                nik = "SJ003"
                password = "admin"
              },
              label = { Text("Admin (SJ003)", fontSize = 11.sp) },
              modifier = Modifier.weight(1f)
            )
          }
        }
      }
    }
  }
}
