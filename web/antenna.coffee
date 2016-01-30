###
Antenna Map
###


map = null
geoMarker = null
markers = {}
querying = false
lastChange = null
lastQuery = null

# Uses Algorithmia as a backend
algorithmiaClient = Algorithmia.client("simAKghMFZNPxZjkZ4PoxbpuXUW1")

# icons[0] = 0..100m
# icons[1] = 1..200m
icons = [
    "icons/A-16.png"
    "icons/A-20.png"
    "icons/A-24.png"
    "icons/A-28.png"
    "icons/A-32.png"
    "icons/A-36.png"
    "icons/A-40.png"
]

# Initialize map
init = ->
    center_lat = 39
    center_lon = -100
    zoom = 5
    geoZoom = 11
    google.maps.event.addDomListener window, 'load', ->
        mapOptions =
            zoom: zoom
            center: new google.maps.LatLng(center_lat, center_lon)
            mapTypeId: google.maps.MapTypeId.HYBRID
        map = new google.maps.Map(document.getElementById('map'), mapOptions)

        if navigator.geolocation
            geoMarker = new GeolocationMarker
            google.maps.event.addListenerOnce geoMarker, 'position_changed', ->
                map.setZoom geoZoom
                map.setCenter @getPosition()
                return
            google.maps.event.addListener geoMarker, 'geolocation_error', (e) ->
                console.error 'There was an error obtaining your position.\n\nMessage: ' + e.message
                return
            geoMarker.setMap map

        # Add listeners
        map.addListener 'bounds_changed', boundsChanged
        map.addListener 'click', closeAllMarkers
        return
    return

# Map moved, fetch new towers
boundsChanged = ->
    lastChange = new Date()
    # TODO: Only update every 100ms

    # Get bounds
    bounds = map.getBounds()
    if bounds
        ne = bounds.getNorthEast()
        sw = bounds.getSouthWest()

        # Query for antennas
        if not querying
            querying = true
            lastQuery = lastChange
            query sw.lat(), ne.lat(), sw.lng(), ne.lng(), (antennas) ->
                console.log "got " + antennas.length + " antennas"
                updateMap(antennas)
                querying = false

                # Check if map has moved since we last queried
                if lastQuery < lastChange
                    boundsChanged()
                return
    return

# Got new list of towers, add new towers and drop old towers
updateMap = (antennas) ->
    oldIds = Object.keys(markers)
    newIds = (antenna.id for antenna in antennas)

    # Remove old markers
    for id in oldIds
        if newIds.indexOf(id) == -1
            marker = markers[id]
            marker.setMap(null)
            delete markers[id]

    # Add new markers
    for antenna in antennas
        if markers[antenna.id] is undefined
            addMarker(antenna)
    return

# Add a tower to the map
addMarker = (antenna) ->
    url = "http://wireless2.fcc.gov/UlsApp/AsrSearch/asrRegistration.jsp?regKey=" + antenna.id
    iconSize = Math.floor(antenna.height / 100)
    alpha = (antenna.height / 610) * 0.4 + 0.6
    marker = new google.maps.Marker
        position:
            lat: antenna.latitude
            lng: antenna.longitude
        map: map
        title: meters2feet(antenna.height)
        icon: icons[iconSize]
        opacity: alpha
    marker.info = new google.maps.InfoWindow
        content:
            """
            <div>#{antenna.latitude.toFixed(6)},#{antenna.longitude.toFixed(6)}</div>
            <div>#{meters2feet(antenna.height)}</div>
            <div><a href="https://maps.google.com/?q=#{antenna.latitude},#{antenna.longitude}&t=h">Map</a>
            <div><a href="#{url}">Details</a><div>
            """
    marker.info.isOpen = false
    marker.addListener 'click', ->
        shouldOpen = not marker.info.isOpen
        # Close all other markers
        closeAllMarkers()
        # Open info window
        if shouldOpen
            marker.info.open(map, marker)
            marker.info.isOpen = true
        return
    markers[antenna.id] = marker
    return

closeAllMarkers = ->
    for id, marker of markers
        if marker.info.isOpen
            marker.info.close()
            marker.info.isOpen = false
    return

meters2feet = (m) -> Math.floor(3.28084 * m) + "ft"

# Query Algorithmia for towers within view bounds
query = (minLatitude, maxLatitude, minLongitude, maxLongitude, cb) ->
    windowSize = window.innerWidth * window.innerHeight
    lowerLimit = 16
    upperLimit = 50
    densityLimit = windowSize / 25000
    limit = Math.floor(Math.max(lowerLimit, Math.min(densityLimit, upperLimit)))
    if map.getZoom() <= 5 then limit += 15
    if map.getZoom() <= 7 then limit += 5
    request =
        minLatitude: minLatitude
        maxLatitude: maxLatitude
        minLongitude: minLongitude
        maxLongitude: maxLongitude
        limit: limit
    # Query Algorithmia for antennas
    algorithmiaClient.algo("baseline/antennas").pipe(request).then (algorithmResult) ->
        cb(algorithmResult.result)
        return
    return

# Initialize on load
init()
