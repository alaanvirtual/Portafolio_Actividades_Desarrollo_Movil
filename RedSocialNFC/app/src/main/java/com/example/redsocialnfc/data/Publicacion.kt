package com.example.redsocialnfc.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

object MediaTipoPost {
    const val NINGUNO = "NINGUNO"
    const val IMAGEN = "IMAGEN"
    const val VIDEO = "VIDEO"
}

@Entity(tableName = "publicaciones")
data class Publicacion(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val autorId: Int,
    val autorNombre: String = "",
    val autorHandle: String,
    val autorAvatarUri: String? = null,
    val autorAnonimo: String,
    val contenido: String,
    val categoria: String = "Principal",
    val etiquetaLugar: String? = null,
    val esAmigoDirecto: Boolean = true,
    val mediaTipo: String = MediaTipoPost.NINGUNO,
    val mediaUri: String? = null,
    val likesCount: Int = 0,
    val likedByMe: Boolean = false,
    val repostsCount: Int = 0,
    val repostedByMe: Boolean = false,
    val guardado: Boolean = false,
    @ColumnInfo(defaultValue = "0")
    val fijado: Boolean = false,
    @ColumnInfo(defaultValue = "")
    val encuestaOpciones: String = "",
    @ColumnInfo(defaultValue = "")
    val encuestaVotos: String = "",
    @ColumnInfo(defaultValue = "-1")
    val encuestaVotoUsuario: Int = -1,
    val citadoAutor: String? = null,
    val citadoContenido: String? = null,
    @ColumnInfo(defaultValue = "TODOS")
    val audiencia: String = VisibilidadMuro.TODOS,
    @ColumnInfo(defaultValue = "")
    val audienciaPermitidos: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
