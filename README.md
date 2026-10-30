# Stone — Reproductor de música

**Stone** es un reproductor de música y video para Android, sin anuncios, pensado para gastar lo mínimo de batería, memoria y datos. **Stone 1.4** (en curso) tiene el aspecto de Stone 1.3.2 y el motor de Lica (antes Sren music).

Para ver qué apartados de Stone ya están: `python3 herramientas/verificar_stone.py` (lista completa en [docs/STONE-1.4.md](docs/STONE-1.4.md)).

## Dónde está cada cosa

- [`stone/`](./stone): la app Android (Kotlin + Jetpack Compose + Media3).
- [`docs/`](./docs): cómo está hecha ([arquitectura](docs/ARQUITECTURA.md), [gestos](docs/GESTOS.md)), [cómo compilarla](docs/COMPILAR.md), [**especificación de Stone 1.4**](docs/STONE-1.4.md), [historia de versiones](docs/VERSIONES.md) e [inventario de carpetas](docs/INVENTARIO.md).
- [`legado/stone/`](./legado/stone): código de **Stone 1.3.2**, la referencia de aspecto y funciones que se pasan a la app. Solo referencia.
- [`CHANGELOG.md`](./CHANGELOG.md): qué cambió en cada versión.

## Descargar la app

La última versión está en la sección **[Releases](https://github.com/SiriusMagic/lica-player/releases)** (archivo `stone-X.Y.Z.apk`).

## Objetivo

- Reproducir la música y los videos del teléfono, sin anuncios y sin nube obligatoria.
- Durar muchas horas reproduciendo: cada función tiene que justificar lo que gasta.

## Licencia

Copyright 2026 SiriusMagic

Este proyecto se distribuye bajo la **Licencia Apache 2.0**. Consulta el archivo [`LICENSE`](./LICENSE) para ver el texto completo.

En resumen: cualquiera puede usar, copiar, modificar y distribuir este código, siempre que conserve el aviso de copyright y la licencia. El software se entrega **"tal cual", sin garantías de ningún tipo**, y los autores no se hacen responsables de daños derivados de su uso.

## Seguridad

¿Encontraste una vulnerabilidad? Lee [`SECURITY.md`](./SECURITY.md) y repórtala de forma privada.
