package dk.itu.moapd.copenhagenbuzz.laku.interfaces

import dk.itu.moapd.copenhagenbuzz.laku.adapters.FavoriteAdapter
import dk.itu.moapd.copenhagenbuzz.laku.models.Event

interface FavoritedStatusProvider {
    fun isEventFavorited(event: Event): Boolean
    fun getFavoriteAddedListener(): (Int) -> Unit
    fun getFavoriteRemovedListener(): (Int) -> Unit
}