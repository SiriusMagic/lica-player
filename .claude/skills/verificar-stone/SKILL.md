---
name: verificar-stone
description: Comprueba que cada apartado, gesto y señal de Stone 1.3.2 esté en Stone 1.4 (docs/STONE-1.4.md) y que los 6 idiomas estén completos. Usar antes de abrir o fusionar un PR de Stone 1.4, o cuando el dueño pregunte qué falta.
---

# Verificar Stone 1.4

El contrato es `docs/STONE-1.4.md`: cada fila es algo que Stone 1.3.2 hace (código en `legado/stone/`) y que
Stone 1.4 (`stone/`) debe hacer igual o mejor. Estado: ⬜ pendiente · 🟡 a medias · ✅ hecho.

## 1. Comprobación automática (siempre)

```
python3 -I herramientas/verificar_stone.py
```

- Toda fila ✅ debe citar su código como `` `stone/ruta/Archivo.kt:texto` `` (o `` `docs/…` ``); el script
  comprueba que el archivo exista y contenga ese texto.
- Revisa que en, es, pt, fr, it, de tengan los mismos textos y marcadores (%1$s, %d).
- Muestra el avance por sección. La CI lo corre en cada PR (`.github/workflows/android.yml`).

## 2. Revisión a mano (lo que el script no puede ver)

Para cada fila que el PR cambia a ✅:
1. Abre el archivo de Stone 1.3.2 que nombra la fila (columna "En Stone 1.3.2", dentro de
   `legado/stone/frontend/`) y anota qué hace exactamente: textos, orden, colores, medidas, gestos (umbrales en
   px, tiempos), qué pasa al tocar, mantener pulsado o deslizar.
2. Abre el código citado en Stone 1.4 y confirma cada punto. Si algo falta o difiere, la fila es 🟡 y se dice qué
   falta entre paréntesis.
3. Si la fila es de texto visible, confirma que use `stringResource` (nada escrito a mano en Kotlin) y que el
   texto exista en los 6 idiomas.
4. Si cambia la interfaz, compila el APK (`docs/COMPILAR.md`) y compárala con las fotos de `docs/diseno/`.

## 3. Resultado

Informa en español, sin jerga: cuántas filas ✅/🟡/⬜ hay (salida del script), qué filas cambiaron en este PR y
cualquier diferencia encontrada con Stone 1.3.2. Nunca marques ✅ algo que no comprobaste.
