package dk.itu.moapd.copenhagenbuzz.laku.interfaces

import dk.itu.moapd.copenhagenbuzz.laku.models.DataViewModel
import dk.itu.moapd.copenhagenbuzz.laku.models.Event

/**
 * Helper class for maintaining favorites collection.
 */
class EventFavoriteStatusProvider(
    private val _model: DataViewModel
) : FavoritedStatusProvider {

    /**
     * Returns whether or not a user has favorited the given event.
     */
    override fun isEventFavorited(event: Event): Boolean {
        return _model.isEventFavorited(event)
    }

    /**
     * Returns an appropriate listener for TimelineFragment's favorite button to use.
     */
    override fun getFavoriteAddedListener(): (Int) -> Unit {
        return { position ->
            val event = _model.events.value?.get(position)
            _model.addToFavorites(event!!)
        }
    }

    /**
     * Returns an appropriate listener for FavoritesFragment's favorite button to use.
     */
    override fun getFavoriteRemovedListener(): (Int) -> Unit {
        return { position ->
            val event = _model.favorites.value?.get(position)
            _model.removeFromFavorites(event!!)
        }
    }
}