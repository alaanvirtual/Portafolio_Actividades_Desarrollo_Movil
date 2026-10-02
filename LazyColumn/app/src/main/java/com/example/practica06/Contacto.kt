package com.example.practica06

data class Contacto(
    val id: Int,
    val nombre: String,
    val telefono: String,
    val correo: String
)

fun obtenerContactos(): List<Contacto> {
    return listOf(
        Contacto(1, "Ana Pérez", "555-1234", "ana.perez@example.com"),
        Contacto(2, "Carlos Gómez", "555-2345", "carlos.gomez@example.com"),
        Contacto(3, "María Rodríguez", "555-3456", "maria.rodriguez@example.com"),
        Contacto(4, "José Martínez", "555-4567", "jose.martinez@example.com"),
        Contacto(5, "Laura Hernández", "555-5678", "laura.hernandez@example.com"),
        Contacto(6, "David López", "555-6789", "david.lopez@example.com"),
        Contacto(7, "Sofía González", "555-7890", "sofia.gonzalez@example.com"),
        Contacto(8, "Alejandro Pérez", "555-8901", "alejandro.perez@example.com"),
        Contacto(9, "Carmen Sánchez", "555-9012", "carmen.sanchez@example.com"),
        Contacto(10, "Jorge Ramírez", "555-0123", "jorge.ramirez@example.com"),
        Contacto(11, "Lucía Torres", "555-1122", "lucia.torres@example.com"),
        Contacto(12, "Miguel Flores", "555-2233", "miguel.flores@example.com")
    )
}
