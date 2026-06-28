import { useEffect, useMemo, useState, useRef } from "react";
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  Image,
  ScrollView,
  TextInput,
  Modal,
  Animated,
  Easing,
} from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { Ionicons } from "@expo/vector-icons";

import { useLanguage } from "@/src/contexts/LanguageContext";
import { useAudio } from "@/src/contexts/AudioContext";
import { DEMO_TRACKS, Track } from "@/src/data/tracks";
import MiniPlayer from "@/src/components/MiniPlayer";
import { storage } from "@/src/utils/storage";
import { LANGUAGES, LangCode } from "@/src/i18n/translations";

const PENGUIN_URL =
  "https://customer-assets.emergentagent.com/job_76e38cfc-d123-494d-b977-20c7b50770d7/artifacts/3wlz6t8o_1782653108966.jpg";

type TabKey = "playlists" | "folders" | "artists" | "albums";
type SortKey = "title" | "artist" | "recent";

// ---------- helpers ----------
function groupBy<T>(arr: T[], key: (t: T) => string) {
  const m = new Map<string, T[]>();
  for (const item of arr) {
    const k = key(item);
    if (!m.has(k)) m.set(k, []);
    m.get(k)!.push(item);
  }
  return Array.from(m.entries()).map(([name, items]) => ({ name, items }));
}

// ---------- screen ----------
export default function LibraryScreen() {
  const { t, lang, setLang } = useLanguage();
  const { current, playTrack } = useAudio();

  const [tab, setTab] = useState<TabKey>("playlists");
  const [scanned, setScanned] = useState(false);
  const [tracks, setTracks] = useState<Track[]>([]);
  const [scanning, setScanning] = useState(false);
  const [searchVisible, setSearchVisible] = useState(false);
  const [query, setQuery] = useState("");
  const [sortKey, setSortKey] = useState<SortKey>("title");
  const [sortVisible, setSortVisible] = useState(false);
  const [langModal, setLangModal] = useState(false);
  const [scanProgress, setScanProgress] = useState(0);

  const spinAnim = useRef(new Animated.Value(0)).current;

  // Restore scanned + tracks
  useEffect(() => {
    (async () => {
      const s = await storage.getItem<boolean>("@app/scanned", false);
      if (s) {
        setScanned(true);
        setTracks(DEMO_TRACKS);
      } else {
        // Show 1 track teaser like the screenshot
        setTracks(DEMO_TRACKS.slice(0, 1));
      }
    })();
  }, []);

  useEffect(() => {
    if (scanning) {
      Animated.loop(
        Animated.timing(spinAnim, {
          toValue: 1,
          duration: 1000,
          easing: Easing.linear,
          useNativeDriver: true,
        })
      ).start();
    } else {
      spinAnim.stopAnimation();
      spinAnim.setValue(0);
    }
  }, [scanning, spinAnim]);

  const runScan = () => {
    if (scanning) return;
    setScanning(true);
    setScanProgress(0);
    let i = 0;
    const id = setInterval(() => {
      i += 1;
      setScanProgress(Math.min(100, i * 10));
      if (i >= 10) {
        clearInterval(id);
        setTracks(DEMO_TRACKS);
        setScanned(true);
        setScanning(false);
        storage.setItem("@app/scanned", true);
      }
    }, 180);
  };

  // Filtering
  const filtered = useMemo(() => {
    let arr = tracks;
    if (query.trim()) {
      const q = query.trim().toLowerCase();
      arr = arr.filter(
        (tr) =>
          tr.title.toLowerCase().includes(q) ||
          tr.artist.toLowerCase().includes(q) ||
          tr.album.toLowerCase().includes(q)
      );
    }
    const sorted = [...arr];
    if (sortKey === "title") sorted.sort((a, b) => a.title.localeCompare(b.title));
    if (sortKey === "artist") sorted.sort((a, b) => a.artist.localeCompare(b.artist));
    // recent = insertion order
    return sorted;
  }, [tracks, query, sortKey]);

  // Groupings per tab
  const groupsByAlbum = useMemo(() => groupBy(filtered, (t) => t.album), [filtered]);
  const groupsByArtist = useMemo(() => groupBy(filtered, (t) => t.artist), [filtered]);
  const groupsByFolder = useMemo(() => groupBy(filtered, (t) => t.folder), [filtered]);

  const onPlay = (tr: Track, queue: Track[]) => {
    playTrack(tr, queue);
  };

  const spin = spinAnim.interpolate({
    inputRange: [0, 1],
    outputRange: ["0deg", "360deg"],
  });

  // ---------- renderers ----------
  const renderPlaylists = () => (
    <View>
      {filtered.length === 0 ? (
        <EmptyHint t={t} />
      ) : (
        filtered.map((tr) => (
          <RowItem
            key={tr.id}
            cover={tr.cover}
            title={tr.title}
            subtitle={`${tr.artist} - ${tr.album}`}
            onPress={() => onPlay(tr, filtered)}
          />
        ))
      )}
    </View>
  );

  const renderGrouped = (
    groups: { name: string; items: Track[] }[],
    icon: keyof typeof Ionicons.glyphMap
  ) => (
    <View>
      {groups.length === 0 ? (
        <EmptyHint t={t} />
      ) : (
        groups.map((g) => (
          <RowItem
            key={g.name}
            iconNode={
              <View style={styles.iconCover}>
                <Ionicons name={icon} size={24} color="#F5C518" />
              </View>
            }
            title={g.name}
            subtitle={`${g.items.length} ${g.items.length === 1 ? t("song") : t("songs")}`}
            onPress={() => onPlay(g.items[0], g.items)}
          />
        ))
      )}
    </View>
  );

  return (
    <SafeAreaView style={styles.safe} edges={["top"]}>
      {/* Top bar */}
      <View style={styles.topBar} testID="library-top-bar">
        <Image source={{ uri: PENGUIN_URL }} style={styles.brandLogo} />
        <View style={styles.topActions}>
          <TouchableOpacity
            style={styles.iconBtn}
            onPress={() => setSearchVisible((s) => !s)}
            testID="library-search-toggle"
          >
            <Ionicons name="search" size={22} color="#F5C518" />
          </TouchableOpacity>
          <TouchableOpacity
            style={styles.iconBtn}
            onPress={() => setSortVisible(true)}
            testID="library-sort-button"
          >
            <Ionicons name="filter" size={22} color="#F5C518" />
          </TouchableOpacity>
          <TouchableOpacity
            style={styles.iconBtn}
            onPress={() => setLangModal(true)}
            testID="library-language-button"
          >
            <Ionicons name="language" size={22} color="#F5C518" />
          </TouchableOpacity>
        </View>
      </View>

      {/* Search bar (toggle) */}
      {searchVisible && (
        <View style={styles.searchBox}>
          <Ionicons name="search" size={18} color="#8B8B92" />
          <TextInput
            value={query}
            onChangeText={setQuery}
            placeholder={t("search_placeholder")}
            placeholderTextColor="#8B8B92"
            style={styles.searchInput}
            autoFocus
            testID="library-search-input"
          />
          {!!query && (
            <TouchableOpacity onPress={() => setQuery("")}>
              <Ionicons name="close-circle" size={18} color="#8B8B92" />
            </TouchableOpacity>
          )}
        </View>
      )}

      {/* Filter chips */}
      <View style={styles.chipsRow}>
        <ScrollView
          horizontal
          showsHorizontalScrollIndicator={false}
          contentContainerStyle={styles.chipsContent}
        >
          {(
            [
              { key: "playlists", icon: "musical-note", label: t("playlists") },
              { key: "folders", icon: "folder-open", label: t("folders") },
              { key: "artists", icon: "person", label: t("artists") },
              { key: "albums", icon: "disc", label: t("albums") },
            ] as { key: TabKey; icon: keyof typeof Ionicons.glyphMap; label: string }[]
          ).map((c) => {
            const active = tab === c.key;
            return (
              <TouchableOpacity
                key={c.key}
                onPress={() => setTab(c.key)}
                activeOpacity={0.85}
                style={[styles.chip, active && styles.chipActive]}
                testID={`library-tab-${c.key}`}
              >
                <Ionicons
                  name={c.icon}
                  size={16}
                  color={active ? "#F5C518" : "#8B8B92"}
                />
                <Text style={[styles.chipText, active && styles.chipTextActive]}>
                  {c.label}
                </Text>
              </TouchableOpacity>
            );
          })}
        </ScrollView>
      </View>

      {/* List content */}
      <ScrollView
        style={{ flex: 1 }}
        contentContainerStyle={[
          styles.listContent,
          { paddingBottom: current ? 130 : 32 },
        ]}
        showsVerticalScrollIndicator={false}
      >
        {tab === "playlists" && renderPlaylists()}
        {tab === "folders" && renderGrouped(groupsByFolder, "folder-open")}
        {tab === "artists" && renderGrouped(groupsByArtist, "person")}
        {tab === "albums" && renderGrouped(groupsByAlbum, "disc")}

        {/* Scan CTA */}
        <View style={styles.scanWrap}>
          <Text style={styles.scanQuestion}>{t("songs_not_found")}</Text>
          <TouchableOpacity
            activeOpacity={0.9}
            onPress={runScan}
            style={styles.scanBtn}
            testID="library-scan-button"
            disabled={scanning}
          >
            <Animated.View style={{ transform: [{ rotate: scanning ? spin : "0deg" }] }}>
              <Ionicons
                name={scanning ? "sync" : "scan"}
                size={20}
                color="#0A0A0E"
              />
            </Animated.View>
            <Text style={styles.scanBtnText}>
              {scanning ? `${t("scanning")} ${scanProgress}%` : t("scan_folders")}
            </Text>
          </TouchableOpacity>
          {!scanned && !scanning && (
            <Text style={styles.scanHint}>{t("scan_hint")}</Text>
          )}
        </View>
      </ScrollView>

      {/* Floating mini player */}
      {current && (
        <View style={styles.miniWrap}>
          <MiniPlayer />
        </View>
      )}

      {/* Sort modal */}
      <BottomSheet
        visible={sortVisible}
        title={t("sort_by")}
        onClose={() => setSortVisible(false)}
      >
        {([
          { key: "title", label: t("sort_title"), icon: "text" },
          { key: "artist", label: t("sort_artist"), icon: "person" },
          { key: "recent", label: t("sort_recent"), icon: "time" },
        ] as { key: SortKey; label: string; icon: keyof typeof Ionicons.glyphMap }[]).map(
          (o) => (
            <TouchableOpacity
              key={o.key}
              style={styles.sheetRow}
              onPress={() => {
                setSortKey(o.key);
                setSortVisible(false);
              }}
              testID={`sort-option-${o.key}`}
            >
              <Ionicons name={o.icon} size={20} color="#F5C518" />
              <Text style={styles.sheetRowText}>{o.label}</Text>
              {sortKey === o.key && (
                <Ionicons name="checkmark" size={20} color="#F5C518" />
              )}
            </TouchableOpacity>
          )
        )}
      </BottomSheet>

      {/* Language modal */}
      <BottomSheet
        visible={langModal}
        title={t("language")}
        onClose={() => setLangModal(false)}
      >
        {LANGUAGES.map((l) => (
          <TouchableOpacity
            key={l.code}
            style={styles.sheetRow}
            onPress={async () => {
              await setLang(l.code as LangCode);
              setLangModal(false);
            }}
            testID={`lang-switch-${l.code}`}
          >
            <Ionicons name="globe-outline" size={20} color="#F5C518" />
            <Text style={styles.sheetRowText}>{l.label}</Text>
            {lang === l.code && (
              <Ionicons name="checkmark" size={20} color="#F5C518" />
            )}
          </TouchableOpacity>
        ))}
      </BottomSheet>
    </SafeAreaView>
  );
}

// ---------- subcomponents ----------
function EmptyHint({ t }: { t: (k: string) => string }) {
  return (
    <View style={styles.emptyHint}>
      <Ionicons name="musical-notes-outline" size={40} color="#2A2A35" />
      <Text style={styles.emptyText}>{t("no_songs")}</Text>
    </View>
  );
}

function RowItem({
  cover,
  iconNode,
  title,
  subtitle,
  onPress,
}: {
  cover?: string;
  iconNode?: React.ReactNode;
  title: string;
  subtitle: string;
  onPress: () => void;
}) {
  return (
    <TouchableOpacity
      activeOpacity={0.85}
      style={styles.row}
      onPress={onPress}
      testID="library-row-item"
    >
      {cover ? (
        <Image source={{ uri: cover }} style={styles.rowCover} />
      ) : (
        iconNode
      )}
      <View style={styles.rowInfo}>
        <Text style={styles.rowTitle} numberOfLines={1}>
          {title}
        </Text>
        <Text style={styles.rowSubtitle} numberOfLines={1}>
          {subtitle}
        </Text>
      </View>
    </TouchableOpacity>
  );
}

function BottomSheet({
  visible,
  title,
  onClose,
  children,
}: {
  visible: boolean;
  title: string;
  onClose: () => void;
  children: React.ReactNode;
}) {
  return (
    <Modal
      visible={visible}
      transparent
      animationType="fade"
      onRequestClose={onClose}
    >
      <TouchableOpacity
        style={styles.modalBackdrop}
        activeOpacity={1}
        onPress={onClose}
      >
        <View style={styles.sheet}>
          <View style={styles.sheetHandle} />
          <Text style={styles.sheetTitle}>{title}</Text>
          {children}
        </View>
      </TouchableOpacity>
    </Modal>
  );
}

const styles = StyleSheet.create({
  safe: { flex: 1, backgroundColor: "#0A0A0E" },
  topBar: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    paddingHorizontal: 20,
    paddingTop: 8,
    paddingBottom: 8,
  },
  brandLogo: { width: 44, height: 44, borderRadius: 12 },
  topActions: { flexDirection: "row", gap: 8 },
  iconBtn: {
    width: 44,
    height: 44,
    borderRadius: 22,
    alignItems: "center",
    justifyContent: "center",
  },
  searchBox: {
    flexDirection: "row",
    alignItems: "center",
    gap: 8,
    marginHorizontal: 20,
    backgroundColor: "#15151B",
    borderRadius: 14,
    paddingHorizontal: 14,
    paddingVertical: 10,
    borderWidth: 1,
    borderColor: "#22222B",
  },
  searchInput: { flex: 1, color: "#FFFFFF", fontSize: 15 },
  chipsRow: { height: 56, marginTop: 6 },
  chipsContent: { paddingHorizontal: 16, gap: 10, alignItems: "center" },
  chip: {
    height: 36,
    minWidth: 92,
    paddingHorizontal: 14,
    borderRadius: 18,
    backgroundColor: "#15151B",
    borderWidth: 1,
    borderColor: "#22222B",
    flexDirection: "row",
    alignItems: "center",
    gap: 6,
    flexShrink: 0,
  },
  chipActive: { borderColor: "#F5C518" },
  chipText: { color: "#A0A0A5", fontSize: 14, fontWeight: "600" },
  chipTextActive: { color: "#F5C518" },
  listContent: { paddingHorizontal: 16, paddingTop: 8 },
  row: {
    flexDirection: "row",
    alignItems: "center",
    paddingVertical: 10,
    paddingHorizontal: 4,
  },
  rowCover: {
    width: 56,
    height: 56,
    borderRadius: 12,
    backgroundColor: "#222",
  },
  iconCover: {
    width: 56,
    height: 56,
    borderRadius: 12,
    backgroundColor: "#15151B",
    borderWidth: 1,
    borderColor: "#22222B",
    alignItems: "center",
    justifyContent: "center",
  },
  rowInfo: { flex: 1, marginLeft: 12 },
  rowTitle: { color: "#FFFFFF", fontSize: 17, fontWeight: "700" },
  rowSubtitle: { color: "#A0A0A5", fontSize: 13, marginTop: 2 },
  scanWrap: {
    marginTop: 24,
    paddingHorizontal: 8,
    alignItems: "center",
    borderTopWidth: 1,
    borderTopColor: "#15151B",
    paddingTop: 24,
  },
  scanQuestion: { color: "#A0A0A5", fontSize: 15, marginBottom: 14 },
  scanBtn: {
    flexDirection: "row",
    alignItems: "center",
    gap: 10,
    backgroundColor: "#F5C518",
    paddingHorizontal: 22,
    paddingVertical: 14,
    borderRadius: 999,
    shadowColor: "#F5C518",
    shadowOpacity: 0.35,
    shadowRadius: 14,
    shadowOffset: { width: 0, height: 4 },
    elevation: 6,
  },
  scanBtnText: { color: "#0A0A0E", fontWeight: "800", fontSize: 16 },
  scanHint: {
    marginTop: 12,
    color: "#6B6B72",
    fontSize: 12,
    textAlign: "center",
    paddingHorizontal: 24,
  },
  emptyHint: { alignItems: "center", paddingVertical: 32, gap: 10 },
  emptyText: { color: "#8B8B92", fontSize: 14 },
  miniWrap: {
    position: "absolute",
    left: 16,
    right: 16,
    bottom: 24,
  },
  modalBackdrop: {
    flex: 1,
    backgroundColor: "rgba(0,0,0,0.6)",
    justifyContent: "flex-end",
  },
  sheet: {
    backgroundColor: "#15151B",
    borderTopLeftRadius: 24,
    borderTopRightRadius: 24,
    paddingHorizontal: 16,
    paddingTop: 12,
    paddingBottom: 28,
    borderTopWidth: 1,
    borderColor: "#22222B",
  },
  sheetHandle: {
    width: 40,
    height: 4,
    borderRadius: 2,
    backgroundColor: "#3A3A45",
    alignSelf: "center",
    marginBottom: 12,
  },
  sheetTitle: {
    color: "#FFFFFF",
    fontSize: 18,
    fontWeight: "700",
    marginBottom: 8,
    paddingHorizontal: 6,
  },
  sheetRow: {
    flexDirection: "row",
    alignItems: "center",
    gap: 14,
    paddingHorizontal: 8,
    paddingVertical: 14,
  },
  sheetRowText: { color: "#FFFFFF", fontSize: 16, flex: 1, fontWeight: "500" },
});
