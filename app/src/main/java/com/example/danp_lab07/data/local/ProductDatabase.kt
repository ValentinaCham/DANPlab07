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
         * To match Room's expected schema exactly (no legacy columns, no SQL-level
         * default values), we recreate the table.
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Create the new table matching the v3 Entity exactly (no SQL defaults)
                db.execSQL(
                    """
                    CREATE TABLE products_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        price REAL NOT NULL,
                        description TEXT NOT NULL,
                        category TEXT NOT NULL,
                        imageUris TEXT NOT NULL,
                        isSynced INTEGER NOT NULL,
                        isDeleted INTEGER NOT NULL
                    )
                    """.trimIndent()
                )

                // 2. Copy data from old table. 
                // imageUris is populated from the old imageUri column.
                // We use the same 'isSynced' and 'isDeleted' (which were added in v2).
                db.execSQL(
                    """
                    INSERT INTO products_new (id, name, price, description, category, isSynced, isDeleted, imageUris)
                    SELECT id, name, price, description, category, isSynced, isDeleted, 
                           COALESCE(imageUri, '') 
                    FROM products
                    """.trimIndent()
                )

                // 3. Remove old table and rename new one
                db.execSQL("DROP TABLE products")
                db.execSQL("ALTER TABLE products_new RENAME TO products")
            }
        }
    }
}
