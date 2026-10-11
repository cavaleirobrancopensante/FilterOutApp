package io.github.cavaleirobrancopensante.filterout

/**
 * The single place that decides whether the block screen may appear.
 *
 * After the block screen is dismissed, nothing can block again for
 * [graceMs] — not a new scan, and not a scan that was already running
 * when "Leave" was tapped. This guarantees the phone can never get stuck
 * in a block → leave → block loop, whatever else goes wrong.
 */
class BlockGate(
    private val graceMs: Long = GRACE_MS,
    private val clock: () -> Long
) {
    private var graceEndsAt = Long.MIN_VALUE

    /** True while we're inside the grace period after a dismissal. */
    fun inGrace(): Boolean = clock() < graceEndsAt

    /** Call before showing the block screen. Returns false during the grace period. */
    fun mayBlock(): Boolean = !inGrace()

    /** Call when the block screen is dismissed. Starts the grace period. */
    fun dismissed() {
        graceEndsAt = clock() + graceMs
    }

    companion object {
        const val GRACE_MS = 5000L
    }
}
