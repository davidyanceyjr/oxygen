package com.oxygen.weather.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FirstRunLocationStateHolderTest {
    @Test
    fun `default app state starts at first-run manual location entry`() {
        val stateHolder = OxygenAppStateHolder()

        val state = stateHolder.presentationState

        assertTrue(state.screen is OxygenAppScreen.FirstRunLocationEntry)
        assertNull(state.selectedLocation)
        assertFalse(state.isShowingHome)
        assertFalse(state.usesSampleWeather)
    }

    @Test
    fun `manual search submission keeps query visible without requesting permission or routing home`() {
        val stateHolder = OxygenAppStateHolder()

        stateHolder.onManualLocationQueryChanged("Madison")
        stateHolder.onManualLocationSearchSubmitted()

        val firstRunState = stateHolder.presentationState.screen as OxygenAppScreen.FirstRunLocationEntry
        assertEquals("Madison", firstRunState.query)
        assertEquals("Madison", firstRunState.submittedQuery)
        assertEquals(FirstRunLocationMessage.SearchNotConnected, firstRunState.message)
        assertNull(stateHolder.consumeNextCommand())
        assertNull(stateHolder.presentationState.selectedLocation)
        assertFalse(stateHolder.presentationState.isShowingHome)
        assertFalse(firstRunState.isSearching)
        assertFalse(firstRunState.hasResults)
    }

    @Test
    fun `manual search trims submitted query but retains typed query`() {
        val stateHolder = OxygenAppStateHolder()

        stateHolder.onManualLocationQueryChanged("  Madison, WI  ")
        stateHolder.onManualLocationSearchSubmitted()

        val firstRunState = stateHolder.presentationState.screen as OxygenAppScreen.FirstRunLocationEntry
        assertEquals("  Madison, WI  ", firstRunState.query)
        assertEquals("Madison, WI", firstRunState.submittedQuery)
        assertEquals(FirstRunLocationMessage.SearchNotConnected, firstRunState.message)
    }

    @Test
    fun `empty manual search asks for a place without requesting permission or routing home`() {
        val stateHolder = OxygenAppStateHolder()

        stateHolder.onManualLocationQueryChanged(" ")
        stateHolder.onManualLocationSearchSubmitted()

        val firstRunState = stateHolder.presentationState.screen as OxygenAppScreen.FirstRunLocationEntry
        assertEquals(FirstRunLocationMessage.EnterPlaceName, firstRunState.message)
        assertNull(firstRunState.submittedQuery)
        assertNull(stateHolder.consumeNextCommand())
        assertFalse(stateHolder.presentationState.isShowingHome)
    }

    @Test
    fun `use my location emits one provider-neutral permission request command`() {
        val stateHolder = OxygenAppStateHolder()

        stateHolder.onUseMyLocation()

        assertEquals(OxygenAppCommand.RequestLocationPermission, stateHolder.consumeNextCommand())
        assertNull(stateHolder.consumeNextCommand())
        assertTrue(stateHolder.presentationState.screen is OxygenAppScreen.FirstRunLocationEntry)
        assertNull(stateHolder.presentationState.selectedLocation)
        assertFalse(stateHolder.presentationState.isShowingHome)
    }

    @Test
    fun `denied location permission remains on manual entry with optional-location message`() {
        val stateHolder = OxygenAppStateHolder()

        stateHolder.onUseMyLocation()
        stateHolder.consumeNextCommand()
        stateHolder.onLocationPermissionResult(LocationPermissionResult.Denied)

        val firstRunState = stateHolder.presentationState.screen as OxygenAppScreen.FirstRunLocationEntry
        assertEquals(FirstRunLocationMessage.LocationPermissionOptional, firstRunState.message)
        assertNull(stateHolder.presentationState.selectedLocation)
        assertFalse(stateHolder.presentationState.isShowingHome)
    }

    @Test
    fun `unavailable location permission remains on manual entry with optional-location message`() {
        val stateHolder = OxygenAppStateHolder()

        stateHolder.onLocationPermissionResult(LocationPermissionResult.Unavailable)

        val firstRunState = stateHolder.presentationState.screen as OxygenAppScreen.FirstRunLocationEntry
        assertEquals(FirstRunLocationMessage.LocationPermissionOptional, firstRunState.message)
        assertNull(stateHolder.presentationState.selectedLocation)
        assertFalse(stateHolder.presentationState.isShowingHome)
    }
}
