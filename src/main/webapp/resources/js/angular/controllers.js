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
        'toastr',
        function($scope, $routeParams, $http, $location, localStorageService ,toastr) {
            $scope.loader = true;
            $scope.countLoader = true;
            $scope.searchData = $location.search();
            $scope.limit = $scope.searchData.limit ? parseInt($scope.searchData.limit) : 24;
            $scope.offset = $scope.searchData.offset ? parseInt($scope.searchData.offset) : 0;
            $scope.activeFilters = [];

            $scope.changeLimit = function(limit){
                $scope.limit = limit;
                $scope.offset = 0;
                $scope.searchData.limit = limit;
                $scope.searchData.offset = $scope.offset;
                $location.search($scope.searchData);
                $scope.search();
            }


            $scope.filtersCategories = [
                {id: 0, title: 'By media type', searchString: 'mediaType', data: [], isDisabled: false},
                {id: 1, title: 'By language of description', searchString: 'language', data: [], isDisabled: false},
                {id: 2, title: 'By providing country', searchString: '', data: [], isDisabled: false},
                {id: 3, title: 'Can I use it?', searchString: '', data: [], isDisabled: false},
                {id: 4, title: 'By copyright', searchString: '', data: [], isDisabled: false},
                {id: 5, title: 'By provider', searchString: 'provider', data: [], isDisabled: false},
                {id: 6, title: 'By data provider', searchString: 'dataProvider', data: [], isDisabled: false}
            ]
            $scope.hasFacets = function(filter){
                return filter.data.length > 0;
            }


/*            $scope.$watch('exportFilter', function(newValue, oldValue) {
                $scope.filteredGraphs = filterFilter($scope.graphs, $scope.exportFilter);
                $scope.changePageSize();
            });*/

            $scope.params = $routeParams;
            $scope.test = 'ResultCtrl';

            $scope.getCount = function(searchString){
                $http.get('/rest/search/count?' + searchString).
                    then(function(response) {
                        $scope.count = response.data;
                        $scope.totalPages = Math.ceil($scope.count/$scope.limit);
                        $scope.page = $scope.offset/$scope.limit + 1;
                        $scope.countLoader = false;
                    }, function(){
                        $scope.count = 0;
                        toastr.error('No info about number of elements', '');
                        $scope.countLoader = false;
                    })
            }

            $scope.search = function(newSearch){
                $scope.countLoader = true;
                $scope.loader = true;
                if (newSearch) {
                    $scope.activeFilters = [];
                    $scope.searchData = {
                        query: $scope.searchQuery,
                        limit: $scope.limit
                    }
                    $location.search($scope.searchData);
                }
                $scope.searchData = $location.search();
                var searchString = '';
                for(index in $scope.searchData) {
                    if (searchString != '') {
                        searchString += '&';
                    }
                    searchString += index + '=' + $scope.searchData[index];
                }
                searchString = encodeURI(searchString);
                $http.get('/rest/search?' + searchString).
                    then(function(response) {
                        $scope.status = response.status;
                        $scope.data = response.data;
                        for(var i = 0; i < $scope.filtersCategories.length; i++){
                            if ($scope.data.facets[$scope.filtersCategories[i].searchString]){
                                $scope.filtersCategories[i].data = $scope.data.facets[$scope.filtersCategories[i].searchString];
                                for (var j = 0; j < $scope.filtersCategories[i].data.length; j++) {
                                    $scope.filtersCategories[i].data[j].id = j;
                                }
                                $scope.filtersCategories[i].isDisabled = false;
                            } else {
                                $scope.filtersCategories[i].data = [];
                                $scope.filtersCategories[i].isDisabled = true;
                            }
                        }
                        console.log($scope.filtersCategories);
                        $scope.loader = false;
                        $scope.getCount(searchString);
                    }, function(response) {
                        $scope.data = response.data || "Request failed";
                        $scope.status = response.status;
                        $scope.loader = false;
                    });
            }
            var t;
            $scope.addRemoveFilter = function(categoryIndex, facetIndex){
                debugger;
                var newFilter = {
                    catIndex: categoryIndex,
                    facetName: $scope.filtersCategories[categoryIndex].data[facetIndex].facetName,
                    facetValue: $scope.filtersCategories[categoryIndex].data[facetIndex].facetValue
                }
                $scope.activeFilters.push(newFilter);
                clearTimeout(t);
                t = setTimeout($scope.search(), 2000);
            }

            $scope.changePage = function(){
                $scope.offset = $scope.limit * ($scope.page - 1);
                $scope.searchData.offset = $scope.offset;
                $location.search($scope.searchData);
                $scope.search();
            }

            if($scope.searchData.query) {
                $scope.searchQuery = $scope.searchData.query;
                $scope.search();
            } else {
                $scope.loader = false;
            }
        }
    ]);

    efdControllers.controller('ResourceCtrl', ["$scope", '$routeParams', function($scope, $routeParams) {

        $scope.params = $routeParams;
        $scope.test = 'ResourceCtrl';
    }]);

    return efdControllers;

});
