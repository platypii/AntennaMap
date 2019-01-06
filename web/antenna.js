//
// Antenna Map
//
var googleMap = null;
var geoMarker = null;
var markers = {};
var infoWindow = null;
var querying = false;
var lastChange = null;
var lastQuery = null;
// Uses Algorithmia as a backend
var algorithmiaClient = Algorithmia.client("simAKghMFZNPxZjkZ4PoxbpuXUW1");
// icons[0] = 0..100m
// icons[1] = 1..200m
var icons = [
    "icons/A-16.png",
    "icons/A-20.png",
    "icons/A-24.png",
    "icons/A-28.png",
    "icons/A-32.png",
    "icons/A-36.png",
    "icons/A-40.png"
];
// Initialize map
function init() {
    var center = new google.maps.LatLng(39, -100);
    var zoom = 5;
    var geoZoom = 11;
    google.maps.event.addDomListener(window, "load", function () {
        var mapOptions = {
            center: center,
            mapTypeId: google.maps.MapTypeId.HYBRID,
            zoom: zoom
        };
        googleMap = new google.maps.Map(document.getElementById("map"), mapOptions);
        if (window.navigator.geolocation) {
            geoMarker = new GeolocationMarker();
            google.maps.event.addListenerOnce(geoMarker, "position_changed", function () {
                googleMap.setZoom(geoZoom);
                googleMap.setCenter(this.getPosition());
            });
            // google.maps.event.addListener(geoMarker, "geolocation_error", (e: any) => {
            //   console.error(`Error obtaining your position.\n\nMessage: ${e.message}`)
            // })
            geoMarker.setMap(googleMap);
        }
        // Add listeners
        googleMap.addListener("bounds_changed", boundsChanged);
        googleMap.addListener("click", closeMarkers);
    });
}
// Map moved, fetch new towers
function boundsChanged() {
    lastChange = new Date();
    // TODO: Only update every 100ms
    // Get bounds
    var bounds = googleMap.getBounds();
    if (bounds) {
        var ne = bounds.getNorthEast();
        var sw = bounds.getSouthWest();
        // Query for antennas
        if (!querying) {
            querying = true;
            lastQuery = lastChange;
            var antennas = query(sw.lat(), ne.lat(), sw.lng(), ne.lng(), function (antennas) {
                updateMap(antennas);
                querying = false;
                // Check if map has moved since we last queried
                if (lastQuery < lastChange) {
                    boundsChanged();
                }
            });
        }
    }
}
// Got new list of towers, add new towers and drop old towers
function updateMap(antennas) {
    var oldIds = Object.keys(markers);
    var newIds = [];
    for (var _i = 0, antennas_1 = antennas; _i < antennas_1.length; _i++) {
        var antenna = antennas_1[_i];
        newIds.push(JSON.stringify(antenna));
    }
    // Remove old markers
    for (var _a = 0, oldIds_1 = oldIds; _a < oldIds_1.length; _a++) {
        var id = oldIds_1[_a];
        if (newIds.indexOf(id) === -1) {
            var marker = markers[id];
            marker.setMap(null);
            delete markers[id];
        }
    }
    // Add new markers
    for (var _b = 0, antennas_2 = antennas; _b < antennas_2.length; _b++) {
        var antenna = antennas_2[_b];
        if (markers[JSON.stringify(antenna)] === undefined) {
            addMarker(antenna);
        }
    }
}
// Add a tower to the map
function addMarker(antenna) {
    var iconSize = Math.min(Math.floor(antenna.height / 100), 6);
    var alpha = ((antenna.height / 610) * 0.4) + 0.6;
    var marker = new google.maps.Marker({
        icon: icons[iconSize],
        map: googleMap,
        opacity: alpha,
        position: {
            lat: antenna.latitude,
            lng: antenna.longitude
        },
        title: meters2feet(antenna.height)
    });
    var content = "    <div>" + antenna.latitude.toFixed(6) + "," + antenna.longitude.toFixed(6) + "</div>\n    <div>" + meters2feet(antenna.height) + "</div>\n    <div><a href=\"https://maps.google.com/?q=" + antenna.latitude + "," + antenna.longitude + "&t=h\">Map</a>";
    if (antenna.url) {
        content += "\n    <div><a href=\"" + antenna.url + "\">Details</a><div>";
    }
    marker.addListener("click", function () {
        // Close other markers
        closeMarkers();
        // Open info window
        infoWindow = new google.maps.InfoWindow({ content: content });
        infoWindow.open(googleMap, marker);
    });
    markers[JSON.stringify(antenna)] = marker;
}
function closeMarkers() {
    if (infoWindow) {
        infoWindow.close();
        infoWindow = null;
    }
}
function meters2feet(m) {
    return Math.floor(3.28084 * m) + " ft";
}
// Query Algorithmia for towers within view bounds
function query(minLatitude, maxLatitude, minLongitude, maxLongitude, cb) {
    var windowSize = window.innerWidth * window.innerHeight;
    var lowerLimit = 16;
    var upperLimit = 50;
    var densityLimit = windowSize / 25000;
    var limit = Math.floor(Math.max(lowerLimit, Math.min(densityLimit, upperLimit)));
    if (googleMap.getZoom() <= 5) {
        limit += 15;
    }
    if (googleMap.getZoom() <= 7) {
        limit += 5;
    }
    var request = { minLatitude: minLatitude, maxLatitude: maxLatitude, minLongitude: minLongitude, maxLongitude: maxLongitude, limit: limit };
    // Query Algorithmia for antennas
    algorithmiaClient.algo("baseline/antennas").pipe(request).then(function (algorithmResult) {
        cb(algorithmResult.result);
    });
}
// Auto init
init();
