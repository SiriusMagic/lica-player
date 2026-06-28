import { View, Text, StyleSheet, TouchableOpacity, Image } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { useRouter } from "expo-router";
import { useAudio } from "@/src/contexts/AudioContext";
import { useLanguage } from "@/src/contexts/LanguageContext";

export default function MiniPlayer() {
  const router = useRouter();
  const { current, isPlaying, toggle } = useAudio();
  const { t } = useLanguage();

  if (!current) return null;

  return (
    <TouchableOpacity
      activeOpacity={0.92}
      style={styles.wrap}
      onPress={() => router.push("/player")}
      testID="mini-player"
    >
      <Image source={{ uri: current.cover }} style={styles.cover} />
      <View style={styles.info}>
        <Text style={styles.title} numberOfLines={1}>
          {current.title}
        </Text>
        <Text style={styles.subtitle} numberOfLines={1}>
          {isPlaying ? t("now_playing") : t("tap_to_play")}
        </Text>
      </View>
      <TouchableOpacity
        onPress={(e) => {
          e.stopPropagation();
          toggle();
        }}
        style={styles.playBtn}
        testID="mini-player-play-button"
      >
        <Ionicons
          name={isPlaying ? "pause" : "play"}
          size={22}
          color="#F5C518"
        />
      </TouchableOpacity>
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  wrap: {
    flexDirection: "row",
    alignItems: "center",
    backgroundColor: "#3A2E08",
    borderRadius: 18,
    paddingVertical: 10,
    paddingHorizontal: 10,
    borderWidth: 1,
    borderColor: "#5C470A",
    shadowColor: "#F5C518",
    shadowOpacity: 0.25,
    shadowRadius: 16,
    shadowOffset: { width: 0, height: 6 },
    elevation: 6,
  },
  cover: { width: 48, height: 48, borderRadius: 10, backgroundColor: "#222" },
  info: { flex: 1, marginLeft: 12, marginRight: 8 },
  title: { color: "#FFFFFF", fontSize: 16, fontWeight: "700" },
  subtitle: { color: "#E8C25C", fontSize: 13, marginTop: 2, fontWeight: "500" },
  playBtn: {
    width: 44,
    height: 44,
    borderRadius: 22,
    borderWidth: 2,
    borderColor: "#F5C518",
    alignItems: "center",
    justifyContent: "center",
  },
});
