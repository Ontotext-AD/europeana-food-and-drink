

define(['angular'], function(){
    var factories = angular.module('efdApp.factories', []);

    factories.factory('mapFactory', mapFactory);

    function mapFactory() {
        var factory = {
            map: '',
            markerClusterer: '',
            markers: [],
            clearMarkers: clearMarkers,
        }

        return factory;

        function clearMarkers() {
            factory.markers = [];
            factory.markerClusterer.clearMarkers();
            factory.markerClusterer.setMap(null);
        }
    }

    return factories;
});
