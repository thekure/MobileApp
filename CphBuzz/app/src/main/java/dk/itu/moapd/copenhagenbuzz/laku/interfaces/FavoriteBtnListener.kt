package dk.itu.moapd.copenhagenbuzz.laku.interfaces

import dk.itu.moapd.copenhagenbuzz.laku.adapters.FavoriteAdapter

interface FavoriteBtnListener {
    fun onFavoriteBtnClicked(adapter: FavoriteAdapter, position: Int)
}