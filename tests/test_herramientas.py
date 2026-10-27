"""Pruebas de las herramientas de música."""
import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "herramientas" / "sarap"))

from sarap import HOME, MUSIC, MUSIC_SEARCH, PLAYING, SEARCH, Sarap  # noqa: E402

SECRET = re.compile(r"AIza[0-9A-Za-z_\-]{35}|hf_[A-Za-z0-9]{30,}|sk-[A-Za-z0-9]{20,}|github_pat_\w+")


def say(app, text):
    action, value = app.decide(text)
    assert action, f"no entendió: {text!r}"
    assert app.step(action, value)


def test_navegar_buscar_y_reproducir():
    app = Sarap()
    say(app, "quiero música")
    assert app.state == MUSIC
    say(app, "busca canción")
    assert app.state == MUSIC_SEARCH and app.results == ["Tú eres mi canción"]
    say(app, "pon tú eres mi canción")
    assert app.state == PLAYING and app.playing == "Tú eres mi canción"
    say(app, "atrás")
    say(app, "atrás")
    assert app.state == HOME


def test_buscador_general_empieza_vacio():
    app = Sarap()
    say(app, "música")
    say(app, "busca dnd")
    say(app, "atrás")
    say(app, "atrás")
    say(app, "buscar")
    assert app.state == SEARCH and app.query is None
    say(app, "escribe hola")
    assert app.query == "hola"
    say(app, "borra")
    assert app.query is None


def test_frase_desconocida_no_hace_nada():
    app = Sarap()
    assert app.decide("hola qué tal") == (None, None)
    assert app.state == HOME


def test_cuaderno_limpio():
    raw = (ROOT / "herramientas" / "biblioteca" / "biblioteca_musical.ipynb").read_text(encoding="utf-8")
    assert not SECRET.search(raw)
    nb = json.loads(raw)
    for cell in nb["cells"]:
        if cell["cell_type"] == "code":
            assert not cell.get("outputs")
    # La plantilla de nombres no debe tener espacios tras "%" (bug ya corregido).
    assert "% (" not in raw
