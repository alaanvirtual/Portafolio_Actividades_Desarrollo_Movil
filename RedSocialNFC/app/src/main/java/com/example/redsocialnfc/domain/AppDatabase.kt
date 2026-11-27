package com.example.redsocialnfc.domain

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.redsocialnfc.data.Amigo
import com.example.redsocialnfc.data.AmigoDao
import com.example.redsocialnfc.data.Categoria
import com.example.redsocialnfc.data.CategoriaDao
import com.example.redsocialnfc.data.Comentario
import com.example.redsocialnfc.data.ComentarioDao
import com.example.redsocialnfc.data.Mensaje
import com.example.redsocialnfc.data.MensajeDao
import com.example.redsocialnfc.data.Publicacion
import com.example.redsocialnfc.data.PublicacionDao
import com.example.redsocialnfc.data.Usuario
import com.example.redsocialnfc.data.UsuarioDao

@Database(
    entities = [
        Usuario::class,
        Publicacion::class,
        Comentario::class,
        Categoria::class,
        Mensaje::class,
        Amigo::class
    ],
    version = 9,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun publicacionDao(): PublicacionDao
    abstract fun comentarioDao(): ComentarioDao
    abstract fun categoriaDao(): CategoriaDao
    abstract fun mensajeDao(): MensajeDao
    abstract fun amigoDao(): AmigoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `categorias` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `nombre` TEXT NOT NULL, `autorId` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL)"
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_categorias_nombre` ON `categorias` (`nombre`)"
                )
            }
        }

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `publicaciones` ADD COLUMN `fijado` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `publicaciones` ADD COLUMN `encuestaOpciones` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `publicaciones` ADD COLUMN `encuestaVotos` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `publicaciones` ADD COLUMN `encuestaVotoUsuario` INTEGER NOT NULL DEFAULT -1")
                db.execSQL("ALTER TABLE `publicaciones` ADD COLUMN `citadoAutor` TEXT")
                db.execSQL("ALTER TABLE `publicaciones` ADD COLUMN `citadoContenido` TEXT")
                db.execSQL("ALTER TABLE `mensajes` ADD COLUMN `leido` INTEGER NOT NULL DEFAULT 1")
            }
        }

        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `categorias` ADD COLUMN `visibilidad` TEXT NOT NULL DEFAULT 'TODOS'")
                db.execSQL("ALTER TABLE `categorias` ADD COLUMN `permitidosHandles` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `publicaciones` ADD COLUMN `audiencia` TEXT NOT NULL DEFAULT 'TODOS'")
                db.execSQL("ALTER TABLE `publicaciones` ADD COLUMN `audienciaPermitidos` TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "red_social_nfc_db"
                )
                    .addMigrations(MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9)
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
