import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';

import '../../core/localization/sren_strings.dart';
import '../../core/state/app_controller.dart';
import '../../core/theme/sren_theme.dart';

class LanguageSelectionScreen extends StatelessWidget {
  const LanguageSelectionScreen({super.key, required this.controller});

  final AppController controller;

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: controller,
      builder: (context, _) {
        final strings = controller.strings;

        return Scaffold(
          body: SafeArea(
            child: Padding(
              padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 20),
              child: Column(
                children: [
                  const Spacer(flex: 2),
                  ClipOval(
                    child: Image.asset(
                      'assets/images/sren_logo.jpg',
                      width: 118,
                      height: 118,
                      fit: BoxFit.cover,
                    ),
                  ),
                  const SizedBox(height: 24),
                  Text(
                    strings.chooseLanguage,
                    style: const TextStyle(
                      color: Colors.white,
                      fontSize: 30,
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                  const SizedBox(height: 28),
                  Container(
                    decoration: BoxDecoration(
                      color: SrenTheme.card,
                      borderRadius: BorderRadius.circular(26),
                      border: Border.all(color: SrenTheme.border),
                    ),
                    child: Column(
                      children: [
                        _LanguageTile(
                          label: AppLanguage.spanish.label,
                          selected: controller.selectedLanguage == AppLanguage.spanish,
                          onTap: () => controller.setLanguage(AppLanguage.spanish),
                        ),
                        const Divider(height: 1, color: SrenTheme.border),
                        _LanguageTile(
                          label: AppLanguage.english.label,
                          selected: controller.selectedLanguage == AppLanguage.english,
                          onTap: () => controller.setLanguage(AppLanguage.english),
                        ),
                      ],
                    ),
                  ),
                  const Spacer(),
                  SizedBox(
                    width: double.infinity,
                    child: FilledButton(
                      onPressed: controller.completeOnboarding,
                      style: FilledButton.styleFrom(
                        backgroundColor: SrenTheme.accent,
                        foregroundColor: Colors.black,
                        padding: const EdgeInsets.symmetric(vertical: 22),
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(999),
                        ),
                      ),
                      child: Text(
                        strings.start,
                        style: const TextStyle(
                          fontSize: 18,
                          fontWeight: FontWeight.w700,
                        ),
                      ),
                    ),
                  ),
                  const SizedBox(height: 22),
                  Text.rich(
                    TextSpan(
                      style: const TextStyle(
                        color: SrenTheme.muted,
                        fontSize: 14,
                        height: 1.55,
                      ),
                      children: [
                        TextSpan(text: strings.termsPrefix),
                        TextSpan(
                          text: strings.terms,
                          style: const TextStyle(
                            color: SrenTheme.accent,
                            decoration: TextDecoration.underline,
                          ),
                          recognizer: TapGestureRecognizer()
                            ..onTap = () => _openLink(controller.config.urlTerminos),
                        ),
                        TextSpan(text: strings.andText),
                        TextSpan(
                          text: strings.privacy,
                          style: const TextStyle(
                            color: SrenTheme.accent,
                            decoration: TextDecoration.underline,
                          ),
                          recognizer: TapGestureRecognizer()
                            ..onTap = () => _openLink(controller.config.urlPrivacidad),
                        ),
                      ],
                    ),
                    textAlign: TextAlign.center,
                  ),
                  const SizedBox(height: 16),
                ],
              ),
            ),
          ),
        );
      },
    );
  }

  Future<void> _openLink(String rawUrl) async {
    await controller.openExternalUrl(rawUrl);
  }
}

class _LanguageTile extends StatelessWidget {
  const _LanguageTile({
    required this.label,
    required this.selected,
    required this.onTap,
  });

  final String label;
  final bool selected;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(26),
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 22),
        child: Row(
          children: [
            Icon(
              Icons.language,
              color: selected ? SrenTheme.accent : Colors.white70,
              size: 26,
            ),
            const SizedBox(width: 14),
            Expanded(
              child: Text(
                label,
                style: const TextStyle(
                  color: Colors.white,
                  fontSize: 18,
                ),
              ),
            ),
            Icon(
              selected ? Icons.check_rounded : Icons.circle_outlined,
              color: selected ? SrenTheme.accent : Colors.transparent,
              size: 28,
            ),
          ],
        ),
      ),
    );
  }
}
