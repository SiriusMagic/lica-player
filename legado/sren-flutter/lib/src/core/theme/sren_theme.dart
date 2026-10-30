import 'package:flutter/material.dart';

class SrenTheme {
  static const Color background = Color(0xFF0B0B18);
  static const Color card = Color(0xFF161824);
  static const Color accent = Color(0xFFF9DF80);
  static const Color muted = Color(0xFFA5A8B7);
  static const Color border = Color(0xFF24283A);

  static ThemeData get themeData {
    return ThemeData(
      useMaterial3: true,
      scaffoldBackgroundColor: background,
      colorScheme: const ColorScheme.dark(
        primary: accent,
        secondary: accent,
        surface: card,
      ),
      textTheme: const TextTheme(
        displaySmall: TextStyle(
          fontSize: 36,
          fontWeight: FontWeight.w700,
          color: Colors.white,
        ),
        headlineMedium: TextStyle(
          fontSize: 24,
          fontWeight: FontWeight.w700,
          color: Colors.white,
        ),
        bodyLarge: TextStyle(
          fontSize: 18,
          color: Colors.white,
        ),
        bodyMedium: TextStyle(
          fontSize: 16,
          color: muted,
        ),
      ),
    );
  }
}
