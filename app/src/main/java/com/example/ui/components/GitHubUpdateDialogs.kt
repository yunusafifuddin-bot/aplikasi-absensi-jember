package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.data.remote.GitHubUpdateInfo
import com.example.ui.theme.*
import com.example.ui.viewmodel.HrisViewModel

@Composable
fun GitHubUpdateBanner(
  updateInfo: GitHubUpdateInfo,
  onDownload: () -> Unit,
  onDetails: () -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .shadow(8.dp, RoundedCornerShape(18.dp)),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
    border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f))
  ) {
    Column(
      modifier = Modifier
        .background(
          Brush.linearGradient(
            colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF0C4A6E))
          )
        )
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Box(
            modifier = Modifier
              .size(32.dp)
              .clip(CircleShape)
              .background(Color(0xFF38BDF8).copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.SystemUpdate,
              contentDescription = null,
              tint = Color(0xFF38BDF8),
              modifier = Modifier.size(18.dp)
            )
          }

          Column {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Text(
                text = "Pembaruan GitHub",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
              )
              Surface(
                color = Color(0xFF0284C7),
                shape = RoundedCornerShape(6.dp)
              ) {
                Text(
                  text = updateInfo.latestVersion,
                  color = Color.White,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.ExtraBold,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }
            Text(
              text = "Perubahan file terdeteksi di GitHub",
              color = Color(0xFF94A3B8),
              fontSize = 11.sp
            )
          }
        }

        IconButton(
          onClick = onDismiss,
          modifier = Modifier.size(24.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Tutup",
            tint = Color(0xFF94A3B8),
            modifier = Modifier.size(16.dp)
          )
        }
      }

      Text(
        text = updateInfo.releaseTitle.ifBlank { updateInfo.latestCommitMessage },
        color = Color(0xFFE2E8F0),
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Button(
          onClick = onDownload,
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
          shape = RoundedCornerShape(10.dp),
          contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
          modifier = Modifier.weight(1f)
        ) {
          Icon(
            imageVector = Icons.Default.Download,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text("Unduh APK Baru", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        OutlinedButton(
          onClick = onDetails,
          colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8)),
          border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.6f)),
          shape = RoundedCornerShape(10.dp),
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
        ) {
          Text("Detail", fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
      }
    }
  }
}

@Composable
fun GitHubUpdateDetailDialog(
  updateInfo: GitHubUpdateInfo,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Icon(
          imageVector = Icons.Default.SystemUpdateAlt,
          contentDescription = null,
          tint = SjPrimary
        )
        Text(
          text = "Pembaruan GitHub Tersedia",
          fontWeight = FontWeight.Bold,
          fontSize = 16.sp
        )
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Surface(
          color = Color(0xFFF1F5F9),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
              text = "Versi / Rilis: ${updateInfo.latestVersion}",
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp,
              color = BentoSlate800
            )
            if (updateInfo.latestCommitSha.isNotBlank()) {
              Text(
                text = "Commit: ${updateInfo.latestCommitSha.take(8)}",
                fontSize = 11.sp,
                color = BentoSlate500
              )
            }
            if (updateInfo.publishedDate.isNotBlank()) {
              Text(
                text = "Tanggal: ${updateInfo.publishedDate.take(10)}",
                fontSize = 11.sp,
                color = BentoSlate500
              )
            }
          }
        }

        Text(
          text = "Catatan Perubahan / Rilis:",
          fontWeight = FontWeight.Bold,
          fontSize = 12.sp,
          color = BentoSlate800
        )

        Surface(
          color = Color(0xFFF8FAFC),
          shape = RoundedCornerShape(10.dp),
          border = BorderStroke(1.dp, FormalBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = updateInfo.releaseNotes.ifBlank { updateInfo.latestCommitMessage }.ifBlank { "Pembaruan bug fix & kestabilan." },
            fontSize = 12.sp,
            color = BentoSlate700,
            modifier = Modifier.padding(10.dp)
          )
        }

        Text(
          text = "File APK debug akan diunduh dari GitHub Releases. Setelah selesai diunduh, ketuk notifikasi unduhan untuk menginstall pembaruan aplikasi.",
          fontSize = 11.sp,
          color = BentoSlate500,
          lineHeight = 15.sp
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val url = updateInfo.apkDownloadUrl ?: updateInfo.releasePageUrl
          val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
          }
          context.startActivity(intent)
          onDismiss()
        },
        colors = ButtonDefaults.buttonColors(containerColor = SjPrimary),
        shape = RoundedCornerShape(8.dp)
      ) {
        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Unduh & Install APK")
      }
    },
    dismissButton = {
      Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        OutlinedButton(
          onClick = {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateInfo.releasePageUrl)).apply {
              flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
          },
          shape = RoundedCornerShape(8.dp)
        ) {
          Text("Lihat GitHub")
        }
        TextButton(onClick = onDismiss) {
          Text("Tutup")
        }
      }
    }
  )
}

@Composable
fun GitHubRepoSettingsDialog(
  viewModel: HrisViewModel,
  onDismiss: () -> Unit
) {
  val currentRepo by viewModel.githubRepo.collectAsState()
  val isChecking by viewModel.isCheckingUpdate.collectAsState()
  val updateInfo by viewModel.githubUpdate.collectAsState()

  var inputRepo by remember { mutableStateOf(currentRepo) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Sync,
          contentDescription = null,
          tint = SjPrimary
        )
        Text(
          text = "Sinkronisasi & Update GitHub",
          fontWeight = FontWeight.Bold,
          fontSize = 16.sp
        )
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Text(
          text = "Aplikasi akan memantau repositori GitHub ini untuk mendeteksi perubahan file/commit atau rilis APK baru.",
          fontSize = 12.sp,
          color = BentoSlate600,
          lineHeight = 16.sp
        )

        OutlinedTextField(
          value = inputRepo,
          onValueChange = { inputRepo = it },
          label = { Text("Repositori GitHub (owner/repo)") },
          placeholder = { Text("contoh: yunusafifuddin/hris-app") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        Surface(
          color = Color(0xFFF1F5F9),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
              text = "Status Versi Terpasang:",
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp,
              color = BentoSlate700
            )
            Text(
              text = "• Versi: ${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})",
              fontSize = 11.sp,
              color = BentoSlate600
            )
            if (updateInfo != null) {
              Text(
                text = "• Di GitHub: ${updateInfo?.latestVersion} (${if (updateInfo?.hasUpdate == true) "Ada pembaruan!" else "Terbaru"})",
                fontSize = 11.sp,
                color = if (updateInfo?.hasUpdate == true) SjSuccess else BentoSlate600,
                fontWeight = if (updateInfo?.hasUpdate == true) FontWeight.Bold else FontWeight.Normal
              )
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (inputRepo.isNotBlank()) {
            viewModel.setGithubRepo(inputRepo)
          } else {
            viewModel.checkAppUpdate(userInitiated = true)
          }
          onDismiss()
        },
        enabled = !isChecking,
        colors = ButtonDefaults.buttonColors(containerColor = SjPrimary),
        shape = RoundedCornerShape(8.dp)
      ) {
        if (isChecking) {
          CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
          Spacer(modifier = Modifier.width(6.dp))
          Text("Memeriksa...")
        } else {
          Text("Simpan & Cek Update")
        }
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Tutup")
      }
    }
  )
}
