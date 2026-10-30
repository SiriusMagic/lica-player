import 'package:flutter/material.dart';

import '../../core/state/app_controller.dart';
import '../../core/theme/sren_theme.dart';

class PermissionIntroScreen extends StatefulWidget {
  const PermissionIntroScreen({super.key, required this.controller});

  final AppController controller;

  @override
  State<PermissionIntroScreen> createState() => _PermissionIntroScreenState();
}

class _PermissionIntroScreenState extends State<PermissionIntroScreen> {
  bool _started = false;

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    if (_started) {
      return;
    }
    _started = true;
    WidgetsBinding.instance.addPostFrameCallback((_) {
      widget.controller.requestInitialPermissions();
    });
  }

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: widget.controller,
      builder: (context, _) {
        final strings = widget.controller.strings;

        return Scaffold(
          body: SafeArea(
            child: Padding(
              padding: const EdgeInsets.symmetric(horizontal: 32, vertical: 24),
              child: Column(
                children: [
                  const Spacer(flex: 3),
                  ClipOval(
                    child: Image.asset(
                      'assets/images/sren_logo.jpg',
                      width: 110,
                      height: 110,
                      fit: BoxFit.cover,
                    ),
                  ),
                  const SizedBox(height: 28),
                  Text(
                    strings.playYourMusic,
                    textAlign: TextAlign.center,
                    style: const TextStyle(
                      color: Colors.white,
                      fontSize: 34,
                      fontWeight: FontWeight.w800,
                    ),
                  ),
                  const SizedBox(height: 16),
                  Text(
                    strings.musicAccessMessage,
                    textAlign: TextAlign.center,
                    style: const TextStyle(
                      color: SrenTheme.muted,
                      fontSize: 22,
                      height: 1.4,
                    ),
                  ),
                  const Spacer(flex: 4),
                  if (widget.controller.isRequestingPermissions) ...[
                    const SizedBox(
                      width: 34,
                      height: 34,
                      child: CircularProgressIndicator(
                        color: SrenTheme.accent,
                        strokeWidth: 3.2,
                      ),
                    ),
                    const SizedBox(height: 16),
                    Text(
                      strings.requestingPermissions,
                      style: const TextStyle(color: SrenTheme.muted, fontSize: 16),
                    ),
                  ],
                  const Spacer(),
                ],
              ),
            ),
          ),
        );
      },
    );
  }
}
