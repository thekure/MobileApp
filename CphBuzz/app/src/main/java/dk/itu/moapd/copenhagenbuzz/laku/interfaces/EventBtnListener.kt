package dk.itu.moapd.copenhagenbuzz.laku.interfaces

import dk.itu.moapd.copenhagenbuzz.laku.models.Event

interface EventBtnListener {
    fun onEditEventClicked(event: Event, position: Int)
    fun onInfoEventClicked(event: Event, position: Int)
}