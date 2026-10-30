# Arquitectura de Sren music

Mapa del código de la app, para saber **qué hace cada parte y en qué archivo seguir escribiendo**. Todo el código está en `sren/app/src/main/java/com/sren/music/` (en este documento se omite ese prefijo).

## La idea en una frase
Sren lee la música que Android ya conoce (MediaStore), la guarda en una base de datos local para abrir al instante, y la reproduce desde un **servicio** que sigue sonando con la pantalla apagada, gastando lo mínimo.

## Cómo fluye todo

```
Archivos del teléfono
  └─ MediaStore (índice de Android)
       └─ data/local/mediastore/MediaStoreScanner ──► data/repository/MediaRepositoryImpl
                                                        └─ library.db (caché: canciones, videos)
                                                             └─ ViewModels ──► Pantallas (Compose)

Pantallas ──► playback/PlayerViewModel ──► playback/PlaybackConnection (MediaController)
                                              └─ playback/PlaybackService (MediaSession + ExoPlayer)
                                                   ├─ PlayTracker ──► stats.db (historial, favoritas)
                                                   └─ SleepTimerEnforcer (temporizador)
```

## Las dos bases de datos (importante)

| Archivo | Clase | Qué guarda | ¿Se puede borrar? |
|---|---|---|---|
| `library.db` | `data/local/db/LibraryDatabase` | Copia de la biblioteca (canciones, videos) | **Sí.** Es una caché: si cambia su estructura se borra y se vuelve a escanear sola. |
| `stats.db` | `data/user/UserDatabase` | Datos del usuario: veces escuchada, última vez, **favoritas** | **Nunca.** Cada cambio de estructura necesita una migración (`MIGRATION_x_y`) y su esquema se guarda en `sren/app/schemas/`. |

Preferencias pequeñas (perfil de audio, orden, recomendación diaria…) van en SharedPreferences, en `data/settings/AppSettings`.

## Paquetes y archivos

### Arranque
| Archivo | Qué hace |
|---|---|
| `SrenApplication.kt` | Inicia Hilt (inyección de dependencias) y configura Coil (portadas: 10 % de memoria, 32 MB en disco). |
| `MainActivity.kt` | La única pantalla de Android. Limita a 60 Hz en el perfil "Máxima duración", abre la canción de la notificación diaria y reprograma esa notificación. |

### `data/` — datos
| Archivo | Qué hace |
|---|---|
| `local/mediastore/MediaStoreScanner.kt` | Consulta MediaStore (canciones, videos, carpeta de cada canción). Avisa de cambios solo mientras la app está visible. |
| `local/mediastore/LibraryStamp.kt` | "Huella" de la biblioteca: decide si hay que escanear todo, solo lo cambiado o nada. |
| `local/db/LibraryDatabase.kt`, `dao/SongDao.kt`, `dao/VideoDao.kt` | Caché `library.db` y sus consultas (álbumes, artistas, carpetas, búsqueda). |
| `model/` | `Song`, `Video`, `Album`, `Artist`, `Folder`. |
| `repository/MediaRepository.kt` (+`Impl`) | Punto único para leer la biblioteca y sincronizarla. |
| `settings/AppSettings.kt` | Preferencias: perfil de audio, orden de canciones, recomendación diaria. |
| `settings/AudioProfile.kt` | Perfiles Máxima duración / Equilibrado / Alta fidelidad. |
| `settings/SongSort.kt` | Órdenes de la lista de canciones (con reglas del idioma: "Álvaro" junto a "Alvaro"). |
| `user/UserDatabase.kt` | `stats.db`: tablas `play_stats` y `favorites`, y sus migraciones. |
| `user/UserLibrary.kt` | Une los datos del usuario con la biblioteca (favoritas, recientes, más escuchadas). |

### `playback/` — reproducción
| Archivo | Qué hace |
|---|---|
| `PlaybackService.kt` | Servicio en primer plano con MediaSession: notificación, pantalla de bloqueo, audífonos, Bluetooth. ExoPlayer solo de audio; *offload* según el perfil. |
| `PlaybackConnection.kt` | Un solo `MediaController` compartido por toda la app. |
| `PlayerViewModel.kt` | Estado del reproductor para las pantallas y todas las acciones (reproducir, cola, aleatorio, velocidad, temporizador…). |
| `PlayerUiState.kt` | Datos que ven las pantallas (canción actual, cola). |
| `PlayTracker.kt` | Cuenta una canción como escuchada a los 30 s (o la mitad), sin temporizadores. |
| `SleepTimer.kt` | Temporizador para dormir: pausa al terminar la canción en la que se acaba el tiempo. |
| `PaletteCache.kt` | Color de fondo según la portada (calculado una vez por álbum). |

### `suggestion/` — recomendación diaria
`SuggestionPicker.kt` (elige la canción), `SuggestionNotifier.kt` (muestra la notificación), `SuggestionScheduler.kt` (alarma diaria flexible ~18:00, receptores de alarma y de reinicio).

### `ui/` — pantallas
| Archivo | Qué hace |
|---|---|
| `navigation/SrenNavHost.kt` | Estructura de la app: barra superior, mini reproductor, pestañas y rutas (`home`, `library`, `search`, `settings`, `player`, `collection/{tipo}/{clave}`). |
| `navigation/Destinations.kt` | Las pestañas de abajo: Inicio, Biblioteca, Buscar, Ajustes. |
| `screens/PermissionGate.kt` | Pide el permiso de música antes de mostrar la app. |
| `screens/home/` | Inicio: bienvenida, recomendación, filas de historial y favoritas. |
| `screens/library/LibraryScreen.kt` | Biblioteca y sus subpestañas (Canciones, Favoritas, Carpetas, Artistas, Álbumes, Videos). |
| `screens/library/CollectionAndSearch.kt` | Un álbum/artista/carpeta abierto, y la pantalla Buscar. |
| `screens/library/LibraryViewModels.kt` | Los ViewModels de esas pantallas. |
| `screens/settings/SettingsScreen.kt` | Ajustes. |
| `screens/videos/` | Lista de videos. |
| `player/NowPlayingBar.kt` | Mini reproductor. |
| `player/PlayerScreen.kt` | Reproductor completo y sus hojas (velocidad, temporizador, cola). |
| `components/SongList.kt` | Lista y fila de canción reutilizables, botones "Reproducir todo / Aleatorio". |
| `components/SongActionsSheet.kt` | Menú de acciones de una canción. |
| `components/AlbumArt.kt` | Portada con tamaño de decodificación limitado. |
| `theme/` | Colores y tema. |

### Recursos (`sren/app/src/main/res/`)
- Textos: `values/strings.xml` (inglés) y `values-es/strings.xml` (español). **Todo texto visible va aquí**, nunca escrito en el código.
- Íconos: `drawable/ic_launcher_*`, `mipmap*/`, `drawable/ic_stat_music.xml` (barra de estado).

## Dónde agregar cada cosa
| Quiero… | Dónde |
|---|---|
| Una pantalla nueva | `ui/screens/<nombre>/` + su ruta en `ui/navigation/SrenNavHost.kt` (y en `Destinations.kt` si es pestaña). |
| Un ajuste nuevo | Valor en `data/settings/AppSettings.kt` + fila en `ui/screens/settings/SettingsScreen.kt`. |
| Guardar datos del usuario | Entidad y DAO en `data/user/UserDatabase.kt`, **subir la versión y escribir la migración**; compilar para regenerar `sren/app/schemas/`. |
| Una consulta de la biblioteca | `data/local/db/dao/SongDao.kt` + `MediaRepository`. |
| Una acción del reproductor | `playback/PlayerViewModel.kt` (si necesita ExoPlayer directamente, en `PlaybackService.kt`). |
| Una opción del menú de canción | `ui/components/SongActionsSheet.kt`. |
| Un gesto | Ver [`GESTOS.md`](GESTOS.md). |
| Un texto | `res/values/strings.xml` y `res/values-es/strings.xml`. |

## Reglas que no se rompen
1. **Batería primero:** nada se ejecuta en segundo plano sin necesidad; nada de temporizadores que revisen cada segundo; las listas leen datos solo mientras se ven.
2. **Un solo proceso:** el servicio y la interfaz comparten proceso.
3. **Sin anuncios ni analíticas.**
4. **`stats.db` nunca se borra:** siempre migraciones.
5. **Quitar lo que no se usa** (archivos y librerías).
