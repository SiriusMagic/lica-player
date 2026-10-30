import 'package:flutter/material.dart';

enum AppLanguage {
  spanish('es', 'Español'),
  english('en', 'English');

  const AppLanguage(this.code, this.label);

  final String code;
  final String label;

  Locale get locale => Locale(code);
}

class SrenStrings {
  const SrenStrings(this.currentLanguage);

  final AppLanguage currentLanguage;

  bool get isSpanish => currentLanguage == AppLanguage.spanish;

  String get chooseLanguage => isSpanish ? 'Elige tu idioma' : 'Choose your language';
  String get start => isSpanish ? 'Empezar' : 'Start';
  String get termsPrefix => isSpanish
      ? 'Al pulsar “Empezar”, reconoce que ha leído y aceptado nuestros '
      : 'By tapping “Start”, you acknowledge that you have read and accepted our ';
  String get terms => isSpanish ? 'Términos de Servicio' : 'Terms of Service';
  String get andText => isSpanish ? ' y ' : ' and ';
  String get privacy => isSpanish ? 'Política de privacidad' : 'Privacy Policy';

  String get playYourMusic => isSpanish ? 'Reproduce tu música' : 'Play your music';
  String get musicAccessMessage => isSpanish
      ? 'Déjanos acceder a tu música\npara reproducir tus canciones.'
      : 'Let us access your music\nto play your songs.';
  String get requestingPermissions =>
      isSpanish ? 'Solicitando permisos…' : 'Requesting permissions...';

  String get homeTitle => isSpanish ? 'Sren' : 'Sren';
  String get homeSubtitle =>
      isSpanish ? 'Tu música empezará aquí.' : 'Your music starts here.';
  String get fullAccessTitle =>
      isSpanish ? 'Acceso completo concedido' : 'Full access granted';
  String get fullAccessBody => isSpanish
      ? 'La base está lista. La siguiente imagen definirá esta pantalla principal.'
      : 'The base is ready. The next image will define this main screen.';
  String get limitedAccessTitle =>
      isSpanish ? 'Acceso limitado activo' : 'Limited access active';
  String get limitedAccessBody => isSpanish
      ? 'Sren puede abrirse, pero solo verá una parte de tus archivos. Puedes ampliar el permiso luego.'
      : 'Sren can open, but it will only see part of your files. You can expand access later.';
  String get deniedAccessTitle =>
      isSpanish ? 'Sin acceso a archivos' : 'No file access';
  String get deniedAccessBody => isSpanish
      ? 'La app entra igual, pero mostrará un estado vacío hasta que concedas acceso.'
      : 'The app still opens, but it will show an empty state until you grant access.';
  String get unknownAccessTitle =>
      isSpanish ? 'Permisos pendientes' : 'Permissions pending';
  String get unknownAccessBody => isSpanish
      ? 'Todavía no hemos confirmado el acceso multimedia.'
      : 'We have not confirmed media access yet.';

  String get grantAccess => isSpanish ? 'Conceder acceso' : 'Grant access';
  String get manageAccess => isSpanish ? 'Ampliar acceso' : 'Expand access';
  String get openSettings => isSpanish ? 'Abrir ajustes' : 'Open settings';
  String get settings => isSpanish ? 'Ajustes' : 'Settings';
  String get player => isSpanish ? 'Reproductor' : 'Player';
  String get languageLabel => isSpanish ? 'Idioma' : 'Language';
  String get legalLinks => isSpanish ? 'Enlaces legales' : 'Legal links';
  String get support => isSpanish ? 'Asistencia' : 'Support';
  String get notifications => isSpanish ? 'Notificaciones' : 'Notifications';
  String get notificationsGranted =>
      isSpanish ? 'Permitidas' : 'Allowed';
  String get notificationsDenied => isSpanish ? 'No permitidas' : 'Not allowed';
  String get close => isSpanish ? 'Cerrar' : 'Close';
  String get placeholderPlayerTitle =>
      isSpanish ? 'Reproductor en construcción' : 'Player under construction';
  String get placeholderPlayerBody => isSpanish
      ? 'Aquí conectaremos la experiencia del reproductor cuando compartas esa pantalla.'
      : 'We will connect the player experience here when you share that screen.';
  String get versionNote => isSpanish
      ? 'Base Flutter lista para iterar en vivo por ADB.'
      : 'Flutter base ready to iterate live over ADB.';
}
