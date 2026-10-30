import 'package:flutter/services.dart';

class PlatformBridge {
  static const MethodChannel _channel =
      MethodChannel('app.sren.mobile/platform');

  Future<Map<String, dynamic>> getState() async {
    final state = await _channel.invokeMapMethod<String, dynamic>('getState');
    return Map<String, dynamic>.from(state ?? const <String, dynamic>{});
  }

  Future<void> saveState(Map<String, dynamic> state) {
    return _channel.invokeMethod<void>('saveState', state);
  }

  Future<String> requestNotificationPermission() async {
    return (await _channel.invokeMethod<String>('requestNotificationPermission')) ??
        'denied';
  }

  Future<String> requestMediaPermission() async {
    return (await _channel.invokeMethod<String>('requestMediaPermission')) ??
        'denied';
  }

  Future<void> openSystemSettings() {
    return _channel.invokeMethod<void>('openSystemSettings');
  }

  Future<void> openExternalUrl(String url) {
    return _channel.invokeMethod<void>('openExternalUrl', <String, dynamic>{
      'url': url,
    });
  }
}
