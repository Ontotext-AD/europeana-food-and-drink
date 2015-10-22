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

            $scope.searchData = $location.search();
            $scope.limit = $scope.searchData.limit ? parseInt($scope.searchData.limit) : 24;
            $scope.offset = $scope.searchData.offset ? parseInt($scope.searchData.offset) : 0;
            $scope.panelsSettings = {};
            //Active Filters arrays
            $scope.activeFilters = [];
            $scope.activeArticles = [];
            $scope.activeCategories = [];

            //Loaders
            $scope.loader = true; //Main loader
            $scope.countLoader = true; //Loader for results per page + Pagination
            $scope.categoriesLoader = true; //Loader for Categories panel

            //Categories obj
            $scope.categories = {};
            //Temp obj used to store data about clicked category when wait for API response for subCategories
            $scope.tempGetCategoriesData = {};

            //Facets obj template
            $scope.filtersCategories = [
                {id: 0, title: 'Media type', searchString: 'mediaType', data: [], isDisabled: true},
                {id: 1, title: 'Language of description', searchString: 'language', data: [], isDisabled: true},
                {id: 2, title: 'Providing country', searchString: 'providingCountry', data: [], isDisabled: true},
                {id: 3, title: 'Data provider', searchString: 'dataProvider', data: [], isDisabled: true}
            ]

            //Change number of results on page
            $scope.changeLimit = function(limit){
                $scope.limit = limit;
                $scope.offset = 0;
                $scope.searchData.limit = limit;
                $scope.searchData.offset = parseInt($scope.offset);
                $location.search($scope.searchData);
                $scope.search();
            }

            $scope.hasFacets = function(filter){
                return filter.data.length > 0;
            }

            //If no attr get the first level categoies and articles, otherwise get subCategories of category
            $scope.getCategories = function(category, categories, index, path){
                if (!category) {
                    //First load or new query search
                    $scope.categoriesLoader = true;
                } else {
                    //Set temp data to use it after API response
                    $scope.tempGetCategoriesData = {
                        category: category,
                        categories: categories,
                        index: index,
                        path: angular.copy(path)
                    }
                }
                //Setup API url
                var httpString = '/app/rest/categoryFacet?query=' + $scope.searchQuery;
                if(category) {
                    var string = category.split(' ').join('_');
                    httpString += '&category=' + string;
                }

                $http.get(httpString).
                    then(function(response) {
                        if (!category) {
                            //First load
                            $scope.categories = response.data;
                            $scope.categories.path = [];
                            $scope.categoriesLoader = false;
                        } else {
                            //receive subCategories
                            var parentCategory = $scope.tempGetCategoriesData.categories[$scope.tempGetCategoriesData.index];
                            parentCategory.subElements = response.data;
                            var path = $scope.tempGetCategoriesData.path;
                            path.push($scope.tempGetCategoriesData.category)
                            parentCategory.subElements.path = path;
                        }
                        //Add new data to sessionStorage
                        localStorageService.set('categories', $scope.categories);
                    }, function(){
                        if (category) {
                            $scope.tempGetCategoriesData.categories[$scope.tempGetCategoriesData.index].open = false;
                        }
                        toastr.error('No info about categories', '');
                        $scope.categoriesLoader = false;
                    })
            }

            //Get data about number of results - also it's used for calculate number of pages
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

            //Set state of Facets checkboxes based on facets used in page URL
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

            //Search results
            $scope.search = function(newSearch){
                $scope.countLoader = true;
                $scope.loader = true;

                //On new search always go to first page
                if (newSearch) {
                    $scope.activeFilters = [];
                    $scope.offset = 0;
                    $scope.searchData.query = $scope.searchQuery;
                    $scope.searchData.limit = $scope.limit;
                    $scope.searchData.offset = parseInt($scope.offset);

                    //Clear categories so you'll get it again based on new query
                    $scope.categories = {};
                    localStorageService.remove('categories');

                    $location.search($scope.searchData);
                }

                $scope.searchData = $location.search();

                //Create arrays for use in "Active filters" panel
                $scope.setActiveFilters();
                $scope.setActiveArticles();
                $scope.setActiveCategories();

                //Create search URL
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
                        if ($scope.data) {
                            //Fill facets template obj with current facets
                            for(var i = 0; i < $scope.filtersCategories.length; i++){
                                if ($scope.data.facets[$scope.filtersCategories[i].searchString]){
                                    $scope.filtersCategories[i].data = $scope.data.facets[$scope.filtersCategories[i].searchString];
                                    for (var j = 0; j < $scope.filtersCategories[i].data.length; j++) {
                                        $scope.filtersCategories[i].data[j].id = j;
                                    }
                                    //Set Facets' categories state (open/close) based on checked and unchecked facets
                                    if ($scope.setCheckState($scope.filtersCategories[i].data)) {
                                        $scope.filtersCategories[i].open = true;
                                    }
                                    $scope.filtersCategories[i].isDisabled = false;
                                } else {
                                    $scope.filtersCategories[i].data = [];
                                    $scope.filtersCategories[i].isDisabled = true;
                                }
                            }
                        }
                        $scope.loader = false;

                        //Get number of all resultss
                        $scope.getCount(searchString);
                    }, function(response) {
                        $scope.data = "";
                        $scope.status = response.status;
                        $scope.loader = false;
                        $scope.countLoader = false;
                        toastr.error('Request failed', '');
                    });
            }

            //Set active filters to array for use in Active filters panel
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

            //Set active articles to array for use in Active filters panel
            $scope.setActiveArticles = function(){
                if ($scope.searchData.article){
                    var articles = $scope.searchData.article.split(',');
                    for (var i = 0; i < articles.length; i++) {
                        $scope.activeArticles.push(decodeURIComponent(articles[i]).split('_').join(' '));
                    }
                }
            }

            //Set active categories to array for use in Active filters panel
            $scope.setActiveCategories = function(){
                if ($scope.searchData.category){
                    var categories = $scope.searchData.category.split(',');
                    for (var i = 0; i < categories.length; i++) {
                        $scope.activeCategories.push(decodeURIComponent(categories[i]).split('_').join(' '));
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

            //Click on article to add it to search filters
            $scope.addSearchArticle = function(article){
                var article = encodeURIComponent(article.split(' ').join('_'));
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

            //Remove article from search filters
            $scope.removeArticle = function(article){
                var article = encodeURIComponent(article.split(' ').join('_')),
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

            //Click on Category to add it to search filters
            $scope.addCategory = function(category){
                var category = encodeURIComponent(category.split(' ').join('_'));
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

            //Remove category from search filters
            $scope.removeCategory = function(category){
                var category = encodeURIComponent(category.split(' ').join('_')),
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

            //Open/Close category // Show/Hide Subcategories and articles
            $scope.openCategory = function(category, path, clickEvent){
                clickEvent.stopImmediatePropagation();
                var category  = category,
                    path = path,
                    categories = $scope.categories.categoryFacet;

                //Get current level of categories
                for (var i = 0; i < path.length; i++) {
                    for (var j = 0; j < categories.length; j++) {
                        if (path[i] == categories[j].facetName) {
                            categories = categories[j].subElements.categoryFacet;
                            break;
                        }
                    }
                }


                for (var i = 0; i < categories.length; i++) {
                    //Find current category
                    if (category == categories[i].facetName){
                        if (categories[i].open) {
                            //Close category
                            categories[i].open = false;
                            localStorageService.set('categories', $scope.categories);
                        } else {
                            //Open category
                            if (categories[i].subElements) {
                                //Subcategories are loaded from API before - only change open/close state
                                categories[i].open = true;
                                localStorageService.set('categories', $scope.categories);
                            } else {
                                //Load subcategories from API
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

            //Change result page
            var changePageTimeout;
            $scope.changePage = function(number){
                if (number && $scope.results.page){
                    if (number > 0 && $scope.results.page < $scope.totalPages) {
                        $scope.results.page++;
                    } else if (number < 0 && $scope.results.page > 1) {
                        $scope.results.page--;
                    }
                }
                //Cancel previous timeout to wait for user to choose page
                $timeout.cancel(changePageTimeout);
                //Wait 2 seconds before change page (offset) in URL to give time to user to add more digits or click several times on page selector
                changePageTimeout = $timeout(function(){
                    if ($scope.results.page) {
                        $scope.offset = $scope.limit * ($scope.results.page - 1);
                        $scope.searchData.offset = parseInt($scope.offset);
                        $location.search($scope.searchData);
                    }
                }, 1000);

                $scope.$on("$destroy", function(event) {
                    $timeout.cancel(changePageTimeout);
                });
            }

            //Go to Resource page
            $scope.loadResource = function(resource){
                $location.path('/app/resource/'+ encodeURIComponent(resource)).search($scope.searchData);
            }

            //First load
            $scope.searchQuery = $scope.searchData.query;
            $scope.search();

            //Load categories
            if (localStorageService.get('categories')) {
                //Reload page
                $scope.categories = localStorageService.get('categories');
                $scope.categoriesLoader = false;
            } else {
                //First load
                $scope.getCategories();
            }

            //Load panelSettings (is panel is open or closed)
            if (localStorageService.get('panelSettings')) {
                //Reload page
                $scope.panelSettings = localStorageService.get('panelSettings');
            } else {
                //First load
                $scope.panelSettings = {
                    foodAndDrink: true
                }
                localStorageService.set('panelSettings', $scope.panelSettings);
            }

            $scope.toggleOpenFood = function(){
                $scope.panelSettings.foodAndDrink = !$scope.panelSettings.foodAndDrink;
                localStorageService.set('panelSettings', $scope.panelSettings);

            }
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
