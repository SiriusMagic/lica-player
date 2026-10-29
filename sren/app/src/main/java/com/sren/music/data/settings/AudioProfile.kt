package com.sren.music.data.settings

/**
 * How the player trades battery for audio fidelity.
 *
 * Lowering the bitrate of a local file would not save battery (re-encoding costs
 * more than playing the original), so the profiles change *how* audio is
 * processed instead:
 *
 * - [LONG_LIFE]: audio offload whenever the device supports it, even without
 *   gapless. The DSP decodes and the CPU sleeps for seconds at a time (Media3
 *   releases the wake lock while "sleeping for offload"). The UI is capped at
 *   60 Hz while it is visible.
 * - [BALANCED]: offload only when the device can keep gapless transitions.
 *   Otherwise normal playback. Default.
 * - [HIGH_FIDELITY]: no offload; 32-bit float pipeline so hi-res files are not
 *   truncated to 16 bits. Uses the most battery.
 *
 * When the equalizer arrives, BALANCED will also turn offload off while the
 * screen is on (effects cannot run on offloaded audio).
 */
enum class AudioProfile {
    LONG_LIFE,
    BALANCED,
    HIGH_FIDELITY;

    val usesFloatOutput: Boolean get() = this == HIGH_FIDELITY
    val capsRefreshRate: Boolean get() = this == LONG_LIFE

    companion object {
        val DEFAULT = BALANCED
    }
}
