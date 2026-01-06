package com.wilkins.safezone.frontend.ui.user.News

data class News(
    val id: Int,
    val title: String,
    val description: String,
    val imageUrl: String,
    val date: String,
    val author: String,
    val authorAvatar: String,
    val likes: Int,
    val comments: List<Comment>,
    val category: String
)

data class Comment(
    val id: Int,
    val author: String,
    val content: String,
    val timestamp: String,
    val likes: Int,
    val replies: List<Comment> = emptyList()
)

object NewsData {
    val featuredNews = News(
        id = 1,
        title = "Alerta de Seguridad en la Zona Este",
        description = "Las autoridades han emitido una alerta de seguridad para la zona este de la ciudad debido a reportes de actividad sospechosa. Se recomienda a los residentes mantenerse vigilantes y reportar cualquier comportamiento inusual.",
        imageUrl = "",
        date = "Hace 2 horas",
        author = "Dra. María Rodríguez",
        authorAvatar = "",
        likes = 245,
        category = "Seguridad",
        comments = listOf(
            Comment(
                id = 1,
                author = "Juan Pérez",
                content = "Gracias por la información, estaré atento en mi zona",
                timestamp = "Hace 1 hora",
                likes = 12,
                replies = listOf(
                    Comment(
                        id = 11,
                        author = "Ana López",
                        content = "Yo también, mejor prevenir",
                        timestamp = "Hace 45 min",
                        likes = 5
                    ),
                    Comment(
                        id = 12,
                        author = "Pedro Castro",
                        content = "¿Tienen más detalles sobre la zona exacta?",
                        timestamp = "Hace 30 min",
                        likes = 3
                    )
                )
            ),
            Comment(
                id = 2,
                author = "María González",
                content = "¿Alguien sabe más detalles sobre el tipo de actividad?",
                timestamp = "Hace 45 min",
                likes = 8,
                replies = listOf(
                    Comment(
                        id = 13,
                        author = "Carlos Rodríguez",
                        content = "Vi patrullas en el área esta mañana cerca del parque central",
                        timestamp = "Hace 30 min",
                        likes = 15
                    )
                )
            ),
            Comment(
                id = 3,
                author = "Roberto Díaz",
                content = "Excelente trabajo del equipo de seguridad manteniéndonos informados",
                timestamp = "Hace 20 min",
                likes = 20
            )
        )
    )

    val latestNews = listOf(
        News(
            id = 2,
            title = "Nuevo Sistema de Alertas Comunitarias",
            description = "Se implementará un sistema innovador de alertas que permitirá a los vecinos comunicarse en tiempo real ante situaciones de emergencia.",
            imageUrl = "",
            date = "Hace 3 horas",
            author = "Ing. Carlos Mendoza",
            authorAvatar = "",
            likes = 189,
            category = "Actualizaciones",
            comments = listOf(
                Comment(
                    id = 4,
                    author = "Ana López",
                    content = "Excelente iniciativa para nuestra comunidad",
                    timestamp = "Hace 2 horas",
                    likes = 25,
                    replies = listOf(
                        Comment(
                            id = 14,
                            author = "Luis Torres",
                            content = "Totalmente de acuerdo, muy necesario",
                            timestamp = "Hace 1 hora",
                            likes = 8
                        )
                    )
                ),
                Comment(
                    id = 5,
                    author = "Pedro Martínez",
                    content = "¿Cómo funciona exactamente? ¿Es una app móvil?",
                    timestamp = "Hace 1 hora",
                    likes = 10
                )
            )
        ),
        News(
            id = 3,
            title = "Reunión Vecinal Exitosa con Record de Asistencia",
            description = "La reunión de seguridad vecinal superó todas las expectativas con más de 150 participantes comprometidos con la seguridad del barrio.",
            imageUrl = "",
            date = "Hace 5 horas",
            author = "Lic. Sandra Morales",
            authorAvatar = "",
            likes = 312,
            category = "Comunidad",
            comments = listOf(
                Comment(
                    id = 6,
                    author = "Luis Fernández",
                    content = "Fue muy productiva, se tomaron decisiones importantes",
                    timestamp = "Hace 4 horas",
                    likes = 18,
                    replies = listOf(
                        Comment(
                            id = 15,
                            author = "Carmen Silva",
                            content = "¿Cuáles fueron las principales decisiones?",
                            timestamp = "Hace 3 horas",
                            likes = 6
                        )
                    )
                )
            )
        ),
        News(
            id = 4,
            title = "Refuerzo de Patrullaje en Horario Nocturno",
            description = "Las autoridades aumentarán significativamente el patrullaje nocturno en las principales avenidas del sector norte de la ciudad.",
            imageUrl = "",
            date = "Hace 8 horas",
            author = "Cap. Roberto Vargas",
            authorAvatar = "",
            likes = 428,
            category = "Alertas",
            comments = listOf(
                Comment(
                    id = 7,
                    author = "Carmen Silva",
                    content = "Me siento mucho más segura con esta medida",
                    timestamp = "Hace 7 horas",
                    likes = 32
                ),
                Comment(
                    id = 8,
                    author = "Roberto Díaz",
                    content = "Muy necesario en estos tiempos, gracias por la acción rápida",
                    timestamp = "Hace 6 horas",
                    likes = 28
                )
            )
        ),
        News(
            id = 5,
            title = "Inscripciones Abiertas: Taller de Primeros Auxilios",
            description = "La comunidad ofrece capacitación gratuita en primeros auxilios y manejo de emergencias. Cupos limitados disponibles.",
            imageUrl = "",
            date = "Hace 12 horas",
            author = "Dra. Patricia Gómez",
            authorAvatar = "",
            likes = 156,
            category = "Eventos",
            comments = listOf(
                Comment(
                    id = 9,
                    author = "Sandra Morales",
                    content = "¿Dónde exactamente me puedo inscribir? Muy interesada",
                    timestamp = "Hace 10 horas",
                    likes = 14,
                    replies = listOf(
                        Comment(
                            id = 16,
                            author = "Dra. Patricia Gómez",
                            content = "En la oficina comunitaria o por el portal web",
                            timestamp = "Hace 9 horas",
                            likes = 22
                        )
                    )
                )
            )
        ),
        News(
            id = 6,
            title = "Reporte: Incidente Menor en Sector Sur Resuelto",
            description = "Un incidente menor reportado en el sector sur fue atendido rápidamente por las autoridades. No hubo heridos y la situación está bajo control.",
            imageUrl = "",
            date = "Hace 15 horas",
            author = "Oficial José Ramírez",
            authorAvatar = "",
            likes = 89,
            category = "Alertas",
            comments = listOf(
                Comment(
                    id = 17,
                    author = "Miguel Ángel",
                    content = "Buena respuesta de las autoridades",
                    timestamp = "Hace 14 horas",
                    likes = 7
                )
            )
        ),
        News(
            id = 7,
            title = "Jornada de Limpieza Comunitaria Este Sábado",
            description = "Invitamos a todos los vecinos a participar en la jornada de limpieza y embellecimiento del parque central este sábado a las 8:00 AM.",
            imageUrl = "",
            date = "Hace 1 día",
            author = "Comité Vecinal",
            authorAvatar = "",
            likes = 203,
            category = "Eventos",
            comments = listOf(
                Comment(
                    id = 18,
                    author = "Laura Martínez",
                    content = "¡Allí estaré con mi familia!",
                    timestamp = "Hace 20 horas",
                    likes = 15
                )
            )
        )
    )
}