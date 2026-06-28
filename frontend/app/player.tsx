import { useState, useRef } from "react";
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  Image,
  PanResponder,
} from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { Ionicons } from "@expo/vector-icons";
import { useRouter } from "expo-router";
import { useAudio } from "@/src/contexts/AudioContext";
import { useLanguage } from "@/src/contexts/LanguageContext";
import { formatMs } from "@/src/data/tracks";

export default function PlayerScreen() {
  const router = useRouter();
  const { t } = useLanguage();
  const {
    current,
    isPlaying,
    positionMs,
    durationMs,
    toggle,
    next,
    prev,
    seekMs,
    shuffle,
    repeat,
    toggleShuffle,
    cycleRepeat,
  } = useAudio();

  const [barWidth, setBarWidth] = useState(1);
  const [isDragging, setIsDragging] = useState(false);
  const dragPos = useRef(0);
  const [previewMs, setPreviewMs] = useState(0);

  const dur = durationMs || current?.durationMs || 0;
  const pos = isDragging ? previewMs : positionMs;
  const progress = dur > 0 ? Math.min(1, Math.max(0, pos / dur)) : 0;

  const panResponder = useRef(
    PanResponder.create({
      onStartShouldSetPanResponder: () => true,
      onMoveShouldSetPanResponder: () => true,
      onPanResponderGrant: (e) => {
        setIsDragging(true);
        const x = e.nativeEvent.locationX;
        const p = Math.max(0, Math.min(1, x / Math.max(1, barWidth)));
        dragPos.current = p;
        setPreviewMs(p * (dur || 1));
      },
      onPanResponderMove: (e) => {
        const x = Math.max(0, Math.min(barWidth, e.nativeEvent.locationX));
        const p = x / Math.max(1, barWidth);
        dragPos.current = p;
        setPreviewMs(p * (dur || 1));
      },
      onPanResponderRelease: () => {
        const ms = dragPos.current * (dur || 1);
        seekMs(ms);
        setTimeout(() => setIsDragging(false), 100);
      },
    })
  ).current;

  if (!current) {
    return (
      <SafeAreaView style={styles.safe}>
        <View style={styles.emptyWrap}>
          <Ionicons name="musical-notes-outline" size={56} color="#3A3A45" />
          <Text style={styles.emptyText}>{t("no_songs")}</Text>
          <TouchableOpacity style={styles.closeBtn} onPress={() => router.back()}>
            <Text style={styles.closeBtnText}>{t("cancel")}</Text>
          </TouchableOpacity>
        </View>
      </SafeAreaView>
    );
  }

  return (
    <SafeAreaView style={styles.safe} edges={["top", "bottom"]}>
      {/* Header */}
      <View style={styles.header}>
        <TouchableOpacity
          onPress={() => router.back()}
          style={styles.hBtn}
          testID="player-close-button"
        >
          <Ionicons name="chevron-down" size={28} color="#FFFFFF" />
        </TouchableOpacity>
        <View style={{ alignItems: "center" }}>
          <Text style={styles.hLabel}>{t("now_playing")}</Text>
          <Text style={styles.hAlbum} numberOfLines={1}>
            {current.album}
          </Text>
        </View>
        <View style={styles.hBtn} />
      </View>

      {/* Cover */}
      <View style={styles.coverWrap}>
        <Image source={{ uri: current.cover }} style={styles.cover} />
      </View>

      {/* Track info */}
      <View style={styles.info}>
        <Text style={styles.title} numberOfLines={1}>
          {current.title}
        </Text>
        <Text style={styles.artist} numberOfLines={1}>
          {current.artist}
        </Text>
      </View>

      {/* Progress */}
      <View style={styles.progressWrap}>
        <View
          style={styles.progressBg}
          onLayout={(e) => setBarWidth(e.nativeEvent.layout.width)}
          {...panResponder.panHandlers}
        >
          <View
            style={[styles.progressFill, { width: `${progress * 100}%` }]}
          />
          <View
            style={[styles.thumb, { left: `${progress * 100}%` }]}
          />
        </View>
        <View style={styles.times}>
          <Text style={styles.timeText}>{formatMs(pos)}</Text>
          <Text style={styles.timeText}>{formatMs(dur)}</Text>
        </View>
      </View>

      {/* Controls */}
      <View style={styles.controls}>
        <TouchableOpacity
          onPress={toggleShuffle}
          style={styles.smallBtn}
          testID="player-shuffle-button"
        >
          <Ionicons
            name="shuffle"
            size={22}
            color={shuffle ? "#F5C518" : "#8B8B92"}
          />
        </TouchableOpacity>
        <TouchableOpacity
          onPress={prev}
          style={styles.medBtn}
          testID="player-prev-button"
        >
          <Ionicons name="play-skip-back" size={32} color="#FFFFFF" />
        </TouchableOpacity>
        <TouchableOpacity
          onPress={toggle}
          style={styles.playBtn}
          testID="player-play-button"
        >
          <Ionicons
            name={isPlaying ? "pause" : "play"}
            size={36}
            color="#0A0A0E"
          />
        </TouchableOpacity>
        <TouchableOpacity
          onPress={next}
          style={styles.medBtn}
          testID="player-next-button"
        >
          <Ionicons name="play-skip-forward" size={32} color="#FFFFFF" />
        </TouchableOpacity>
        <TouchableOpacity
          onPress={cycleRepeat}
          style={styles.smallBtn}
          testID="player-repeat-button"
        >
          <Ionicons
            name={repeat === "one" ? "repeat" : "repeat"}
            size={22}
            color={repeat !== "off" ? "#F5C518" : "#8B8B92"}
          />
          {repeat === "one" && <View style={styles.repeatDot} />}
        </TouchableOpacity>
      </View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safe: { flex: 1, backgroundColor: "#0A0A0E" },
  header: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    paddingHorizontal: 16,
    paddingVertical: 10,
  },
  hBtn: { width: 44, height: 44, alignItems: "center", justifyContent: "center" },
  hLabel: { color: "#8B8B92", fontSize: 12, letterSpacing: 1, textTransform: "uppercase" },
  hAlbum: { color: "#FFFFFF", fontSize: 14, fontWeight: "600", maxWidth: 200 },
  coverWrap: {
    alignItems: "center",
    marginTop: 16,
    marginHorizontal: 24,
  },
  cover: {
    width: "100%",
    aspectRatio: 1,
    borderRadius: 24,
    backgroundColor: "#15151B",
  },
  info: { alignItems: "center", marginTop: 28, paddingHorizontal: 24 },
  title: { color: "#FFFFFF", fontSize: 26, fontWeight: "800", textAlign: "center" },
  artist: { color: "#A0A0A5", fontSize: 16, marginTop: 6, textAlign: "center" },
  progressWrap: { marginTop: 28, paddingHorizontal: 28 },
  progressBg: {
    height: 30,
    justifyContent: "center",
  },
  progressFill: {
    height: 4,
    backgroundColor: "#F5C518",
    borderRadius: 2,
    position: "absolute",
    left: 0,
  },
  thumb: {
    width: 14,
    height: 14,
    borderRadius: 7,
    backgroundColor: "#F5C518",
    position: "absolute",
    marginLeft: -7,
    shadowColor: "#F5C518",
    shadowOpacity: 0.5,
    shadowRadius: 8,
  },
  times: { flexDirection: "row", justifyContent: "space-between", marginTop: 2 },
  timeText: { color: "#8B8B92", fontSize: 12 },
  controls: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-around",
    marginTop: 28,
    paddingHorizontal: 16,
    marginBottom: 12,
  },
  smallBtn: { width: 44, height: 44, alignItems: "center", justifyContent: "center" },
  medBtn: { width: 56, height: 56, alignItems: "center", justifyContent: "center" },
  playBtn: {
    width: 76,
    height: 76,
    borderRadius: 38,
    backgroundColor: "#F5C518",
    alignItems: "center",
    justifyContent: "center",
    shadowColor: "#F5C518",
    shadowOpacity: 0.5,
    shadowRadius: 16,
    shadowOffset: { width: 0, height: 6 },
    elevation: 8,
  },
  repeatDot: {
    position: "absolute",
    bottom: 8,
    width: 4,
    height: 4,
    borderRadius: 2,
    backgroundColor: "#F5C518",
  },
  emptyWrap: { flex: 1, alignItems: "center", justifyContent: "center", gap: 16 },
  emptyText: { color: "#A0A0A5" },
  closeBtn: {
    backgroundColor: "#15151B",
    borderRadius: 999,
    paddingVertical: 12,
    paddingHorizontal: 24,
  },
  closeBtnText: { color: "#FFFFFF", fontWeight: "600" },
});
