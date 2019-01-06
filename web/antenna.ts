//
// Antenna Map
//

interface Antenna {
  latitude: number
  longitude: number
  height: number
  url: string
}

let googleMap: google.maps.Map = null
let geoMarker = null as any
const markers: {[key: string]: google.maps.Marker} = {}
let infoWindow: google.maps.InfoWindow = null
let querying = false
let lastChange: Date = null
let lastQuery: Date = null

// Uses Algorithmia as a backend
const algorithmiaClient = Algorithmia.client("simAKghMFZNPxZjkZ4PoxbpuXUW1")

// icons[0] = 0..100m
// icons[1] = 1..200m
const icons = [
  "icons/A-16.png",
  "icons/A-20.png",
  "icons/A-24.png",
  "icons/A-28.png",
  "icons/A-32.png",
  "icons/A-36.png",
  "icons/A-40.png"
]

// Initialize map
function init() {
  const center = new google.maps.LatLng(39, -100)
  const zoom = 5
  const geoZoom = 11
  google.maps.event.addDomListener(window, "load", () => {
    const mapOptions = {
      center,
      mapTypeId: google.maps.MapTypeId.HYBRID,
      zoom
    }
    googleMap = new google.maps.Map(document.getElementById("map"), mapOptions)

    if (window.navigator.geolocation) {
      geoMarker = new GeolocationMarker()
      google.maps.event.addListenerOnce(geoMarker, "position_changed", function(this: any) {
        googleMap.setZoom(geoZoom)
        googleMap.setCenter(this.getPosition())
      })
      // google.maps.event.addListener(geoMarker, "geolocation_error", (e: any) => {
      //   console.error(`Error obtaining your position.\n\nMessage: ${e.message}`)
      // })
      geoMarker.setMap(googleMap)
    }

    // Add listeners
    googleMap.addListener("bounds_changed", boundsChanged)
    googleMap.addListener("click", closeMarkers)
  })
}

// Map moved, fetch new towers
function boundsChanged() {
  lastChange = new Date()
  // TODO: Only update every 100ms

  // Get bounds
  const bounds = googleMap.getBounds()
  if (bounds) {
    const ne = bounds.getNorthEast()
    const sw = bounds.getSouthWest()

    // Query for antennas
    if (!querying) {
      querying = true
      lastQuery = lastChange
      const antennas = query(sw.lat(), ne.lat(), sw.lng(), ne.lng(), function(antennas) {
        updateMap(antennas)
        querying = false

        // Check if map has moved since we last queried
        if (lastQuery < lastChange) {
          boundsChanged()
        }
      })
    }
  }
}

// Got new list of towers, add new towers and drop old towers
function updateMap(antennas: Antenna[]) {
  const oldIds = Object.keys(markers)
  const newIds = []
  for (const antenna of antennas) {
    newIds.push(JSON.stringify(antenna))
  }

  // Remove old markers
  for (const id of oldIds) {
    if (newIds.indexOf(id) === -1) {
      const marker = markers[id]
      marker.setMap(null)
      delete markers[id]
    }
  }

  // Add new markers
  for (const antenna of antennas) {
    if (markers[JSON.stringify(antenna)] === undefined) {
      addMarker(antenna)
    }
  }
}

// Add a tower to the map
function addMarker(antenna: Antenna) {
  const iconSize = Math.min(Math.floor(antenna.height / 100), 6)
  const alpha = ((antenna.height / 610) * 0.4) + 0.6
  const marker = new google.maps.Marker({
    icon: icons[iconSize],
    map: googleMap,
    opacity: alpha,
    position: {
      lat: antenna.latitude,
      lng: antenna.longitude
    },
    title: meters2feet(antenna.height)
  })
  let content = `\
    <div>${antenna.latitude.toFixed(6)},${antenna.longitude.toFixed(6)}</div>
    <div>${meters2feet(antenna.height)}</div>
    <div><a href="https://maps.google.com/?q=${antenna.latitude},${antenna.longitude}&t=h">Map</a>`
  if (antenna.url) {
    content += `
    <div><a href="${antenna.url}">Details</a><div>`
  }
  marker.addListener("click", () => {
    // Close other markers
    closeMarkers()
    // Open info window
    infoWindow = new google.maps.InfoWindow({ content })
    infoWindow.open(googleMap, marker)
  })
  markers[JSON.stringify(antenna)] = marker
}

function closeMarkers() {
  if (infoWindow) {
    infoWindow.close()
    infoWindow = null
  }
}

function meters2feet(m: number): string {
  return Math.floor(3.28084 * m) + " ft"
}

// Query Algorithmia for towers within view bounds
function query(minLatitude: number, maxLatitude: number, minLongitude: number, maxLongitude: number, cb: Function) {
  const windowSize = window.innerWidth * window.innerHeight
  const lowerLimit = 16
  const upperLimit = 50
  const densityLimit = windowSize / 25000
  let limit = Math.floor(Math.max(lowerLimit, Math.min(densityLimit, upperLimit)))
  if (googleMap.getZoom() <= 5) { limit += 15 }
  if (googleMap.getZoom() <= 7) { limit += 5 }
  const request = { minLatitude, maxLatitude, minLongitude, maxLongitude, limit }

  // Query Algorithmia for antennas
  algorithmiaClient.algo("baseline/antennas").pipe(request).then((algorithmResult: any) => {
    cb(algorithmResult.result)
  })
}

// Auto init
init()
