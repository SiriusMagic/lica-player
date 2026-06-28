export type Track = {
  id: string;
  title: string;
  artist: string;
  album: string;
  folder: string;
  cover: string;
  url: string;
  durationMs: number;
};

// Royalty-free demo tracks from SoundHelix + Pixabay/Pexels covers.
export const DEMO_TRACKS: Track[] = [
  {
    id: "t1",
    title: "Royalty",
    artist: "Egzod, Maestro Chives, Neoni",
    album: "Royalty",
    folder: "Descargas",
    cover: "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=400&q=80&auto=format&fit=crop",
    url: "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
    durationMs: 372000,
  },
  {
    id: "t2",
    title: "Neon Skyline",
    artist: "Aurora Pulse",
    album: "Midnight Drive",
    folder: "Descargas",
    cover: "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=400&q=80&auto=format&fit=crop",
    url: "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
    durationMs: 422000,
  },
  {
    id: "t3",
    title: "Sunset Drive",
    artist: "Aurora Pulse",
    album: "Midnight Drive",
    folder: "Música",
    cover: "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=400&q=80&auto=format&fit=crop",
    url: "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
    durationMs: 348000,
  },
  {
    id: "t4",
    title: "Ocean Mirror",
    artist: "Lumen Wave",
    album: "Tidal",
    folder: "Música",
    cover: "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=400&q=80&auto=format&fit=crop",
    url: "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
    durationMs: 401000,
  },
  {
    id: "t5",
    title: "Crystal Cave",
    artist: "Lumen Wave",
    album: "Tidal",
    folder: "Música",
    cover: "https://images.unsplash.com/photo-1496293455970-f8581aae0e3b?w=400&q=80&auto=format&fit=crop",
    url: "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3",
    durationMs: 287000,
  },
  {
    id: "t6",
    title: "Echoes of You",
    artist: "Verse North",
    album: "Polar Heart",
    folder: "Descargas",
    cover: "https://images.unsplash.com/photo-1459749411175-04bf5292ceea?w=400&q=80&auto=format&fit=crop",
    url: "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3",
    durationMs: 318000,
  },
  {
    id: "t7",
    title: "Paper Planes",
    artist: "Verse North",
    album: "Polar Heart",
    folder: "Descargas",
    cover: "https://images.unsplash.com/photo-1483412033650-1015ddeb83d1?w=400&q=80&auto=format&fit=crop",
    url: "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-7.mp3",
    durationMs: 295000,
  },
  {
    id: "t8",
    title: "Lantern",
    artist: "Kite & Kin",
    album: "Soft Light",
    folder: "Música",
    cover: "https://images.unsplash.com/photo-1487180144351-b8472da7d491?w=400&q=80&auto=format&fit=crop",
    url: "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3",
    durationMs: 365000,
  },
  {
    id: "t9",
    title: "Morning Static",
    artist: "Kite & Kin",
    album: "Soft Light",
    folder: "Música",
    cover: "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=400&q=80&auto=format&fit=crop",
    url: "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-9.mp3",
    durationMs: 410000,
  },
  {
    id: "t10",
    title: "Slow Lights",
    artist: "Egzod, Maestro Chives, Neoni",
    album: "Royalty",
    folder: "Descargas",
    cover: "https://images.unsplash.com/photo-1485579149621-3123dd979885?w=400&q=80&auto=format&fit=crop",
    url: "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-10.mp3",
    durationMs: 332000,
  },
];

export function formatMs(ms: number) {
  const total = Math.max(0, Math.floor(ms / 1000));
  const m = Math.floor(total / 60);
  const s = total % 60;
  return `${m}:${s.toString().padStart(2, "0")}`;
}
