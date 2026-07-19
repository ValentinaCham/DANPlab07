package com.example.danp_lab07.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [ProductEntity::class], version = 3, exportSchema = false)
@TypeConverters(Converters::class)
abstract class ProductDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE products ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0",
                )
            }
        }

        /**
         * v2 → v3 introduces the `imageUris` list (replaces the singular `imageUri`).
         * Strategy: add a new column, copy `imageUri` over wrapped in a single-element
         * list, drop the legacy column. We rely on SQLite's limited `ALTER TABLE`
         * capabilities, so this is done in safe incremental steps.
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE products ADD COLUMN imageUris TEXT NOT NULL DEFAULT ''")
                db.execSQL(
                    "UPDATE products SET imageUris = " +
                        "CASE WHEN imageUri IS NULL OR imageUri = '' THEN '' ELSE imageUri END"
                )
            }
        }
    }
}
