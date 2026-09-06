package com.example.data.local

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DatabaseInitializer {

  suspend fun preseedIfEmpty(database: AppDatabase) {
    withContext(Dispatchers.IO) {
      val empDao = database.employeeDao()
      if (empDao.getEmployeeByNik("SJ001") == null) {
        // Seed Employees
        val employees = listOf(
          EmployeeEntity(
            nik = "SJ001",
            nama = "Ahmad Fauzi",
            jabatan = "IT Specialist",
            role = "USER",
            status = "Aktif",
            latKantor = -8.1724,
            longKantor = 113.6995,
            limitKasbon = 3000000L,
            noRekening = "BCA 0881928371",
            gajiPokok = 4500000L,
            tunjangan = 900000L,
            email = "ahmad.fauzi@suksesjaya.co.id",
            phone = "081234567890",
            password = "123",
            shiftId = "S1",
            noBpjsKes = "000182736451",
            statusKes = "Aktif",
            noBpjsTk = "19283746501",
            statusTk = "Aktif",
            faskes = "Klinik Pratama Rawat Inap Jember Sehat"
          ),
          EmployeeEntity(
            nik = "SJ002",
            nama = "Dewi Sartika",
            jabatan = "HR Manager",
            role = "HR",
            status = "Aktif",
            latKantor = -8.1724,
            longKantor = 113.6995,
            limitKasbon = 4000000L,
            noRekening = "Mandiri 1420019283711",
            gajiPokok = 6000000L,
            tunjangan = 1200000L,
            email = "dewi.sartika@suksesjaya.co.id",
            phone = "081398765432",
            password = "123",
            shiftId = "S1",
            noBpjsKes = "000182736452",
            statusKes = "Aktif",
            noBpjsTk = "19283746502",
            statusTk = "Aktif",
            faskes = "Puskesmas Kaliwates Jember"
          ),
          EmployeeEntity(
            nik = "SJ003",
            nama = "Budi Santoso",
            jabatan = "General Manager",
            role = "ADMIN",
            status = "Aktif",
            latKantor = -8.1724,
            longKantor = 113.6995,
            limitKasbon = 6000000L,
            noRekening = "BRI 001201928374501",
            gajiPokok = 8500000L,
            tunjangan = 2000000L,
            email = "budi.santoso@suksesjaya.co.id",
            phone = "081211223344",
            password = "admin",
            shiftId = "S1",
            noBpjsKes = "000182736453",
            statusKes = "Aktif",
            noBpjsTk = "19283746503",
            statusTk = "Aktif",
            faskes = "RS Jember Klinik"
          ),
          EmployeeEntity(
            nik = "SJ004",
            nama = "Rina Wulandari",
            jabatan = "Staff Keuangan",
            role = "USER",
            status = "Aktif",
            latKantor = -8.1724,
            longKantor = 113.6995,
            limitKasbon = 2500000L,
            noRekening = "BNI 0918273645",
            gajiPokok = 4000000L,
            tunjangan = 750000L,
            email = "rina.wulandari@suksesjaya.co.id",
            phone = "085712349988",
            password = "123",
            shiftId = "S1"
          ),
          EmployeeEntity(
            nik = "SJ005",
            nama = "Hendra Wijaya",
            jabatan = "Staff Logistik",
            role = "USER",
            status = "Aktif",
            latKantor = -8.1724,
            longKantor = 113.6995,
            limitKasbon = 2000000L,
            noRekening = "BCA 0883344556",
            gajiPokok = 3800000L,
            tunjangan = 650000L,
            email = "hendra.wijaya@suksesjaya.co.id",
            phone = "087812998877",
            password = "123",
            shiftId = "S2"
          )
        )
        empDao.insertAll(employees)

        // Seed Shifts
        val shifts = listOf(
          ShiftEntity(
            shiftId = "S1",
            namaShift = "Shift Reguler",
            jamMasuk = "08:00",
            jamPulang = "17:00",
            toleransiMenit = 15,
            keterangan = "Jam kerja standar Senin s/d Jumat"
          ),
          ShiftEntity(
            shiftId = "S2",
            namaShift = "Shift Pagi (Logistik)",
            jamMasuk = "07:00",
            jamPulang = "16:00",
            toleransiMenit = 10,
            keterangan = "Khusus pergudangan dan operasional armada"
          ),
          ShiftEntity(
            shiftId = "S3",
            namaShift = "Shift Siang",
            jamMasuk = "12:30",
            jamPulang = "21:00",
            toleransiMenit = 10,
            keterangan = "Operasional retail & customer care"
          )
        )
        database.shiftDao().insertAll(shifts)

        // Seed Attendance for the past 14 days
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        val attendances = mutableListOf<AttendanceEntity>()

        for (i in 13 downTo 1) {
          val pastCal = Calendar.getInstance()
          pastCal.add(Calendar.DAY_OF_YEAR, -i)
          val dayOfWeek = pastCal.get(Calendar.DAY_OF_WEEK)
          if (dayOfWeek == Calendar.SUNDAY) continue // skip Sunday

          val dateStr = sdf.format(pastCal.time)
          val isLate = (i % 4 == 0)
          val jamMasuk = if (isLate) "08:22" else "07:54"
          val status = if (isLate) "Terlambat" else "Tepat Waktu"
          attendances.add(
            AttendanceEntity(
              nik = "SJ001",
              tanggal = dateStr,
              jamMasuk = jamMasuk,
              jamPulang = "17:08",
              status = status,
              jarakMeter = if (isLate) 38 else 18,
              lat = -8.1724,
              lng = 113.6995,
              durasiJam = if (isLate) 8.7 else 9.2
            )
          )
        }
        database.attendanceDao().insertAll(attendances)

        // Seed Kasbon
        val kasbons = listOf(
          KasbonEntity(
            idKasbon = "KB-2026-001",
            nik = "SJ001",
            nama = "Ahmad Fauzi",
            tanggalPengajuan = "2026-09-01",
            jumlah = 750000L,
            keterangan = "Keperluan mendesak perbaikan sepeda motor operasional",
            statusPersetujuan = "Approved"
          ),
          KasbonEntity(
            idKasbon = "KB-2026-002",
            nik = "SJ004",
            nama = "Rina Wulandari",
            tanggalPengajuan = "2026-09-03",
            jumlah = 500000L,
            keterangan = "Pembayaran buku sekolah anak",
            statusPersetujuan = "Pending"
          )
        )
        database.kasbonDao().insertAll(kasbons)

        // Seed Cuti
        val cutis = listOf(
          CutiEntity(
            idCuti = "CT-2026-001",
            nik = "SJ001",
            nama = "Ahmad Fauzi",
            jenisCuti = "Cuti Tahunan",
            tglMulai = "2026-08-20",
            tglSelesai = "2026-08-21",
            alasan = "Acara pernikahan keluarga di Banyuwangi",
            status = "Approved"
          ),
          CutiEntity(
            idCuti = "CT-2026-002",
            nik = "SJ005",
            nama = "Hendra Wijaya",
            jenisCuti = "Sakit",
            tglMulai = "2026-09-02",
            tglSelesai = "2026-09-03",
            alasan = "Demam flu dan istirahat dokter",
            status = "Approved"
          ),
          CutiEntity(
            idCuti = "CT-2026-003",
            nik = "SJ001",
            nama = "Ahmad Fauzi",
            jenisCuti = "Cuti Tahunan",
            tglMulai = "2026-09-18",
            tglSelesai = "2026-09-19",
            alasan = "Liburan keluarga ke Bromo",
            status = "Pending"
          )
        )
        database.cutiDao().insertAll(cutis)

        // Seed Lembur
        val lemburs = listOf(
          LemburEntity(
            idLembur = "OT-2026-001",
            nik = "SJ001",
            nama = "Ahmad Fauzi",
            tanggal = "2026-09-02",
            jamMulai = "17:30",
            jamSelesai = "20:30",
            durasiJam = 3.0,
            deskripsi = "Migrasi server database dan backup bulanan sistem",
            statusPersetujuan = "Approved"
          ),
          LemburEntity(
            idLembur = "OT-2026-002",
            nik = "SJ005",
            nama = "Hendra Wijaya",
            tanggal = "2026-09-04",
            jamMulai = "16:30",
            jamSelesai = "19:00",
            durasiJam = 2.5,
            deskripsi = "Bongkar muat armada kiriman barang dari Surabaya",
            statusPersetujuan = "Pending"
          )
        )
        database.lemburDao().insertAll(lemburs)

        // Seed Payroll
        val payrolls = listOf(
          PayrollEntity(
            idPayroll = "PR-2026-08-SJ001",
            nik = "SJ001",
            nama = "Ahmad Fauzi",
            jabatan = "IT Specialist",
            periode = "2026-08",
            gajiPokok = 4500000L,
            tunjangan = 900000L,
            uangLembur = 350000L,
            potonganKasbon = 500000L,
            potonganLain = 120000L,
            gajiBersih = 5130000L,
            status = "Paid",
            tanggalBayar = "2026-08-28"
          ),
          PayrollEntity(
            idPayroll = "PR-2026-07-SJ001",
            nik = "SJ001",
            nama = "Ahmad Fauzi",
            jabatan = "IT Specialist",
            periode = "2026-07",
            gajiPokok = 4500000L,
            tunjangan = 900000L,
            uangLembur = 200000L,
            potonganKasbon = 0L,
            potonganLain = 120000L,
            gajiBersih = 5480000L,
            status = "Paid",
            tanggalBayar = "2026-07-28"
          ),
          PayrollEntity(
            idPayroll = "PR-2026-09-SJ001",
            nik = "SJ001",
            nama = "Ahmad Fauzi",
            jabatan = "IT Specialist",
            periode = "2026-09",
            gajiPokok = 4500000L,
            tunjangan = 900000L,
            uangLembur = 300000L,
            potonganKasbon = 750000L,
            potonganLain = 120000L,
            gajiBersih = 4830000L,
            status = "Draft"
          )
        )
        database.payrollDao().insertAll(payrolls)

        // Seed Announcements
        val announcements = listOf(
          AnnouncementEntity(
            judul = "Sistem Presensi V3 Berbasis GPS & Selfie",
            isi = "Diberitahukan kepada seluruh karyawan bahwa sistem presensi V3 Sukses Jaya resmi digunakan. Presensi wajib dilakukan dalam radius 100 meter dari kantor dengan verifikasi foto selfie.",
            kategori = "Penting",
            tanggal = "2026-09-01",
            target = "Semua"
          ),
          AnnouncementEntity(
            judul = "Pembaruan Fasilitas BPJS Kesehatan & Ketenagakerjaan",
            isi = "Kartu kepesertaan digital dan informasi Faskes Tingkat 1 kini dapat langsung dicek di menu Profil & BPJS pada aplikasi HRIS.",
            kategori = "HR",
            tanggal = "2026-09-03",
            target = "Semua"
          ),
          AnnouncementEntity(
            judul = "Pengajuan Kasbon & Lembur Melalui Aplikasi",
            isi = "Mulai bulan ini, permohonan salary advance (kasbon) dan surat perintah lembur diproses 100% digital tanpa formulir kertas.",
            kategori = "Operasional",
            tanggal = "2026-09-04",
            target = "Semua"
          )
        )
        database.announcementDao().insertAll(announcements)
      }
    }
  }
}
