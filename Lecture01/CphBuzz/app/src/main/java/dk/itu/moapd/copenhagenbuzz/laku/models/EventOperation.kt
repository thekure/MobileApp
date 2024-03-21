package dk.itu.moapd.copenhagenbuzz.laku.models
data class EventOperation(val operation: Operation, val events: List<Event>) {
    enum class Operation {
        CREATE,
        UPDATE,
        DELETE
    }
}