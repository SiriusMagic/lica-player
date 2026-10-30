import 'package:flutter/material.dart';

import '../../core/state/app_controller.dart';
import '../../core/theme/sren_theme.dart';
import '../player/player_screen.dart';
import '../settings/settings_screen.dart';

class HomeScreen extends StatelessWidget {
  const HomeScreen({super.key, required this.controller});

  final AppController controller;

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: controller,
      builder: (context, _) {
        final strings = controller.strings;
        final status = switch (controller.mediaPermission) {
          MediaPermissionState.full => (
              title: strings.fullAccessTitle,
              body: strings.fullAccessBody,
            ),
          MediaPermissionState.limited => (
              title: strings.limitedAccessTitle,
              body: strings.limitedAccessBody,
            ),
          MediaPermissionState.denied => (
              title: strings.deniedAccessTitle,
              body: strings.deniedAccessBody,
            ),
          MediaPermissionState.unknown => (
              title: strings.unknownAccessTitle,
              body: strings.unknownAccessBody,
            ),
        };

        return Scaffold(
          body: SafeArea(
            child: Padding(
              padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 18),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      ClipRRect(
                        borderRadius: BorderRadius.circular(18),
                        child: Image.asset(
                          'assets/images/sren_logo.jpg',
                          width: 56,
                          height: 56,
                          fit: BoxFit.cover,
                        ),
                      ),
                      const SizedBox(width: 14),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              strings.homeTitle,
                              style: const TextStyle(
                                fontSize: 24,
                                fontWeight: FontWeight.w700,
                                color: Colors.white,
                              ),
                            ),
                            const SizedBox(height: 4),
                            Text(strings.homeSubtitle),
                          ],
                        ),
                      ),
                      IconButton(
                        onPressed: () {
                          Navigator.of(context).push(
                            MaterialPageRoute<void>(
                              builder: (_) => SettingsScreen(controller: controller),
                            ),
                          );
                        },
                        icon: const Icon(Icons.settings_outlined, color: Colors.white),
                      ),
                    ],
                  ),
                  const SizedBox(height: 24),
                  Container(
                    width: double.infinity,
                    padding: const EdgeInsets.all(20),
                    decoration: BoxDecoration(
                      color: SrenTheme.card,
                      borderRadius: BorderRadius.circular(28),
                      border: Border.all(color: SrenTheme.border),
                    ),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          status.title,
                          style: const TextStyle(
                            color: Colors.white,
                            fontSize: 20,
                            fontWeight: FontWeight.w700,
                          ),
                        ),
                        const SizedBox(height: 10),
                        Text(
                          status.body,
                          style: const TextStyle(
                            color: SrenTheme.muted,
                            fontSize: 16,
                            height: 1.45,
                          ),
                        ),
                        const SizedBox(height: 18),
                        Wrap(
                          spacing: 10,
                          runSpacing: 10,
                          children: [
                            if (controller.mediaPermission == MediaPermissionState.denied)
                              FilledButton(
                                onPressed: controller.requestMediaPermissionAgain,
                                style: FilledButton.styleFrom(
                                  backgroundColor: SrenTheme.accent,
                                  foregroundColor: Colors.black,
                                ),
                                child: Text(strings.grantAccess),
                              ),
                            if (controller.mediaPermission == MediaPermissionState.limited)
                              FilledButton(
                                onPressed: controller.requestMediaPermissionAgain,
                                style: FilledButton.styleFrom(
                                  backgroundColor: SrenTheme.accent,
                                  foregroundColor: Colors.black,
                                ),
                                child: Text(strings.manageAccess),
                              ),
                            OutlinedButton(
                              onPressed: controller.openSystemSettings,
                              child: Text(strings.openSettings),
                            ),
                          ],
                        ),
                      ],
                    ),
                  ),
                  const SizedBox(height: 20),
                  Container(
                    width: double.infinity,
                    padding: const EdgeInsets.all(18),
                    decoration: BoxDecoration(
                      color: const Color(0xFF10131E),
                      borderRadius: BorderRadius.circular(24),
                    ),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          strings.notifications,
                          style: const TextStyle(
                            color: Colors.white,
                            fontWeight: FontWeight.w700,
                          ),
                        ),
                        const SizedBox(height: 8),
                        Text(
                          controller.notificationPermission ==
                                  NotificationPermissionState.granted
                              ? strings.notificationsGranted
                              : strings.notificationsDenied,
                        ),
                      ],
                    ),
                  ),
                  const Spacer(),
                  SizedBox(
                    width: double.infinity,
                    child: FilledButton(
                      onPressed: () {
                        Navigator.of(context).push(
                          MaterialPageRoute<void>(
                            builder: (_) => PlayerScreen(controller: controller),
                          ),
                        );
                      },
                      style: FilledButton.styleFrom(
                        backgroundColor: SrenTheme.accent,
                        foregroundColor: Colors.black,
                        padding: const EdgeInsets.symmetric(vertical: 18),
                      ),
                      child: Text(strings.player),
                    ),
                  ),
                  const SizedBox(height: 14),
                  Center(
                    child: Text(
                      strings.versionNote,
                      textAlign: TextAlign.center,
                      style: const TextStyle(color: SrenTheme.muted, fontSize: 13),
                    ),
                  ),
                ],
              ),
            ),
          ),
        );
      },
    );
  }
}
