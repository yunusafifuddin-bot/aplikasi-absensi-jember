package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "employees")
data class EmployeeEntity(
  @PrimaryKey val nik: String,
  val nama: String,
  val jabatan: String,
  val role: String, // "ADMIN", "HR", "USER"
  val status: String = "Aktif", // "Aktif", "Nonaktif"
  val latKantor: Double = -8.1724,
  val longKantor: Double = 113.6995,
  val limitKasbon: Long = 2500000L,
  val noRekening: String = "BCA 0881928371",
  val gajiPokok: Long = 4200000L,
  val tunjangan: Long = 850000L,
  val email: String = "",
  val phone: String = "",
  val password: String = "123456",
  val shiftId: String = "S1",
  val fotoUrl: String = "",
  val noBpjsKes: String = "000182736451",
  val statusKes: String = "Aktif",
  val noBpjsTk: String = "19283746501",
  val statusTk: String = "Aktif",
  val faskes: String = "Klinik Pratama Sukses Sehat Jember"
)

@Entity(tableName = "shifts")
data class ShiftEntity(
  @PrimaryKey val shiftId: String,
  val namaShift: String,
  val jamMasuk: String,
  val jamPulang: String,
  val toleransiMenit: Int = 15,
  val keterangan: String = ""
)

@Entity(tableName = "attendances")
data class AttendanceEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val nik: String,
  val tanggal: String, // "YYYY-MM-DD"
  val jamMasuk: String? = null,
  val jamPulang: String? = null,
  val status: String = "Belum Absen", // "Tepat Waktu", "Terlambat", "Cuti", "Belum Absen"
  val jarakMeter: Int = 24,
  val fotoMasukUrl: String? = null,
  val fotoPulangUrl: String? = null,
  val lat: Double = -8.1724,
  val lng: Double = 113.6995,
  val durasiJam: Double = 8.0
)

@Entity(tableName = "kasbons")
data class KasbonEntity(
  @PrimaryKey val idKasbon: String,
  val nik: String,
  val nama: String,
  val tanggalPengajuan: String,
  val jumlah: Long,
  val keterangan: String,
  val statusPersetujuan: String = "Pending" // "Pending", "Approved", "Rejected"
)

@Entity(tableName = "cutis")
data class CutiEntity(
  @PrimaryKey val idCuti: String,
  val nik: String,
  val nama: String,
  val jenisCuti: String, // "Cuti Tahunan", "Izin", "Sakit", "Cuti Khusus"
  val tglMulai: String,
  val tglSelesai: String,
  val alasan: String,
  val status: String = "Pending", // "Pending", "Approved", "Rejected"
  val lampiran: String? = null
)

@Entity(tableName = "lemburs")
data class LemburEntity(
  @PrimaryKey val idLembur: String,
  val nik: String,
  val nama: String,
  val tanggal: String,
  val jamMulai: String,
  val jamSelesai: String,
  val durasiJam: Double,
  val deskripsi: String,
  val statusPersetujuan: String = "Pending" // "Pending", "Approved", "Rejected"
)

@Entity(tableName = "payrolls")
data class PayrollEntity(
  @PrimaryKey val idPayroll: String,
  val nik: String,
  val nama: String,
  val jabatan: String,
  val periode: String, // "YYYY-MM"
  val gajiPokok: Long,
  val tunjangan: Long,
  val uangLembur: Long,
  val potonganKasbon: Long,
  val potonganLain: Long,
  val gajiBersih: Long,
  val status: String = "Draft", // "Paid", "Draft"
  val tanggalBayar: String? = null
)

@Entity(tableName = "announcements")
data class AnnouncementEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val judul: String,
  val isi: String,
  val kategori: String = "Info", // "Info", "Penting", "HR", "Operasional"
  val tanggal: String,
  val target: String = "Semua"
)
