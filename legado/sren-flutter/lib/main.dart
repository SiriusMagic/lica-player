import 'package:flutter/widgets.dart';

import 'src/app/sren_app.dart';
import 'src/core/state/app_controller.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  final controller = await AppController.bootstrap();
  runApp(SrenApp(controller: controller));
}
