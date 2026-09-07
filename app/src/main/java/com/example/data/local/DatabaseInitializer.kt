package com.example.data.local

/**
 * Local database is not a data source for the application.
 * Google Sheets / Google Apps Script is authoritative.
 * Kept as a compatibility shim for existing startup code; intentionally does nothing.
 */
object DatabaseInitializer {
  suspend fun preseedIfEmpty(database: AppDatabase) {
    // Intentionally empty: never seed demo/local attendance, employee or payroll data.
  }
}
