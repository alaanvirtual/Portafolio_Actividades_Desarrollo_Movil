package com.example.redsocialnfc.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.redsocialnfc.data.Amigo
import com.example.redsocialnfc.data.VisibilidadMuro
import com.example.redsocialnfc.ui.theme.DarkBackground
import com.example.redsocialnfc.ui.theme.DarkOutline
import com.example.redsocialnfc.ui.theme.MutedAccent
import com.example.redsocialnfc.ui.theme.MutedPurple
import com.example.redsocialnfc.ui.theme.MutedPurpleLight
import com.example.redsocialnfc.ui.theme.SubtleGrayPurple
import com.example.redsocialnfc.ui.theme.TextPrimary
import com.example.redsocialnfc.ui.theme.TextSecondary

private data class OpcionVisibilidadUi(
    val codigo: String,
    val titulo: String,
    val descripcion: String
)

fun iconoPorVisibilidad(visibilidad: String): ImageVector = when (visibilidad) {
    VisibilidadMuro.AMIGOS -> Icons.Default.Group
    VisibilidadMuro.SELECCIONADOS -> Icons.Default.Person
    VisibilidadMuro.PRIVADO -> Icons.Default.Lock
    else -> Icons.Default.Public
}

@Composable
fun SelectorAudienciaMuro(
    visibilidad: String,
    permitidos: Set<String>,
    amigos: List<Amigo>,
    onCambioVisibilidad: (String) -> Unit,
    onToggleAmigo: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val opciones = listOf(
        OpcionVisibilidadUi(
            codigo = VisibilidadMuro.TODOS,
            titulo = "Todos (Público local)",
            descripcion = "Cualquier usuario cercano en Local puede verlo"
        ),
        OpcionVisibilidadUi(
            codigo = VisibilidadMuro.AMIGOS,
            titulo = "Solo amigos NFC",
            descripcion = "Únicamente contactos vinculados (${amigos.size})"
        ),
        OpcionVisibilidadUi(
            codigo = VisibilidadMuro.SELECCIONADOS,
            titulo = "Elegir amigos específicos",
            descripcion = "Marca exactamente quiénes pueden verlo"
        ),
        OpcionVisibilidadUi(
            codigo = VisibilidadMuro.PRIVADO,
            titulo = "Solo tú (Privado)",
            descripcion = "Solo visible para ti en este dispositivo"
        )
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        opciones.forEach { opcion ->
            val seleccionada = visibilidad == opcion.codigo
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (seleccionada) MutedPurple.copy(alpha = 0.18f) else DarkBackground,
                border = BorderStroke(1.dp, if (seleccionada) MutedPurple else DarkOutline),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCambioVisibilidad(opcion.codigo) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = seleccionada,
                        onClick = { onCambioVisibilidad(opcion.codigo) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = MutedPurpleLight,
                            unselectedColor = TextSecondary
                        ),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector = iconoPorVisibilidad(opcion.codigo),
                        contentDescription = null,
                        tint = if (seleccionada) MutedPurpleLight else TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = opcion.titulo,
                            fontSize = 12.sp,
                            fontWeight = if (seleccionada) FontWeight.Bold else FontWeight.Medium,
                            color = if (seleccionada) TextPrimary else TextSecondary
                        )
                        Text(
                            text = opcion.descripcion,
                            fontSize = 10.sp,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        if (visibilidad == VisibilidadMuro.SELECCIONADOS) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkBackground)
                    .border(1.dp, MutedPurple.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (amigos.isEmpty()) {
                    Text(
                        text = "Aún no tienes amigos NFC guardados. Vincula personas en la pestaña Amigos para seleccionarlos aquí (mientras tanto solo tú podrás verlo).",
                        fontSize = 11.sp,
                        color = MutedAccent
                    )
                } else {
                    Text(
                        text = "Seleccionados (${permitidos.size} de ${amigos.size}):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MutedPurpleLight
                    )
                    amigos.forEach { amigo ->
                        val marcado = amigo.handle in permitidos
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { onToggleAmigo(amigo.handle) }
                                .padding(vertical = 2.dp)
                        ) {
                            Checkbox(
                                checked = marcado,
                                onCheckedChange = { onToggleAmigo(amigo.handle) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MutedPurple,
                                    uncheckedColor = TextSecondary,
                                    checkmarkColor = Color.White
                                ),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = amigo.nombre.ifBlank { amigo.handle.removePrefix("@") },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = amigo.handle,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    if (permitidos.isEmpty()) {
                        Text(
                            text = "Marca al menos un amigo o solo tú podrás verlo.",
                            fontSize = 10.sp,
                            color = MutedAccent
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BadgeAudienciaPost(
    visibilidad: String,
    permitidosCsv: String,
    modifier: Modifier = Modifier
) {
    if (visibilidad == VisibilidadMuro.TODOS) return

    val etiqueta = when (visibilidad) {
        VisibilidadMuro.AMIGOS -> "Amigos"
        VisibilidadMuro.SELECCIONADOS -> {
            val total = VisibilidadMuro.parsearHandles(permitidosCsv).size
            if (total > 0) "$total amigo(s)" else "Específico"
        }
        VisibilidadMuro.PRIVADO -> "Solo tú"
        else -> return
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = SubtleGrayPurple,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
        ) {
            Icon(
                imageVector = iconoPorVisibilidad(visibilidad),
                contentDescription = null,
                tint = MutedPurpleLight,
                modifier = Modifier.size(10.dp)
            )
            Spacer(Modifier.width(3.dp))
            Text(
                text = etiqueta,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = MutedPurpleLight
            )
        }
    }
}
