package dk.itu.moapd.copenhagenbuzz.laku.models

data class FavoriteOperation(val operation: Operation, val events: List<String>){
    enum class Operation {
        ADD,
        REMOVE
    }
}
