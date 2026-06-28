import React, { createContext, useContext, useMemo, useRef, useState, ReactNode, useEffect, useCallback } from "react";
import { useAudioPlayer, useAudioPlayerStatus, setAudioModeAsync } from "expo-audio";
import { DEMO_TRACKS, Track } from "@/src/data/tracks";

type RepeatMode = "off" | "all" | "one";

type AudioCtx = {
  current: Track | null;
  queue: Track[];
  isPlaying: boolean;
  positionMs: number;
  durationMs: number;
  shuffle: boolean;
  repeat: RepeatMode;
  playTrack: (track: Track, queue?: Track[]) => Promise<void>;
  toggle: () => void;
  next: () => void;
  prev: () => void;
  seekMs: (ms: number) => void;
  toggleShuffle: () => void;
  cycleRepeat: () => void;
};

const Ctx = createContext<AudioCtx | null>(null);

export function AudioProvider({ children }: { children: ReactNode }) {
  const player = useAudioPlayer(null);
  const status = useAudioPlayerStatus(player);

  const [queue, setQueue] = useState<Track[]>(DEMO_TRACKS);
  const [currentIndex, setCurrentIndex] = useState<number>(-1);
  const [shuffle, setShuffle] = useState(false);
  const [repeat, setRepeat] = useState<RepeatMode>("off");

  // refs for use inside handlers
  const queueRef = useRef(queue);
  const indexRef = useRef(currentIndex);
  const shuffleRef = useRef(shuffle);
  const repeatRef = useRef(repeat);
  useEffect(() => { queueRef.current = queue; }, [queue]);
  useEffect(() => { indexRef.current = currentIndex; }, [currentIndex]);
  useEffect(() => { shuffleRef.current = shuffle; }, [shuffle]);
  useEffect(() => { repeatRef.current = repeat; }, [repeat]);

  useEffect(() => {
    setAudioModeAsync({
      playsInSilentMode: true,
      shouldPlayInBackground: false,
      interruptionMode: "mixWithOthers",
    }).catch(() => {});
  }, []);

  const current = currentIndex >= 0 && currentIndex < queue.length ? queue[currentIndex] : null;

  const playAtIndex = useCallback(async (idx: number, q: Track[]) => {
    if (idx < 0 || idx >= q.length) return;
    const tr = q[idx];
    setCurrentIndex(idx);
    try {
      player.replace({ uri: tr.url });
      player.play();
    } catch (e) {
      console.log("audio replace error", e);
    }
  }, [player]);

  const playTrack = useCallback(async (track: Track, q?: Track[]) => {
    const newQueue = q && q.length ? q : queueRef.current;
    const idx = newQueue.findIndex((t) => t.id === track.id);
    if (q && q.length) setQueue(newQueue);
    await playAtIndex(idx >= 0 ? idx : 0, newQueue);
  }, [playAtIndex]);

  const toggle = useCallback(() => {
    if (indexRef.current < 0) {
      // start from first
      playAtIndex(0, queueRef.current);
      return;
    }
    if (status.playing) {
      player.pause();
    } else {
      player.play();
    }
  }, [playAtIndex, player, status.playing]);

  const computeNext = useCallback((dir: 1 | -1) => {
    const q = queueRef.current;
    if (!q.length) return -1;
    const cur = indexRef.current;
    if (shuffleRef.current && dir === 1) {
      // random different index
      if (q.length === 1) return 0;
      let r = cur;
      while (r === cur) r = Math.floor(Math.random() * q.length);
      return r;
    }
    if (cur < 0) return 0;
    let n = cur + dir;
    if (n >= q.length) n = 0;
    if (n < 0) n = q.length - 1;
    return n;
  }, []);

  const next = useCallback(() => {
    const n = computeNext(1);
    if (n >= 0) playAtIndex(n, queueRef.current);
  }, [computeNext, playAtIndex]);

  const prev = useCallback(() => {
    // if more than 3s in, restart current
    if (status.currentTime > 3) {
      try { player.seekTo(0); } catch {}
      return;
    }
    const n = computeNext(-1);
    if (n >= 0) playAtIndex(n, queueRef.current);
  }, [computeNext, playAtIndex, player, status.currentTime]);

  const seekMs = useCallback((ms: number) => {
    try { player.seekTo(Math.max(0, ms / 1000)); } catch {}
  }, [player]);

  const toggleShuffle = useCallback(() => setShuffle((s) => !s), []);
  const cycleRepeat = useCallback(() => {
    setRepeat((r) => (r === "off" ? "all" : r === "all" ? "one" : "off"));
  }, []);

  // Auto-advance on track end
  const didEndRef = useRef(false);
  useEffect(() => {
    if (!status.duration || status.duration <= 0) return;
    // didJustFinish style detection
    if (!status.playing && status.currentTime > 0 && status.duration > 0 && status.currentTime >= status.duration - 0.25) {
      if (didEndRef.current) return;
      didEndRef.current = true;
      const mode = repeatRef.current;
      if (mode === "one") {
        try { player.seekTo(0); player.play(); } catch {}
      } else {
        const n = computeNext(1);
        if (n >= 0) playAtIndex(n, queueRef.current);
      }
      setTimeout(() => { didEndRef.current = false; }, 600);
    }
  }, [status.playing, status.currentTime, status.duration, computeNext, playAtIndex, player]);

  const value = useMemo<AudioCtx>(() => ({
    current,
    queue,
    isPlaying: !!status.playing,
    positionMs: Math.floor((status.currentTime || 0) * 1000),
    durationMs: Math.floor((status.duration || (current?.durationMs ? current.durationMs / 1000 : 0)) * 1000),
    shuffle,
    repeat,
    playTrack,
    toggle,
    next,
    prev,
    seekMs,
    toggleShuffle,
    cycleRepeat,
  }), [current, queue, status.playing, status.currentTime, status.duration, shuffle, repeat, playTrack, toggle, next, prev, seekMs, toggleShuffle, cycleRepeat]);

  return <Ctx.Provider value={value}>{children}</Ctx.Provider>;
}

export function useAudio() {
  const v = useContext(Ctx);
  if (!v) throw new Error("useAudio must be used inside AudioProvider");
  return v;
}
