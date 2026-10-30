import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';

import '../core/localization/sren_strings.dart';
import '../core/state/app_controller.dart';
import '../core/theme/sren_theme.dart';
import '../features/home/home_screen.dart';
import '../features/onboarding/language_selection_screen.dart';
import '../features/permissions/permission_intro_screen.dart';

class SrenApp extends StatelessWidget {
  const SrenApp({super.key, required this.controller});

  final AppController controller;

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: controller,
      builder: (context, _) {
        return MaterialApp(
          title: 'Sren',
          debugShowCheckedModeBanner: false,
          theme: SrenTheme.themeData,
          locale: controller.locale,
          supportedLocales: AppLanguage.values.map((language) => language.locale),
          localizationsDelegates: const [
            GlobalMaterialLocalizations.delegate,
            GlobalWidgetsLocalizations.delegate,
            GlobalCupertinoLocalizations.delegate,
          ],
          home: switch (controller.currentStep) {
            AppStep.language => LanguageSelectionScreen(controller: controller),
            AppStep.permissions => PermissionIntroScreen(controller: controller),
            AppStep.home => HomeScreen(controller: controller),
          },
        );
      },
    );
  }
}
