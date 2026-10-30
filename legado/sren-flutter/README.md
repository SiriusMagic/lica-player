# Sren Flutter (28 jun 2026)

Prototipo de Sren en Flutter (carpeta `Documents\Codex\2026-06-28\es\work\sren` de la PC). Paquete `app.sren.mobile`.

## Qué tenía
- **Bienvenida**: "Elige tu idioma" (español / inglés) y aceptación de Términos y Privacidad.
- **Permisos**: pantalla "Reproduce tu música" y estados de acceso: completo, limitado, denegado o pendiente. Estado de notificaciones.
- **Inicio** con el logo del pingüino (`assets/images/sren_logo.jpg`) y la tarjeta de estado de permisos.
- **Ajustes**: idioma y enlaces legales (`assets/config/app_config.json`, con direcciones provisionales `gob.ec`).
- Reproductor: solo un aviso de "en construcción".
- Puente nativo (`android/.../MainActivity.kt`, canal `app.sren.mobile/platform`) para permisos, ajustes del sistema y abrir enlaces.

## Qué se guardó
`lib/`, `assets/`, `test/`, `pubspec.yaml`, `pubspec.lock`, `analysis_options.yaml` y `android/` (sin `local.properties`, que tenía rutas de la PC). No se guardaron las carpetas `ios/`, `macos/`, `windows/`, `linux/` y `web/`: son plantillas que Flutter genera solo con `flutter create .`.

## Correcciones para que compile (2026-10-29)
1. `MainActivity` heredaba de `FlutterActivity`, que no permite `registerForActivityResult`: se cambió a `FlutterFragmentActivity` (marcado con un comentario).
2. En la PC, `JAVA_HOME` apuntaba a una carpeta que no existía, y por eso nunca se generó un APK.

## Cómo compilarla
```bash
cd legado/sren-flutter
flutter create --platforms=android .   # regenera lo que falte
printf 'sdk.dir=%s\nflutter.sdk=%s\n' "$ANDROID_HOME" "$(dirname $(dirname $(which flutter)))" > android/local.properties
flutter build apk --release --target-platform android-arm64
```
