package dk.itu.moapd.copenhagenbuzz.laku

enum class EventType {
    BIRTHDAY,
    WEDDING,
    CONFERENCE
}
data class Event(
    var eventName: String,
    var eventLocation: String,
    var eventDate: String,
    var eventType: EventType,
    var eventDescription: String
) {
    override fun toString(): String {
        return "Event (eventName = ’$eventName’, " +
                "eventLocation = ’$eventLocation’) " +
                "eventDate = ’$eventDate’) " +
                "eventType = ’$eventType’) " +
                "eventDescription = ’$eventDescription’)"
    }
}