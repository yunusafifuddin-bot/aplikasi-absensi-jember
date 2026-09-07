package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EmployeeDao {
  @Query("SELECT * FROM employees ORDER BY nik ASC") fun getAllEmployees(): Flow<List<EmployeeEntity>>
  @Query("SELECT * FROM employees WHERE nik = :nik LIMIT 1") suspend fun getEmployeeByNik(nik: String): EmployeeEntity?
  @Query("SELECT * FROM employees WHERE nik = :nik LIMIT 1") fun observeEmployeeByNik(nik: String): Flow<EmployeeEntity?>
  @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertOrUpdate(employee: EmployeeEntity)
  @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAll(employees: List<EmployeeEntity>)
  @Query("DELETE FROM employees WHERE nik = :nik") suspend fun deleteByNik(nik: String)
  @Query("DELETE FROM employees") suspend fun clearAll()
}

@Dao
interface ShiftDao {
  @Query("SELECT * FROM shifts ORDER BY shiftId ASC") fun getAllShifts(): Flow<List<ShiftEntity>>
  @Query("SELECT * FROM shifts WHERE shiftId = :shiftId LIMIT 1") suspend fun getShiftById(shiftId: String): ShiftEntity?
  @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertOrUpdate(shift: ShiftEntity)
  @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAll(shifts: List<ShiftEntity>)
  @Query("DELETE FROM shifts WHERE shiftId = :shiftId") suspend fun deleteById(shiftId: String)
  @Query("DELETE FROM shifts") suspend fun clearAll()
}

@Dao
interface AttendanceDao {
  @Query("SELECT * FROM attendances WHERE nik = :nik ORDER BY tanggal DESC") fun getAttendancesByNik(nik: String): Flow<List<AttendanceEntity>>
  @Query("SELECT * FROM attendances ORDER BY tanggal DESC") fun getAllAttendances(): Flow<List<AttendanceEntity>>
  @Query("SELECT * FROM attendances WHERE nik = :nik AND tanggal = :tanggal LIMIT 1") suspend fun getAttendance(nik: String, tanggal: String): AttendanceEntity?
  @Query("SELECT * FROM attendances WHERE nik = :nik AND tanggal = :tanggal LIMIT 1") fun observeAttendance(nik: String, tanggal: String): Flow<AttendanceEntity?>
  @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertOrUpdate(attendance: AttendanceEntity)
  @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAll(attendances: List<AttendanceEntity>)
  @Query("DELETE FROM attendances") suspend fun clearAll()
}

@Dao
interface KasbonDao {
  @Query("SELECT * FROM kasbons WHERE nik = :nik ORDER BY tanggalPengajuan DESC") fun getKasbonsByNik(nik: String): Flow<List<KasbonEntity>>
  @Query("SELECT * FROM kasbons ORDER BY tanggalPengajuan DESC") fun getAllKasbons(): Flow<List<KasbonEntity>>
  @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(kasbon: KasbonEntity)
  @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAll(kasbons: List<KasbonEntity>)
  @Query("UPDATE kasbons SET statusPersetujuan = :status WHERE idKasbon = :id") suspend fun updateStatus(id: String, status: String)
  @Query("DELETE FROM kasbons") suspend fun clearAll()
}

@Dao
interface CutiDao {
  @Query("SELECT * FROM cutis WHERE nik = :nik ORDER BY tglMulai DESC") fun getCutisByNik(nik: String): Flow<List<CutiEntity>>
  @Query("SELECT * FROM cutis ORDER BY tglMulai DESC") fun getAllCutis(): Flow<List<CutiEntity>>
  @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(cuti: CutiEntity)
  @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAll(cutis: List<CutiEntity>)
  @Query("UPDATE cutis SET status = :status WHERE idCuti = :id") suspend fun updateStatus(id: String, status: String)
  @Query("DELETE FROM cutis") suspend fun clearAll()
}

@Dao
interface LemburDao {
  @Query("SELECT * FROM lemburs WHERE nik = :nik ORDER BY tanggal DESC") fun getLembursByNik(nik: String): Flow<List<LemburEntity>>
  @Query("SELECT * FROM lemburs ORDER BY tanggal DESC") fun getAllLemburs(): Flow<List<LemburEntity>>
  @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(lembur: LemburEntity)
  @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAll(lemburs: List<LemburEntity>)
  @Query("UPDATE lemburs SET statusPersetujuan = :status WHERE idLembur = :id") suspend fun updateStatus(id: String, status: String)
  @Query("DELETE FROM lemburs") suspend fun clearAll()
}

@Dao
interface PayrollDao {
  @Query("SELECT * FROM payrolls WHERE nik = :nik ORDER BY periode DESC") fun getPayrollsByNik(nik: String): Flow<List<PayrollEntity>>
  @Query("SELECT * FROM payrolls WHERE periode = :periode ORDER BY nik ASC") fun getPayrollsByPeriode(periode: String): Flow<List<PayrollEntity>>
  @Query("SELECT * FROM payrolls ORDER BY periode DESC") fun getAllPayrolls(): Flow<List<PayrollEntity>>
  @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(payroll: PayrollEntity)
  @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAll(payrolls: List<PayrollEntity>)
  @Query("UPDATE payrolls SET status = :status, tanggalBayar = :tanggalBayar WHERE idPayroll = :id") suspend fun updateStatus(id: String, status: String, tanggalBayar: String?)
  @Query("DELETE FROM payrolls") suspend fun clearAll()
}

@Dao
interface AnnouncementDao {
  @Query("SELECT * FROM announcements ORDER BY id DESC") fun getAllAnnouncements(): Flow<List<AnnouncementEntity>>
  @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(announcement: AnnouncementEntity)
  @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAll(announcements: List<AnnouncementEntity>)
  @Query("DELETE FROM announcements") suspend fun clearAll()
}
