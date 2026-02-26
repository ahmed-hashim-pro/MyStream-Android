package com.medoapps.www.onlinequran.ui.helpers

/**
 * Activity or fragment implements this is meant to be a jump destination/target.
 */
interface JumpDestination {
  fun jumpTo(page: Int)
  fun jumpToAndHighlight(page: Int, sura: Int, ayah: Int)
}
