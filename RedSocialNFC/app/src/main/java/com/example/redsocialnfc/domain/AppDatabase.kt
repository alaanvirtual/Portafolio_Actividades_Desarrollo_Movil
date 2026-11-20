package com.example.redsocialnfc.domain

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.redsocialnfc.data.Amigo
import com.example.redsocialnfc.data.AmigoDao
import com.example.redsocialnfc.data.Mensaje
import com.example.redsocialnfc.data.MensajeDao
import com.example.redsocialnfc.data.Publicacion
import com.example.redsocialnfc.data.PublicacionDao
import com.example.redsocialnfc.data.Usuario
import com.example.redsocialnfc.data.UsuarioDao

@Database(
    entities = [Usuario::class, Publicacion::class, Mensaje::class, Amigo::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun publicacionDao(): PublicacionDao
    abstract fun mensajeDao(): MensajeDao
    abstract fun amigoDao(): AmigoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "red_social_nfc_db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
