import 'package:flutter_test/flutter_test.dart';

import 'package:sren/src/app/sren_app.dart';
import 'package:sren/src/core/state/app_controller.dart';

void main() {
  testWidgets('shows language selection on first launch', (tester) async {
    final controller = AppController.preview();

    await tester.pumpWidget(SrenApp(controller: controller));
    await tester.pumpAndSettle();

    expect(find.text('Elige tu idioma'), findsOneWidget);
    expect(find.text('Español'), findsOneWidget);
    expect(find.text('English'), findsOneWidget);
  });
}
