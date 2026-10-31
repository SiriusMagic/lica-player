# APK

Todos los APK, organizados por versión. **Esta carpeta solo existe en el Codespace** (git solo guarda este README). La copia permanente de cada versión publicada está en la pestaña **Releases** de GitHub.

Para pasar un APK al teléfono: en VS Code, clic derecho sobre el archivo → **Download**.

```
apk/
├── v0.2.0/
│   └── sren-music-0.2.0.apk   ← la app actual (base Lica, 2,9 MB)
└── legado/
    └── stone-1.3.2.apk        ← Stone original (React Native, 26 MB), solo para comparar
```

## Reglas
- Una carpeta por versión: `vX.Y.Z/<nombre>-X.Y.Z.apk`, con la versión de `versionName` en `stone/app/build.gradle.kts`.
- Aquí no van APK de otras apps.
- Cómo se compila: [`docs/COMPILAR.md`](../docs/COMPILAR.md).
