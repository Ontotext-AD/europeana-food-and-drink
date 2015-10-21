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
            $location.path('/app/search').search({query: '', limit: 24});
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
            $scope.categoriesLoader = true;
            $scope.searchData = $location.search();
            $scope.limit = $scope.searchData.limit ? parseInt($scope.searchData.limit) : 24;
            $scope.offset = $scope.searchData.offset ? parseInt($scope.searchData.offset) : 0;

            //Active Filters arrays
            $scope.activeFilters = [];
            $scope.activeArticles = [];
            $scope.activeCategories = [];

            $scope.categoryLoader = {};
            $scope.categories = {};
            $scope.tempGetCategoriesData = {};

            $scope.changeLimit = function(limit){
                $scope.limit = limit;
                $scope.offset = 0;
                $scope.searchData.limit = limit;
                $scope.searchData.offset = parseInt($scope.offset);
                $location.search($scope.searchData);
                $scope.search();
            }

            $scope.filtersCategories = [
                {id: 0, title: 'By media type', searchString: 'mediaType', data: [], isDisabled: true},
                {id: 1, title: 'By language of description', searchString: 'language', data: [], isDisabled: true},
                {id: 2, title: 'By providing country', searchString: 'providingCountry', data: [], isDisabled: true},
                {id: 3, title: 'By data provider', searchString: 'dataProvider', data: [], isDisabled: true}
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

            $scope.getCategories = function(category, categories, index, path){
                if (!category) {
                    $scope.categoriesLoader = true;
                } else {
                    $scope.categoryLoader[category] = true;
                    $scope.tempGetCategoriesData = {
                        category: category,
                        categories: categories,
                        index: index,
                        path: angular.copy(path)
                    }
                }
                var httpString = '/app/rest/categoryFacet';
                if(category) {
                    var string = category.split(' ').join('_');
                    httpString += '?category=' + string;
                }
                $http.get(httpString).
                    then(function(response) {
                        if (!category) {
                            $scope.categories = response.data;
                            $scope.categories.path = [];
                            $scope.categoriesLoader = false;
                        } else {
                            var parentCategory = $scope.tempGetCategoriesData.categories[$scope.tempGetCategoriesData.index];
                            parentCategory.subElements = response.data;
                            var path = $scope.tempGetCategoriesData.path;
                            path.push($scope.tempGetCategoriesData.category)
                            parentCategory.subElements.path = path;
                            $scope.categoryLoader[category] = false;
                        }
                        console.log(response.data);
                        localStorageService.set('categories', $scope.categories);
                    }, function(){
                        toastr.error('No info about categories', '');
                        $scope.categoriesLoader = false;
                        $scope.categoryLoader = {};
                    })
            }

            if (localStorageService.get('categories')) {
                $scope.categories = localStorageService.get('categories');
                $scope.categoriesLoader = false;
            } else {
                $scope.getCategories();
            }

            $scope.getCount = function(searchString){
                $http.get('/app/rest/search/count?' + searchString).
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

            $scope.setCheckState = function(data){
                var answer = false;
                if ($scope.activeFilters.length > 0) {
                    for(var i = 0; i < data.length; i++) {
                        for (var j = 0; j < $scope.activeFilters.length; j++) {
                            if (data[i].facetName == $scope.activeFilters[j].facetName){
                                data[i].checked = true;
                                answer = true;
                            }
                        }
                    }
                }
                return answer;
            }

            $scope.search = function(newSearch){
                $scope.countLoader = true;
                $scope.loader = true;
                if (newSearch) {
                    $scope.activeFilters = [];
                    $scope.offset = 0;
                    $scope.searchData.query = $scope.searchQuery;
                    $scope.searchData.limit = $scope.limit;
                    $scope.searchData.offset = $scope.offset;
                    $location.search($scope.searchData);
                }
                $scope.searchData = $location.search();
                $scope.setActiveFilters();
                $scope.setActiveArticles();
                $scope.setActiveCategories();
                var searchString = '';
                for(var index in $scope.searchData) {
                    if (searchString != '') {
                        searchString += '&';
                    }
                    searchString += index + '=' + $scope.searchData[index];
                }
                searchString = encodeURI(searchString);
                $http.get('/app/rest/search?' + searchString).
                    then(function(response) {
                        $scope.status = response.status;
                        $scope.data = response.data;
                        console.log($scope.data);
                        for(var i = 0; i < $scope.filtersCategories.length; i++){
                            if ($scope.data.facets[$scope.filtersCategories[i].searchString]){
                                $scope.filtersCategories[i].data = $scope.data.facets[$scope.filtersCategories[i].searchString];
                                for (var j = 0; j < $scope.filtersCategories[i].data.length; j++) {
                                    $scope.filtersCategories[i].data[j].id = j;
                                }
                                if ($scope.setCheckState($scope.filtersCategories[i].data)) {
                                    $scope.filtersCategories[i].open = true;
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
                        $scope.data = "";
                        $scope.status = response.status;
                        $scope.loader = false;
                        $scope.countLoader = false;
                        toastr.error('Request failed', '');
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

            $scope.setActiveArticles = function(){
                if ($scope.searchData.article){
                    var articles = $scope.searchData.article.split(',');
                    for (var i = 0; i < articles.length; i++) {
                        $scope.activeArticles.push(articles[i]);
                    }
                }
            }

            $scope.setActiveCategories = function(){
                if ($scope.searchData.category){
                    var categories = $scope.searchData.category.split(',');
                    for (var i = 0; i < categories.length; i++) {
                        $scope.activeCategories.push(categories[i]);
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

            $scope.addSearchArticle = function(article){
                var article = article.split(' ').join('_');
                if ($scope.searchData.article) {
                    var articles = $scope.searchData.article.split(',');
                    for (var i = 0; i < articles.length; i++) {
                        if (articles[i] == article) {
                            return;
                        }
                    }
                    articles.push(article);
                    $scope.searchData.article = articles.join(',');
                } else {
                    $scope.searchData.article = article;
                }
                $location.search($scope.searchData);
            }

            $scope.removeArticle = function(article){
                var article = article.split(' ').join('_'),
                    articles = $scope.searchData.article.split(',');
                if (articles.length == 1) {
                    delete $scope.searchData.article;
                    $location.search($scope.searchData);
                } else {
                    for (var i = 0; i < articles.length; i++) {
                        if (articles[i] == article) {
                            articles.splice(i,1);
                            $scope.searchData.article = articles.join(',');
                            $location.search($scope.searchData);
                            return;
                        }
                    }
                }
            }

            $scope.removeCategory = function(category){
                var category = category.split(' ').join('_'),
                    categories = $scope.searchData.category.split(',');
                if (categories.length == 1) {
                    delete $scope.searchData.category;
                    $location.search($scope.searchData);
                } else {
                    for (var i = 0; i < categories.length; i++) {
                        if (categories[i] == category) {
                            categories.splice(i,1);
                            $scope.searchData.category = categories.join(',');
                            $location.search($scope.searchData);
                            return;
                        }
                    }
                }
            }

            $scope.openCategory = function(category, path){
                var category  = category,
                    path = path,
                    categories = $scope.categories.categoryFacet;
                for (var i = 0; i < path.length; i++) {
                    for (var j = 0; j < categories.length; j++) {
                        if (path[i] == categories[j].facetName) {
                            categories = categories[j].subElements.categoryFacet;
                            break;
                        }
                    }
                }
                for (var i = 0; i < categories.length; i++) {
                    if (category == categories[i].facetName){
                        if (categories[i].open) {
                            categories[i].open = false;
                            localStorageService.set('categories', $scope.categories);
                        } else {
                            if (categories[i].subElements) {
                                categories[i].open = true;
                                localStorageService.set('categories', $scope.categories);
                            } else {
                                var index = i;
                                //TODO
                                categories[i].open = true;
                                $scope.getCategories(category, categories, index, path);
                            }
                        }
                        return;
                    }
                }
            }

            $scope.addCategory = function(category){
                var category = category.split(' ').join('_');
                if ($scope.searchData.category) {
                    var categories = $scope.searchData.category.split(',');
                    for (var i = 0; i < categories.length; i++) {
                        if (categories[i] == category) {
                            return;
                        }
                    }
                    categories.push(category);
                    $scope.searchData.category = categories.join(',');
                } else {
                    $scope.searchData.category = category;
                }
                $location.search($scope.searchData);
            }

            var changePageTimeout;
            $scope.changePage = function(){
                $timeout.cancel(changePageTimeout);
                changePageTimeout = $timeout(function(){
                    $scope.offset = $scope.limit * ($scope.results.page - 1);
                    $scope.searchData.offset = parseInt($scope.offset);
                    $location.search($scope.searchData);
                }, 2000);

                $scope.$on("$destroy", function(event) {
                    $timeout.cancel(changePageTimeout);
                });
            }

            $scope.loadResource = function(resource){
                $location.path('/app/resource/'+ encodeURIComponent(resource)).search($scope.searchData);
            }

            $scope.searchQuery = $scope.searchData.query;
            $scope.search();
        }
    ]);

    efdControllers.controller('ResourceCtrl',
        ["$scope",
        '$routeParams',
        '$http',
        '$location',
        '$timeout',
        'toastr',
        function($scope, $routeParams, $http, $location, $timeout, toastr) {

        $scope.loader = true;
        $scope.params = $routeParams;
        $scope.searchData = $location.search();
        $scope.searchQuery = $scope.searchData.query;
        $scope.resource = {};

        $scope.getResource = function(){
            $http.get('/app/rest/resource?uri=' + $scope.params.resourceId).
                then(function(response) {
                    console.log(response.data);
                    $scope.resource = response.data;
                    $scope.loader = false;
                }, function(response){
                    console.log(response);
                    toastr.error('Resource not found', '');
                    var t = $timeout(function(){
                        $location.path('/app/search').search($scope.searchData);
                    }, 2000);
                    $scope.$on("$destroy", function(event) {
                        $timeout.cancel(t);
                    });
                })
        }

        $scope.toSearchResults = function(){
            $location.path('/app/search').search($scope.searchData);
        }

        $scope.search = function(){
            $location.path('/app/search').search({query: $scope.searchQuery, limit: 24});
        }

        $scope.getResource();
    }]);

    return efdControllers;

});
