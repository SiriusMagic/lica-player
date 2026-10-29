# Sren music — Reproductor de música

**Sren music** es un reproductor de música y video para Android, sin anuncios, pensado para gastar lo mínimo de batería, memoria y datos. Antes se llamaba **Lica**.

## Dónde está cada cosa

- [`sren/`](./sren): la app Android (Kotlin + Jetpack Compose + Media3).
- [`sren-music/`](./sren-music): copia de referencia del código original de Sren, mientras sus funciones se pasan a la app. No se compila.

## Objetivo

- Reproducir la música y los videos del teléfono, sin anuncios y sin nube obligatoria.
- Durar muchas horas reproduciendo: cada función tiene que justificar lo que gasta.

## Herramientas

La carpeta [`herramientas/`](./herramientas) reúne prototipos de apoyo: **SARAP**, un asistente que convierte frases en acciones de la app, y un cuaderno de Colab para armar una **biblioteca musical** clasificada por estado de ánimo.

## Licencia

Copyright 2026 SiriusMagic

Este proyecto se distribuye bajo la **Licencia Apache 2.0**. Consulta el archivo [`LICENSE`](./LICENSE) para ver el texto completo.

En resumen: cualquiera puede usar, copiar, modificar y distribuir este código, siempre que conserve el aviso de copyright y la licencia. El software se entrega **"tal cual", sin garantías de ningún tipo**, y los autores no se hacen responsables de daños derivados de su uso.

## Seguridad

¿Encontraste una vulnerabilidad? Lee [`SECURITY.md`](./SECURITY.md) y repórtala de forma privada.
