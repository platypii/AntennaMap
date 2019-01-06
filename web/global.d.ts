declare namespace google.maps {
  class Map {
    constructor(mapDiv: Element|null, opts?: any)
    addListener(event: string, f: Function)
    getBounds()
    getZoom()
    setCenter(pos: any)
    setZoom(zoom: number)
  }
  class Marker {
    constructor(opts: any)
    addListener(event: string, f: Function)
    setMap(map: Map)
  }
  class InfoWindow {
    constructor(opts: any)
    close()
    open(map: Map, marker: Marker)
  }
  class LatLng {
    constructor(lat, lon)
  }
  class event {
    static addDomListener(instance: Object, eventName: string, handler: Function, capture?: boolean)
    static addListenerOnce(instance: Object, eventName: string, handler: Function, capture?: boolean)
  }
  enum MapTypeId {
    /** This map type displays a transparent layer of major streets on satellite images. */
    HYBRID,
    /** This map type displays a normal street map. */
    ROADMAP,
    /** This map type displays satellite images. */
    SATELLITE,
    /** This map type displays maps with physical features such as terrain and vegetation. */
    TERRAIN
  }
}
declare var Algorithmia
declare var GeolocationMarker
