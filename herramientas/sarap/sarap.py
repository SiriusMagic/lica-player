"""SARAP — prototipo de asistente por voz/texto para una app de música.

Simula cómo un asistente convierte frases del usuario ("pon Aver", "busca canción",
"atrás") en acciones sobre las pantallas de la app. El "router" es un conjunto de
reglas simples que hace de LLM simulado.

Pantallas: inicio → música (biblioteca) → búsqueda de música → reproduciendo,
y un buscador general.

Uso:
    python sarap.py          # modo interactivo; escribe "salir" para terminar

Origen: cuaderno "Copia de Untitled4" (Notbook).
"""

MUSIC_LIBRARY = ["Aver", "Dnd", "Tú eres mi canción", "No te lloro tanto"]

HOME, MUSIC, MUSIC_SEARCH, PLAYING, SEARCH = "home", "music_main", "music_search", "music_playing", "search"


class Sarap:
    def __init__(self, library=MUSIC_LIBRARY):
        self.library = list(library)
        self.state = HOME
        self.query = None
        self.results = []
        self.playing = None

    # ---------- Búsqueda ----------
    def _search(self, text):
        self.query = text
        self.results = [m for m in self.library if text.lower() in m.lower()]

    # ---------- Router (LLM simulado) ----------
    def decide(self, user):
        """Convierte la frase del usuario en (acción, valor), o (None, None)."""
        u = user.lower()
        if "atrás" in u and self.state != HOME:
            return ("back", None)

        if self.state == HOME:
            if "music" in u or "música" in u:
                return ("music", None)
            if "busc" in u:
                return ("search", None)

        elif self.state == MUSIC:
            if "busca" in u:
                return ("search_music", u.split("busca")[-1].strip())
            for m in self.library:
                if m.lower() in u:
                    return ("play", m)

        elif self.state == MUSIC_SEARCH:
            for m in self.results:
                if m.lower() in u:
                    return ("play", m)
            if "modifica" in u:
                return ("modify", u.split()[-1])

        elif self.state == SEARCH:
            if "escribe" in u or "busca" in u:
                return ("write", u.split()[-1])
            if "modifica" in u:
                return ("modify", u.split()[-1])
            if "borra" in u:
                return ("delete", None)

        return (None, None)

    # ---------- Acciones ----------
    def step(self, action, value=None):
        """Aplica una acción. Devuelve False si no es válida en la pantalla actual."""
        s = self.state
        if s == HOME:
            if action == "music":
                self.state = MUSIC
            elif action == "search":
                self.query, self.state = None, SEARCH
            else:
                return False

        elif s == MUSIC:
            if action == "play":
                self.playing, self.state = value, PLAYING
            elif action == "search_music":
                self._search(value)
                self.state = MUSIC_SEARCH
            elif action == "back":
                self.state = HOME
            else:
                return False

        elif s == MUSIC_SEARCH:
            if action == "play":
                self.playing, self.state = value, PLAYING
            elif action == "modify":
                self._search(value)
            elif action == "back":
                self.state = MUSIC
            else:
                return False

        elif s == PLAYING:
            if action == "back":
                self.state = MUSIC
            else:
                return False

        elif s == SEARCH:
            if action in ("write", "modify"):
                self.query = value
            elif action == "delete":
                self.query = None
            elif action == "back":
                self.state = HOME
            else:
                return False

        return True

    # ---------- Pantalla ----------
    def render(self):
        lines = [f"--- PANTALLA: {self.state} ---"]
        if self.state == HOME:
            lines += ["Music", "Buscador"]
        elif self.state == MUSIC:
            lines += ["Biblioteca:"] + [f"- {m}" for m in self.library]
        elif self.state == PLAYING:
            lines += ["Reproduciendo:", str(self.playing)]
        elif self.state == MUSIC_SEARCH:
            lines += ["Buscador música:", f"Consulta: {self.query}", "Resultados:"] + [f"- {r}" for r in self.results]
        elif self.state == SEARCH:
            lines += ["Buscador general:", f"Consulta: {self.query}"]
        return "\n".join(lines)


def main():
    app = Sarap()
    print(app.render())
    while True:
        try:
            user = input("USUARIO> ")
        except (KeyboardInterrupt, EOFError):
            print("\nINTERRUMPIDO")
            break
        if user.lower() in ("exit", "salir"):
            break
        action, value = app.decide(user)
        if not action:
            print("ACCIÓN: ninguna => FALLO")
            continue
        ok = app.step(action, value)
        print("ACCIÓN:", {"action": action, "value": value}, "=>", "OK" if ok else "FALLO")
        print(app.render())


if __name__ == "__main__":
    main()
