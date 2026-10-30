import 'package:flutter/material.dart';

import '../../core/state/app_controller.dart';
import '../../core/theme/sren_theme.dart';

class PlayerScreen extends StatelessWidget {
  const PlayerScreen({super.key, required this.controller});

  final AppController controller;

  @override
  Widget build(BuildContext context) {
    final strings = controller.strings;

    return Scaffold(
      appBar: AppBar(
        title: Text(strings.player),
        backgroundColor: Colors.transparent,
      ),
      body: Center(
        child: Padding(
          padding: const EdgeInsets.all(24),
          child: Container(
            width: double.infinity,
            padding: const EdgeInsets.all(24),
            decoration: BoxDecoration(
              color: SrenTheme.card,
              borderRadius: BorderRadius.circular(28),
            ),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                ClipRRect(
                  borderRadius: BorderRadius.circular(24),
                  child: Image.asset(
                    'assets/images/sren_logo.jpg',
                    width: 180,
                    height: 180,
                    fit: BoxFit.cover,
                  ),
                ),
                const SizedBox(height: 18),
                Text(
                  strings.placeholderPlayerTitle,
                  textAlign: TextAlign.center,
                  style: const TextStyle(
                    color: Colors.white,
                    fontSize: 24,
                    fontWeight: FontWeight.w700,
                  ),
                ),
                const SizedBox(height: 10),
                Text(
                  strings.placeholderPlayerBody,
                  textAlign: TextAlign.center,
                  style: const TextStyle(
                    color: SrenTheme.muted,
                    fontSize: 16,
                    height: 1.45,
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
