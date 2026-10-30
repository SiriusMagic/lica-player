#!/usr/bin/env python3
"""Comprueba que cada apartado de Stone 1.4 marcado como hecho esté de verdad en el código.

Uso (desde la raíz del repositorio):  python3 herramientas/verificar_stone.py

Qué revisa:
1. docs/STONE-1.4.md: cada fila de tabla con ✅ en su última columna debe citar al menos
   una prueba entre comillas invertidas con la forma `stone/ruta/Archivo.kt:texto`
   (o solo `stone/ruta/archivo`). La ruta debe existir y, si hay `:texto`, el archivo
   debe contenerlo (por ejemplo el nombre de la función). Así nadie puede marcar ✅ sin
   decir dónde está, y si el código se mueve o se borra, la comprobación falla.
2. Idiomas: los 6 de Stone (en, es, pt, fr, it, de) tienen todos los textos de la app,
   con los mismos marcadores (%1$s, %d…).
3. Muestra el avance por sección (✅ hecho · 🟡 a medias · ⬜ pendiente).

Termina con código 1 si algo falla (lo usa la CI en cada PR).
"""
import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

RAIZ = Path(__file__).resolve().parent.parent
ESPEC = RAIZ / "docs" / "STONE-1.4.md"
RES = RAIZ / "stone" / "app" / "src" / "main" / "res"
IDIOMAS = ["es", "pt", "fr", "it", "de"]  # además del inglés (values/)
PRUEBA = re.compile(r"`((?:stone|docs)/[^`:]+)(?::([^`]+))?`")
MARCADOR = re.compile(r"%(?:\d+\$)?[sd]")

errores: list[str] = []


def celdas(linea: str) -> list[str]:
    return [c.strip() for c in linea.strip().strip("|").split("|")]


def revisar_especificacion() -> None:
    seccion = "(inicio)"
    avance: dict[str, list[int]] = {}
    for n, linea in enumerate(ESPEC.read_text(encoding="utf-8").splitlines(), 1):
        if linea.startswith("#"):
            seccion = linea.lstrip("# ").strip()
            continue
        if not linea.startswith("|") or set(linea) <= set("|-: "):
            continue
        estado = celdas(linea)[-1]
        cuenta = avance.setdefault(seccion, [0, 0, 0])
        if "✅" in estado:
            cuenta[0] += 1
        elif "🟡" in estado:
            cuenta[1] += 1
        elif "⬜" in estado:
            cuenta[2] += 1
        else:
            continue  # encabezado u otra tabla sin estado
        if "✅" not in estado:
            continue
        pruebas = PRUEBA.findall(linea)
        donde = f"STONE-1.4.md:{n} ({celdas(linea)[0][:50]})"
        if not pruebas:
            errores.append(f"{donde}: marcada ✅ pero no cita dónde está (`stone/ruta:función`).")
        for ruta, texto in pruebas:
            archivo = RAIZ / ruta
            if not archivo.exists():
                errores.append(f"{donde}: no existe {ruta}")
            elif texto and texto not in archivo.read_text(encoding="utf-8", errors="replace"):
                errores.append(f"{donde}: {ruta} no contiene «{texto}»")

    print("Avance de Stone 1.4 (✅ hecho · 🟡 a medias · ⬜ pendiente)")
    total = [0, 0, 0]
    for seccion, (h, m, p) in avance.items():
        if h + m + p:
            print(f"  {seccion[:52]:<52} ✅ {h:>2}  🟡 {m:>2}  ⬜ {p:>2}")
            total = [total[0] + h, total[1] + m, total[2] + p]
    suma = sum(total) or 1
    print(f"  {'TOTAL':<52} ✅ {total[0]:>2}  🟡 {total[1]:>2}  ⬜ {total[2]:>2}"
          f"   ({100 * total[0] // suma} % hecho)")


def textos(archivo: Path) -> dict[str, str]:
    """nombre → texto (los plurales se juntan) de un strings.xml."""
    salida = {}
    for nodo in ET.parse(archivo).getroot():
        if nodo.tag == "string":
            salida[nodo.get("name")] = nodo.text or ""
        elif nodo.tag == "plurals":
            salida[nodo.get("name")] = " ".join(i.text or "" for i in nodo)
    return salida


def revisar_idiomas() -> None:
    base = textos(RES / "values" / "strings.xml")
    base.pop("app_name", None)  # el nombre "Stone" no se traduce
    for idioma in IDIOMAS:
        archivo = RES / f"values-{idioma}" / "strings.xml"
        if not archivo.exists():
            errores.append(f"Idioma {idioma}: falta {archivo.relative_to(RAIZ)}")
            continue
        try:
            trad = textos(archivo)
        except ET.ParseError as e:
            errores.append(f"Idioma {idioma}: XML inválido ({e})")
            continue
        for nombre, texto in base.items():
            if nombre not in trad:
                errores.append(f"Idioma {idioma}: falta el texto «{nombre}»")
            elif sorted(set(MARCADOR.findall(texto))) != sorted(set(MARCADOR.findall(trad[nombre]))):
                errores.append(f"Idioma {idioma}: «{nombre}» no tiene los mismos marcadores (%s, %d) que el inglés")
        for nombre in trad.keys() - base.keys() - {"app_name"}:
            errores.append(f"Idioma {idioma}: «{nombre}» sobra (no existe en inglés)")
    print(f"Idiomas: {len(base)} textos revisados en en + {', '.join(IDIOMAS)}")


def main() -> int:
    revisar_especificacion()
    revisar_idiomas()
    if errores:
        print(f"\n✗ {len(errores)} problema(s):")
        for e in errores:
            print("  -", e)
        return 1
    print("\n✓ Todo lo marcado como hecho está en el código y los 6 idiomas están completos.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
