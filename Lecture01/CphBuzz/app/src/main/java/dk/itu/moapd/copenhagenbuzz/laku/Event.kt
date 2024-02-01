package dk.itu.moapd.copenhagenbuzz.laku

class Event(
    private var eventName: String,
    private var eventLocation: String,
    private var eventDate: String,
    private var eventType: String,
    private var eventDescription: String
) {


    fun getEventName(): String {
        return eventName
    }

    fun setEventName(eventName: String) {
        this.eventName = eventName
    }

    fun getEventLocation(): String {
        return eventLocation
    }

    fun setEventLocation(eventLocation: String) {
        this.eventLocation = eventLocation
    }

    fun getEventDate(): String {
        return eventDate
    }

    fun setEventDate(eventDate: String) {
        this.eventDate = eventDate
    }

    fun getEventType(): String {
        return eventType
    }

    fun setEventType(eventType: String) {
        this.eventType = eventType
    }

    fun getEventDescription(): String {
        return eventDescription
    }

    fun setEventDescription(eventDescription: String) {
        this.eventDescription = eventDescription
    }
    //  Event ( eventName = ’Something’,eventLocation = ’Copenhagen’)eventDate = ’Tue, Jan 02 2024 - Wed, Jan 10 2024’)eventType = ’Wedding’)eventDescription = ’Something more.’)
    override fun toString(): String {
        return "Event (eventName = ’$eventName’, " +
                "eventLocation = ’$eventLocation’) " +
                "eventDate = ’$eventDate’) " +
                "eventType = ’$eventType’) " +
                "eventDescription = ’$eventDescription’)"
    }
}