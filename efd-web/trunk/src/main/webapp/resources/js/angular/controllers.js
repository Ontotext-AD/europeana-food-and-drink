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

    efdControllers.controller('HomeCtrl',[
        "$scope",
        '$location',
        function($scope, $location) {

            $scope.test = 'SearchCtrl';
            $scope.search = function(){
                $location.path('/search').search({query: $scope.searchQuery, limit: 24});
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
            $scope.limit = $scope.searchData.limit ? $scope.searchData.limit : 24;
            $scope.changeLimit = function(limit){
                $scope.limit = limit;
                $scope.searchData.limit = limit
                $location.search($scope.searchData);
                $scope.search();
            }
            $scope.filtersCategories = [
                {title: 'By media type', searchString: '', data: [], isDisabled: false},
                {title: 'By language of description', searchString: 'language', data: [], isDisabled: false},
                {title: 'By providing country', searchString: '', data: [], isDisabled: false},
                {title: 'Can I use it?', searchString: '', data: [], isDisabled: false},
                {title: 'By copyright', searchString: '', data: [], isDisabled: false},
                {title: 'By provider', searchString: 'provider', data: [], isDisabled: false},
                {title: 'By data provider', searchString: 'dataProvider', data: [], isDisabled: false}
            ]
            $scope.hasFacets = function(filter){
                return filter.data.length > 0;
            }
            $scope.params = $routeParams;
            $scope.test = 'ResultCtrl';
            $scope.search = function(){
                var searchData = $location.search(),
                    searchString = '';
                for(index in searchData) {
                    if (searchString != '') {
                        searchString += '&';
                    }
                    searchString += index + '=' + searchData[index];
                }
                searchString = encodeURI(searchString);
                $http.get('/rest/search?' + searchString).
                    then(function(response) {
                        $scope.status = response.status;
                        $scope.data = response.data;
                        for(var i = 0; i < $scope.filtersCategories.length; i++){
                            if ($scope.data.facets[$scope.filtersCategories[i].searchString]){
                                $scope.filtersCategories[i].data = $scope.data.facets[$scope.filtersCategories[i].searchString];
                                $scope.filtersCategories[i].isDisabled = false;
                            } else {
                                $scope.filtersCategories[i].data = [];
                                $scope.filtersCategories[i].isDisabled = true;
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
