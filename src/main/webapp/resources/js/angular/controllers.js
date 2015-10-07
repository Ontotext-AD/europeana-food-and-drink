/**
 * Created by Rostislav on 23-Sep-15.
 */


define(['angular'], function(){
    var efdControllers = angular.module('efdApp.controllers', [
        'ngRoute'
    ]);

    efdControllers.controller('MainCtrl', ["$scope",  function($scope) {

        $scope.test = 'MainCtrl';
    }]);

    efdControllers.controller('SearchCtrl',[
        "$scope",
        "$rootScope",
        '$http',
        '$location',
        function($scope, $rootScope, $http, $location) {

            $scope.test = 'SearchCtrl';
            $scope.search = function(){
                $location.path('/search').search({query: $scope.searchQuery});
            }
        }
    ]);

    efdControllers.controller('ResultCtrl', [
        "$scope",
        '$routeParams',
        '$http',
        '$location',
        'localStorageService',
        function($scope, $routeParams, $http, $location, localStorageService) {
            $scope.searchData = $location.search();
            console.log($scope.searchData);

            $scope.filtersCategories = [
                {title: 'By media type', searchString: '', data: []},
                {title: 'By language of description', searchString: 'language', data: []},
                {title: 'By providing country', searchString: '', data: []},
                {title: 'Can I use it?', searchString: '', data: []},
                {title: 'By copyright', searchString: '', data: []},
                {title: 'By provider', searchString: 'provider', data: []},
                {title: 'By data provider', searchString: 'dataProvider', data: []}
            ]
            $scope.hasFacets = function(filter){
                return filter.data.length > 0;
            }
            $scope.params = $routeParams;
            $scope.test = 'ResultCtrl';
            $scope.search = function(){
                $http.get('/rest/search?query=' + $scope.searchQuery).
                    then(function(response) {
                        $scope.status = response.status;
                        $scope.data = response.data;
                        for(var i = 0; i < $scope.filtersCategories.length; i++){
                            if ($scope.data.facets[$scope.filtersCategories[i].searchString]){
                                $scope.filtersCategories[i].data = $scope.data.facets[$scope.filtersCategories[i].searchString];
                            } else {
                                $scope.filtersCategories[i].data = [];
                            }
                        }
                        console.log($scope.filtersCategories);
                    }, function(response) {
                        $scope.data = response.data || "Request failed";
                        $scope.status = response.status;
                    });
            }
            if($scope.searchData.query) {
                $scope.searchQuery = $scope.searchData.query;
                $scope.search();
            }
        }
    ]);

    efdControllers.controller('ResourceCtrl', ["$scope", '$routeParams', function($scope, $routeParams) {

        $scope.params = $routeParams;
        $scope.test = 'ResourceCtrl';
    }]);

    return efdControllers;

});