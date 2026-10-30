import 'dart:convert';

import 'package:flutter/services.dart';

class AppConfig {
  const AppConfig({
    required this.urlTerminos,
    required this.urlPrivacidad,
    required this.urlAsistencia,
  });

  final String urlTerminos;
  final String urlPrivacidad;
  final String urlAsistencia;

  factory AppConfig.defaults() {
    return const AppConfig(
      urlTerminos: 'https://www.gob.ec/',
      urlPrivacidad: 'https://www.gob.ec/',
      urlAsistencia: 'https://www.gob.ec/',
    );
  }

  factory AppConfig.fromJson(Map<String, dynamic> json) {
    return AppConfig(
      urlTerminos: json['url_terminos'] as String? ?? 'https://www.gob.ec/',
      urlPrivacidad: json['url_privacidad'] as String? ?? 'https://www.gob.ec/',
      urlAsistencia: json['url_asistencia'] as String? ?? 'https://www.gob.ec/',
    );
  }

  static Future<AppConfig> load() async {
    try {
      final raw = await rootBundle.loadString('assets/config/app_config.json');
      final decoded = jsonDecode(raw) as Map<String, dynamic>;
      return AppConfig.fromJson(decoded);
    } catch (_) {
      return AppConfig.defaults();
    }
  }
}
