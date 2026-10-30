import 'package:flutter/material.dart';

import '../../core/localization/sren_strings.dart';
import '../../core/state/app_controller.dart';
import '../../core/theme/sren_theme.dart';

class SettingsScreen extends StatelessWidget {
  const SettingsScreen({super.key, required this.controller});

  final AppController controller;

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: controller,
      builder: (context, _) {
        final strings = controller.strings;

        return Scaffold(
          appBar: AppBar(
            title: Text(strings.settings),
            backgroundColor: Colors.transparent,
          ),
          body: ListView(
            padding: const EdgeInsets.all(20),
            children: [
              _SectionCard(
                title: strings.languageLabel,
                child: Column(
                  children: [
                    _LanguageActionTile(
                      label: 'Español',
                      selected: controller.selectedLanguage == AppLanguage.spanish,
                      onTap: () => controller.setLanguage(AppLanguage.spanish),
                    ),
                    const SizedBox(height: 10),
                    _LanguageActionTile(
                      label: 'English',
                      selected: controller.selectedLanguage == AppLanguage.english,
                      onTap: () => controller.setLanguage(AppLanguage.english),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 16),
              _SectionCard(
                title: strings.legalLinks,
                child: Column(
                  children: [
                    ListTile(
                      contentPadding: EdgeInsets.zero,
                      title: Text(controller.strings.terms),
                      trailing: const Icon(Icons.open_in_new, color: Colors.white70),
                      onTap: () => _openLink(controller.config.urlTerminos),
                    ),
                    ListTile(
                      contentPadding: EdgeInsets.zero,
                      title: Text(controller.strings.privacy),
                      trailing: const Icon(Icons.open_in_new, color: Colors.white70),
                      onTap: () => _openLink(controller.config.urlPrivacidad),
                    ),
                    ListTile(
                      contentPadding: EdgeInsets.zero,
                      title: Text(strings.support),
                      trailing: const Icon(Icons.open_in_new, color: Colors.white70),
                      onTap: () => _openLink(controller.config.urlAsistencia),
                    ),
                  ],
                ),
              ),
            ],
          ),
        );
      },
    );
  }

  Future<void> _openLink(String rawUrl) async {
    await controller.openExternalUrl(rawUrl);
  }
}

class _SectionCard extends StatelessWidget {
  const _SectionCard({required this.title, required this.child});

  final String title;
  final Widget child;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(18),
      decoration: BoxDecoration(
        color: SrenTheme.card,
        borderRadius: BorderRadius.circular(24),
        border: Border.all(color: SrenTheme.border),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            title,
            style: const TextStyle(
              color: Colors.white,
              fontSize: 18,
              fontWeight: FontWeight.w700,
            ),
          ),
          const SizedBox(height: 12),
          child,
        ],
      ),
    );
  }
}

class _LanguageActionTile extends StatelessWidget {
  const _LanguageActionTile({
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
      borderRadius: BorderRadius.circular(16),
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 14),
        decoration: BoxDecoration(
          color: selected ? const Color(0xFF1B1E2C) : Colors.transparent,
          borderRadius: BorderRadius.circular(16),
          border: Border.all(
            color: selected ? SrenTheme.accent : SrenTheme.border,
          ),
        ),
        child: Row(
          children: [
            Expanded(
              child: Text(
                label,
                style: const TextStyle(color: Colors.white, fontSize: 16),
              ),
            ),
            if (selected)
              const Icon(Icons.check_rounded, color: SrenTheme.accent),
          ],
        ),
      ),
    );
  }
}
