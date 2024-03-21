package dk.itu.moapd.copenhagenbuzz.laku.interfaces

import dk.itu.moapd.copenhagenbuzz.laku.models.DataViewModel
import dk.itu.moapd.copenhagenbuzz.laku.models.Event

class EventFavoriteStatusProvider(
    private val _model: DataViewModel
) : FavoritedStatusProvider {
    override fun isEventFavorited(event: Event): Boolean {
        return _model.isEventFavorited(event)
    }

    override fun getFavoriteAddedListener(): (Int) -> Unit {
        return { position ->
            val event = _model.events.value?.get(position)
            _model.addToFavorites(event!!)
        }
    }

    override fun getFavoriteRemovedListener(): (Int) -> Unit {
        return { position ->
            val event = _model.favorites.value?.get(position)
            _model.removeFromFavorites(event!!)
        }
    }
}