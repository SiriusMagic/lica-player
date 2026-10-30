# APK de Sren music

Todos los APK, organizados por versión. **Esta carpeta solo existe en el Codespace** (git solo guarda este README). La copia permanente de cada versión publicada está en la pestaña **Releases** de GitHub.

Para pasar un APK al teléfono: en VS Code, clic derecho sobre el archivo → **Download**.

## Estructura

```
apk/
├── v0.2.0/
│   └── sren-music-0.2.0.apk      ← la app actual (Sren music)
└── legado/                       ← versiones antiguas, solo para comparar
    ├── sren-kotlin-1.0.0.apk     ← la versión de Android Studio (com.sren.music.kotlin)
    ├── sren-source-0.1.0.apk     ← variante con el asistente completo (com.sren)
    └── sren-flutter-1.0.0.apk    ← Flutter: idioma, permisos y pingüino (app.sren.mobile)
```

## Reglas
- Una carpeta por versión: `vX.Y.Z/sren-music-X.Y.Z.apk`. La versión es la de `versionName` en `sren/app/build.gradle.kts`.
- Los APK de `legado/` tienen paquetes distintos, así que se instalan **junto** a la app actual sin reemplazarla.
- Aquí no van APK de otras apps: así no se vuelven a confundir con Sren.
- Cómo se compila cada uno: [`docs/COMPILAR.md`](../docs/COMPILAR.md) y los README de [`legado/`](../legado).
