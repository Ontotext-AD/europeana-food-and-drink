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
        '$timeout',
        'localStorageService',
        'toastr',
        function($scope, $routeParams, $http, $location, $timeout, localStorageService ,toastr) {
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
                {id: 0, title: 'By media type', searchString: 'mediaType', data: [], isDisabled: true},
                {id: 1, title: 'By language of description', searchString: 'language', data: [], isDisabled: true},
                {id: 2, title: 'By providing country', searchString: '', data: [], isDisabled: true},
                {id: 3, title: 'Can I use it?', searchString: '', data: [], isDisabled: true},
                {id: 4, title: 'By copyright', searchString: '', data: [], isDisabled: true},
                {id: 5, title: 'By provider', searchString: 'provider', data: [], isDisabled: true},
                {id: 6, title: 'By data provider', searchString: 'dataProvider', data: [], isDisabled: true}
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
                        $scope.results = {page: $scope.offset/$scope.limit + 1};
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
                    $scope.offset = 0;
                    $scope.searchData = {
                        query: $scope.searchQuery,
                        limit: $scope.limit,
                        offset: $scope.offset
                    }
                    $location.search($scope.searchData);
                }
                $scope.searchData = $location.search();
                $scope.setActiveFilters();
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
                        $scope.loader = false;
                        $scope.getCount(searchString);
                    }, function(response) {
                        $scope.data = response.data || "Request failed";
                        $scope.status = response.status;
                        $scope.loader = false;
                    });
            }

            $scope.setActiveFilters = function(){
                for (index in $scope.searchData) {
                    var exist = false;
                    for (var i = 0; i < $scope.filtersCategories.length; i++){
                        if (index == $scope.filtersCategories[i].searchString) {
                            exist = true;
                        }
                    }
                    if (exist){

                        var filterArr = $scope.searchData[index].split(',');

                        for (var i = 0; i < filterArr.length; i++){
                            var newFilter = {
                                categoryName: index,
                                facetName: filterArr[i]
                            }
                            $scope.activeFilters.push(newFilter);
                        }
                    }
                }
            }

            //Remove filter only from the Selected filters
            $scope.removeFilter = function(categoryName, facetName){
                if ($scope.searchData[categoryName]){
                    var filterArr = $scope.searchData[categoryName].split(',');
                    for (var i = 0; i < filterArr.length; i++){
                        if (filterArr[i] == facetName){
                            filterArr.splice(i, 1);
                        }
                    }
                    if (filterArr.length == 0){
                        delete $scope.searchData[categoryName];
                    } else {
                        $scope.searchData[categoryName] = filterArr.join(',');
                    }
                    $location.search($scope.searchData);
                }
            }

            //Add/Remove filter from facets categories menu
            var addRemoveFilterTimeout;
            $scope.addRemoveFilter = function(categoryIndex, facetIndex){
                $timeout.cancel(addRemoveFilterTimeout);
                if ($scope.filtersCategories[categoryIndex].data[facetIndex].checked === false) {
                    $scope.removeFilter($scope.filtersCategories[categoryIndex].searchString, $scope.filtersCategories[categoryIndex].data[facetIndex].facetName);
                    return;
                }

                var newFilter = {
                    categoryName: $scope.filtersCategories[categoryIndex].searchString,
                    facetName: $scope.filtersCategories[categoryIndex].data[facetIndex].facetName
                }
                $scope.activeFilters.push(newFilter);
                if(angular.isUndefined($scope.searchData[$scope.filtersCategories[categoryIndex].searchString])){
                    $scope.searchData[$scope.filtersCategories[categoryIndex].searchString] = newFilter.facetName;
                } else {
                    var temp = $scope.searchData[$scope.filtersCategories[categoryIndex].searchString].split(',');
                    temp.push(newFilter.facetName);
                    $scope.searchData[$scope.filtersCategories[categoryIndex].searchString] = temp.join(",");
                }

                $scope.searchData.offset = 0;
                addRemoveFilterTimeout = $timeout(function(){
                    $location.search($scope.searchData);
                }, 1000);

                $scope.$on("$destroy", function(event) {
                    $timeout.cancel(addRemoveFilterTimeout);
                });
            }

            var changePageTimeout;
            $scope.changePage = function(){
                $timeout.cancel(changePageTimeout);
                changePageTimeout = $timeout(function(){
                    $scope.offset = $scope.limit * ($scope.results.page - 1);
                    $scope.searchData.offset = $scope.offset;
                    $location.search($scope.searchData);
                }, 2000);

                $scope.$on("$destroy", function(event) {
                    $timeout.cancel(changePageTimeout);
                });
            }

            $scope.loadResource = function(resource){
                $location.path('/resource/'+ encodeURIComponent(resource)).search($scope.searchData);
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
