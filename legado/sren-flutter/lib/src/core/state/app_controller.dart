// ignore_for_file: prefer_initializing_formals

import 'package:flutter/material.dart';

import '../config/app_config.dart';
import '../localization/sren_strings.dart';
import '../platform/platform_bridge.dart';

enum NotificationPermissionState { unknown, granted, denied }

enum MediaPermissionState { unknown, full, limited, denied }

enum AppStep { language, permissions, home }

class AppController extends ChangeNotifier {
  AppController._({
    required PlatformBridge? bridge,
    required AppConfig config,
    required AppLanguage selectedLanguage,
    required bool hasSeenOnboarding,
    required bool hasCompletedPermissionFlow,
    required NotificationPermissionState notificationPermission,
    required MediaPermissionState mediaPermission,
  })  : _bridge = bridge,
        _config = config,
        _selectedLanguage = selectedLanguage,
        _hasSeenOnboarding = hasSeenOnboarding,
        _hasCompletedPermissionFlow = hasCompletedPermissionFlow,
        _notificationPermission = notificationPermission,
        _mediaPermission = mediaPermission;

  final PlatformBridge? _bridge;

  AppConfig _config;
  AppLanguage _selectedLanguage;
  bool _hasSeenOnboarding;
  bool _hasCompletedPermissionFlow;
  NotificationPermissionState _notificationPermission;
  MediaPermissionState _mediaPermission;
  bool _isRequestingPermissions = false;

  static Future<AppController> bootstrap() async {
    final bridge = PlatformBridge();
    final state = await bridge.getState();
    final config = await AppConfig.load();

    return AppController._(
      bridge: bridge,
      config: config,
      selectedLanguage: _languageFromCode(state['selectedLanguage'] as String?),
      hasSeenOnboarding: state['hasSeenOnboarding'] as bool? ?? false,
      hasCompletedPermissionFlow:
          state['hasCompletedPermissionFlow'] as bool? ?? false,
      notificationPermission: _notificationFromName(
        state['notificationPermission'] as String?,
      ),
      mediaPermission: _mediaFromName(state['mediaPermission'] as String?),
    );
  }

  factory AppController.preview({
    AppLanguage selectedLanguage = AppLanguage.spanish,
    bool hasSeenOnboarding = false,
    bool hasCompletedPermissionFlow = false,
    NotificationPermissionState notificationPermission =
        NotificationPermissionState.unknown,
    MediaPermissionState mediaPermission = MediaPermissionState.unknown,
  }) {
    return AppController._(
      bridge: null,
      config: AppConfig.defaults(),
      selectedLanguage: selectedLanguage,
      hasSeenOnboarding: hasSeenOnboarding,
      hasCompletedPermissionFlow: hasCompletedPermissionFlow,
      notificationPermission: notificationPermission,
      mediaPermission: mediaPermission,
    );
  }

  AppConfig get config => _config;
  AppLanguage get selectedLanguage => _selectedLanguage;
  NotificationPermissionState get notificationPermission =>
      _notificationPermission;
  MediaPermissionState get mediaPermission => _mediaPermission;
  bool get isRequestingPermissions => _isRequestingPermissions;
  Locale get locale => _selectedLanguage.locale;
  SrenStrings get strings => SrenStrings(_selectedLanguage);

  AppStep get currentStep {
    if (!_hasSeenOnboarding) {
      return AppStep.language;
    }
    if (!_hasCompletedPermissionFlow) {
      return AppStep.permissions;
    }
    return AppStep.home;
  }

  Future<void> setLanguage(AppLanguage language) async {
    _selectedLanguage = language;
    await _persistState();
    notifyListeners();
  }

  Future<void> completeOnboarding() async {
    _hasSeenOnboarding = true;
    await _persistState();
    notifyListeners();
  }

  Future<void> requestInitialPermissions() async {
    if (_isRequestingPermissions) {
      return;
    }

    _isRequestingPermissions = true;
    notifyListeners();

    _notificationPermission = await _requestNotificationPermission();
    _mediaPermission = await _requestMediaPermission();
    _hasCompletedPermissionFlow = true;
    await _persistState();
    _isRequestingPermissions = false;
    notifyListeners();
  }

  Future<void> requestMediaPermissionAgain() async {
    _mediaPermission = await _requestMediaPermission();
    await _persistState();
    notifyListeners();
  }

  Future<void> openSystemSettings() async {
    await _bridge?.openSystemSettings();
  }

  Future<void> openExternalUrl(String url) async {
    await _bridge?.openExternalUrl(url);
  }

  Future<void> reloadConfig() async {
    _config = await AppConfig.load();
    notifyListeners();
  }

  Future<NotificationPermissionState> _requestNotificationPermission() async {
    final value = await _bridge?.requestNotificationPermission() ?? 'denied';
    return _notificationFromName(value);
  }

  Future<MediaPermissionState> _requestMediaPermission() async {
    final value = await _bridge?.requestMediaPermission() ?? 'denied';
    return _mediaFromName(value);
  }

  Future<void> _persistState() async {
    await _bridge?.saveState(<String, dynamic>{
      'hasSeenOnboarding': _hasSeenOnboarding,
      'hasCompletedPermissionFlow': _hasCompletedPermissionFlow,
      'selectedLanguage': _selectedLanguage.code,
      'notificationPermission': _notificationPermission.name,
      'mediaPermission': _mediaPermission.name,
    });
  }

  static AppLanguage _languageFromCode(String? code) {
    return AppLanguage.values.firstWhere(
      (language) => language.code == code,
      orElse: () => AppLanguage.spanish,
    );
  }

  static NotificationPermissionState _notificationFromName(String? raw) {
    return NotificationPermissionState.values.firstWhere(
      (state) => state.name == raw,
      orElse: () => NotificationPermissionState.unknown,
    );
  }

  static MediaPermissionState _mediaFromName(String? raw) {
    return MediaPermissionState.values.firstWhere(
      (state) => state.name == raw,
      orElse: () => MediaPermissionState.unknown,
    );
  }
}
