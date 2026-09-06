package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.StatCard
import com.example.ui.components.StatusBadge
import com.example.ui.components.formatRupiah
import com.example.ui.theme.*
import com.example.ui.viewmodel.HrisViewModel

@Composable
fun FinancialKasbonScreen(
  viewModel: HrisViewModel,
  modifier: Modifier = Modifier
) {
  val user by viewModel.currentUser.collectAsState()
  val kasbons by viewModel.allKasbons.collectAsState()

  val userKasbons = remember(kasbons, user) {
    kasbons.filter { it.nik == user?.nik }
  }

  val limit = user?.limitKasbon ?: 3000000L
  val outstanding = remember(userKasbons) {
    userKasbons.filter { it.statusPersetujuan == "Approved" }.sumOf { it.jumlah }
  }
  val remaining = (limit - outstanding).coerceAtLeast(0L)

  var showDialog by remember { mutableStateOf(false) }
  var amountInput by remember { mutableStateOf("") }
  var reasonInput by remember { mutableStateOf("") }

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
          text = "Financial / Kasbon",
          fontWeight = FontWeight.Black,
          fontSize = 22.sp
        )
        Text(
          text = "Pantau limit dan pengajuan salary advance",
          fontSize = 12.5.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Button(
        onClick = { showDialog = true },
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = SjPrimary),
        modifier = Modifier.testTag("btn_ajukan_kasbon")
      ) {
        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Ajukan Kasbon")
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // 3 Bento Cards: Limit, Outstanding, Sisa Limit
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      com.example.ui.components.BentoStatTile(
        title = "Limit Kasbon",
        value = formatRupiah(limit),
        iconText = "💳",
        bgColor = com.example.ui.theme.BentoIndigoBg,
        iconColor = com.example.ui.theme.BentoIndigo,
        modifier = Modifier.weight(1f)
      )
      com.example.ui.components.BentoStatTile(
        title = "Kasbon Berjalan",
        value = formatRupiah(outstanding),
        iconText = "⏳",
        bgColor = com.example.ui.theme.BentoOrangeBg,
        iconColor = com.example.ui.theme.BentoOrange,
        modifier = Modifier.weight(1f)
      )
    }

    Spacer(modifier = Modifier.height(12.dp))

    com.example.ui.components.BentoTileWide(
      title = "Sisa Limit Kasbon Anda",
      value = formatRupiah(remaining),
      iconText = "✓",
      iconBgColor = com.example.ui.theme.BentoEmeraldBg,
      onClick = null
    )

    Spacer(modifier = Modifier.height(20.dp))

    Text(
      text = "Riwayat Pengajuan Kasbon",
      fontWeight = FontWeight.Black,
      fontSize = 17.sp,
      color = com.example.ui.theme.BentoSlate800
    )

    Spacer(modifier = Modifier.height(12.dp))

    if (userKasbons.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "Belum ada riwayat pengajuan kasbon.",
          color = com.example.ui.theme.BentoSlate400
        )
      }
    } else {
      LazyColumn(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        items(userKasbons, key = { it.idKasbon }) { item ->
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.BentoCardWhite),
            border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.BentoBorder)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = formatRupiah(item.jumlah),
                  fontWeight = FontWeight.Black,
                  fontSize = 17.sp,
                  color = MaterialTheme.colorScheme.primary
                )
                Text(
                  text = "ID: ${item.idKasbon}  •  ${item.tanggalPengajuan}",
                  fontSize = 11.5.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                  text = item.keterangan,
                  fontSize = 12.5.sp,
                  color = MaterialTheme.colorScheme.onSurface
                )
              }
              StatusBadge(status = item.statusPersetujuan)
            }
          }
        }
      }
    }
  }

  // Dialog Ajukan Kasbon
  if (showDialog) {
    AlertDialog(
      onDismissRequest = { showDialog = false },
      title = { Text("Ajukan Kasbon (Salary Advance)", fontWeight = FontWeight.Bold) },
      text = {
        Column {
          Text(
            text = "Sisa limit kasbon Anda: ${formatRupiah(remaining)}",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(14.dp))
          OutlinedTextField(
            value = amountInput,
            onValueChange = { amountInput = it },
            label = { Text("Jumlah (Rp)") },
            placeholder = { Text("Contoh: 500000") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
          )
          Spacer(modifier = Modifier.height(10.dp))
          OutlinedTextField(
            value = reasonInput,
            onValueChange = { reasonInput = it },
            label = { Text("Keterangan Keperluan") },
            placeholder = { Text("Contoh: Biaya berobat keluarga") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3,
            shape = RoundedCornerShape(10.dp)
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val amount = amountInput.toLongOrNull() ?: 0L
            if (amount <= 0) {
              viewModel.emitMessage("Masukkan jumlah kasbon yang valid.")
              return@Button
            }
            if (amount > remaining) {
              viewModel.emitMessage("Jumlah melebihi sisa limit Anda.")
              return@Button
            }
            viewModel.submitKasbon(amount, reasonInput.ifBlank { "Keperluan mendesak" })
            showDialog = false
            amountInput = ""
            reasonInput = ""
          }
        ) {
          Text("Kirim Pengajuan")
        }
      },
      dismissButton = {
        TextButton(onClick = { showDialog = false }) {
          Text("Batal")
        }
      }
    )
  }
}
