package com.mohit.videoskipper.service

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner

/**
 * A minimal LifecycleOwner + SavedStateRegistryOwner for use inside a Service.
 *
 * IMPORTANT: This is a CLASS, not a singleton object. Lifecycle's DESTROYED state
 * is terminal — once set, it can never transition to any other state. If this were
 * a singleton reused across multiple on/off toggles of the overlay, the second
 * "on" after an "off" would crash trying to revive a destroyed lifecycle.
 *
 * Instead, create a NEW instance of this class every time OverlayService starts
 * (in onCreate), and simply discard it when the service stops (onDestroy).
 */
class OverlayLifecycleOwner : LifecycleOwner, SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    fun onCreate() {
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }

    fun onDestroy() {
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
    }
}