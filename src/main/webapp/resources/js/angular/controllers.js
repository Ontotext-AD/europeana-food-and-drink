/**
 * Created by Rostislav on 23-Sep-15.
 */

define(['angular'], function() {
    var efdControllers = angular.module('efdApp.controllers', [
        'ngRoute'
    ]);

    efdControllers.controller('MainCtrl', ["$scope", function($scope) {

        $scope.test = 'MainCtrl';
    }]);

    efdControllers.controller('HomeCtrl', [
        "$scope",
        '$location',
        function($scope, $location) {
            $location.path('/app/search').search({ query: '', limit: 24 });
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
        'NgMap',
        'mapFactory',
        function($scope, $routeParams, $http, $location, $timeout, localStorageService, toastr, NgMap, mapFactory) {

            localStorageService.remove('categories');
            localStorageService.remove('places');

            $scope.searchData = $location.search();
            $scope.limit = $scope.searchData.limit ? parseInt($scope.searchData.limit) : 24;
            $scope.offset = $scope.searchData.offset ? parseInt($scope.searchData.offset) : 0;
            $scope.panelsSettings = {};
            //Active Filters arrays
            $scope.activeFilters = [];
            $scope.activeArticles = [];
            $scope.activeCategories = [];
            $scope.activePlaces = [];

            //Loaders
            $scope.loader = true; //Main loader
            $scope.countLoader = true; //Loader for results per page + Pagination
            $scope.categoriesLoader = true; //Loader for Categories panel
            $scope.placesLoader = true; //Loader for Categories panel

            //Categories obj
            $scope.categories = {};
            //Temp obj used to store data about clicked category when wait for API response for subCategories
            $scope.tempGetCategoriesData = {};

            //Places obj
            $scope.places = {};
            //Temp obj used to store data about clicked place when wait for API response for subPlaces
            $scope.tempGetPlacesData = {};

            //Map to use it for active facets prefixes
            $scope.categoriesNamesMap = {
                mediaType: 'Type (resource)',
                language: 'Language',
                dataProvider: 'Data provider'
            }

            //Facets obj template
            $scope.filtersCategories = [
                { id: 0, title: 'Type (resource)', searchString: 'mediaType', data: [], isDisabled: true },
                { id: 1, title: 'Language', searchString: 'language', data: [], isDisabled: true },
                { id: 2, title: 'Data provider', searchString: 'dataProvider', data: [], isDisabled: true }
            ]

            //Change number of results on page
            $scope.changeLimit = function(limit) {
                $scope.limit = limit;
                $scope.offset = 0;
                $scope.searchData.limit = limit;
                $scope.searchData.offset = parseInt($scope.offset);
                $location.search($scope.searchData);
                $scope.search();
            }

            $scope.hasFacets = function(filter) {
                return filter.data.length > 0;
            }

            $scope.createSearchString = function(map) {
                //Create search URL
                var searchString = '';
                for (var index in $scope.searchData) {
                    if (map) {
                        if (index != 'limit' && index != 'offset') {
                            if (searchString != '') {
                                searchString += '&';
                            }
                            searchString += index + '=' + $scope.searchData[index];
                        }
                    } else {
                        if (searchString != '') {
                            searchString += '&';
                        }
                        searchString += index + '=' + $scope.searchData[index];
                    }
                }
                searchString = encodeURI(searchString);
                return searchString;
            }

            //If no attr get the first level categoies and articles, otherwise get subCategories of category
            $scope.getCategories = function(category, categories, index, path) {
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
                var httpString = '/app/rest/categoryFacet?';

                //Create search string
                var searchString = $scope.createSearchString();
                if (searchString) {
                    httpString += searchString;
                }

                if (category) {
                    var string = category.split(' ').join('_');
                    httpString += '&subCategories=' + string;
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
                }, function() {
                    if (category) {
                        $scope.tempGetCategoriesData.categories[$scope.tempGetCategoriesData.index].open = false;
                    }
                    toastr.error('No info about categories', '');
                    $scope.categoriesLoader = false;
                })
            }

            //If no attr get the first level categoies and articles, otherwise get subCategories of category
            $scope.getPlaces = function(place, places, index, path) {
                if (!places) {
                    //First load or new query search
                    $scope.placesLoader = true;
                } else {
                    //Set temp data to use it after API response
                    $scope.tempGetPlacesData = {
                        place: place,
                        places: places,
                        index: index,
                        path: angular.copy(path)
                    }
                }
                //Setup API url
                var httpString = '/app/rest/placesFacet?';

                //Create search string
                var searchString = $scope.createSearchString();
                if (searchString) {
                    httpString += searchString;
                }

                if (place) {
                    var string = place.split(' ').join('_');
                    httpString += '&subPlace=' + string;
                }

                $http.get(httpString).
                then(function(response) {
                    //console.log(response.data)
                    if (!place) {
                        //First load
                        $scope.places = response.data;
                        $scope.places.path = [];
                        $scope.placesLoader = false;
                    } else {
                        //receive subCategories
                        var parentPlace = $scope.tempGetPlacesData.places[$scope.tempGetPlacesData.index];
                        parentPlace.subElements = response.data;
                        var path = $scope.tempGetPlacesData.path;
                        path.push($scope.tempGetPlacesData.place)
                        parentPlace.subElements.path = path;
                    }
                    //Add new data to sessionStorage
                    localStorageService.set('places', $scope.places);
                }, function() {
                    if (category) {
                        $scope.tempGetPlacesData.places[$scope.tempGetPlacesData.index].open = false;
                    }
                    toastr.error('No info about places', '');
                    $scope.placesLoader = false;
                })
            }

            //Get data about number of results - also it's used for calculate number of pages
            $scope.getCount = function(searchString) {
                $http.get('/app/rest/search/count?' + searchString).
                then(function(response) {
                    $scope.count = response.data;
                    $scope.totalPages = Math.ceil($scope.count / $scope.limit);
                    $scope.results = { page: $scope.offset / $scope.limit + 1 };
                    $scope.countLoader = false;
                }, function() {
                    $scope.count = 0;
                    toastr.error('No info about number of elements', '');
                    $scope.countLoader = false;
                })
            }

            //Set state of Facets checkboxes based on facets used in page URL
            $scope.setCheckState = function(data) {
                var answer = false;
                if ($scope.activeFilters.length > 0) {
                    for (var i = 0; i < data.length; i++) {
                        for (var j = 0; j < $scope.activeFilters.length; j++) {
                            if (data[i].facetName == $scope.activeFilters[j].facetName) {
                                data[i].checked = true;
                                answer = true;
                            }
                        }
                    }
                }
                return answer;
            }

            //Search results
            $scope.search = function(newSearch) {
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

                    $location.search($scope.searchData);
                }

                $scope.searchData = $location.search();

                //Create arrays for use in "Active filters" panel
                $scope.setActiveFilters();
                $scope.setActiveArticles();
                $scope.setActiveCategories();
                $scope.setActivePlaces();

                //Create search string
                var searchString = $scope.createSearchString();
                $scope.googleMapsUrl = "https://maps.googleapis.com/maps/api/js?key=AIzaSyDVmtxpD_M2Y3mAEOYcx_Xbld_ywPqfyI8";
                if (mapFactory.map) {
                    $scope.getLocations();
                }
                $http.get('/app/rest/search?' + searchString).
                then(function(response) {
                    $scope.status = response.status;
                    $scope.data = response.data;
                    if ($scope.data) {
                        //Fill facets template obj with current facets
                        for (var i = 0; i < $scope.filtersCategories.length; i++) {
                            if ($scope.data.facets[$scope.filtersCategories[i].searchString]) {
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

            if (!mapFactory.map) {
                NgMap.getMap().then(function(map) {
                    mapFactory.map = map;
                    $scope.getLocations();
                });
            }

            $scope.getLocations = function() {
                $scope.showMapButton = false;
                $scope.showMap = false;
                var searchString = $scope.createSearchString(true);
                $http.get('/app/rest/search/locations?' + searchString)
                    .then(function(response) {
                        if (mapFactory.markerClusterer) {
                            mapFactory.clearMarkers();
                        }

                        var results = response.data.searchResults;
                        for (var i in results) {
                            if (results[i].lat && results[i].longitude) {
                                var latLng = new google.maps.LatLng(results[i].lat, results[i].longitude),
                                    marker = new google.maps.Marker({ position: latLng, data: results[i] }),
                                    data = results[i];
                                google.maps.event.addListener(marker, 'click', function(event) {
                                    $scope.infoWinData = this.data;
                                    mapFactory.map.showInfoWindow('infoWindow', this);
                                })
                                mapFactory.markers.push(marker);
                            }
                        };

                        mapFactory.markerClusterer = new MarkerClusterer(mapFactory.map, mapFactory.markers, { imagePath: '/app/resources/images/markerclusterer/m' });
                        //$scope.map.setCenter([52.504185, 13.469238]);
                        mapFactory.map.setCenter(new google.maps.LatLng(52.504185, 13.469238));
                        mapFactory.map.setZoom(4);

                        $scope.showMapButton = true;
                    }, function() {});
            }

            //Set active filters to array for use in Active filters panel
            $scope.setActiveFilters = function() {
                for (index in $scope.searchData) {
                    var exist = false;
                    for (var i = 0; i < $scope.filtersCategories.length; i++) {
                        if (index == $scope.filtersCategories[i].searchString) {
                            exist = true;
                        }
                    }
                    if (exist) {

                        var filterArr = $scope.searchData[index].split(',');

                        for (var i = 0; i < filterArr.length; i++) {
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
            $scope.setActiveArticles = function() {
                if ($scope.searchData.article) {
                    var articles = $scope.searchData.article.split(',');
                    for (var i = 0; i < articles.length; i++) {
                        $scope.activeArticles.push(decodeURIComponent(articles[i]).split('_').join(' '));
                    }
                }
            }

            //Set active categories to array for use in Active filters panel
            $scope.setActiveCategories = function() {
                if ($scope.searchData.category) {
                    var categories = $scope.searchData.category.split(',');
                    for (var i = 0; i < categories.length; i++) {
                        $scope.activeCategories.push(decodeURIComponent(categories[i]).split('_').join(' '));
                    }
                }
            }

            //Set active places to array for use in Active filters panel
            $scope.setActivePlaces = function() {
                if ($scope.searchData.place) {
                    var places = $scope.searchData.place.split(',');
                    for (var i = 0; i < places.length; i++) {
                        $scope.activePlaces.push(decodeURIComponent(places[i]).split('_').join(' '));
                    }
                }
            }

            //Remove filter only from the Selected filters
            $scope.removeFilter = function(categoryName, facetName) {
                if ($scope.searchData[categoryName]) {
                    var filterArr = $scope.searchData[categoryName].split(',');
                    for (var i = 0; i < filterArr.length; i++) {
                        if (filterArr[i] == facetName) {
                            filterArr.splice(i, 1);
                        }
                    }
                    if (filterArr.length == 0) {
                        delete $scope.searchData[categoryName];
                    } else {
                        $scope.searchData[categoryName] = filterArr.join(',');
                    }
                    $location.search($scope.searchData);
                }
            }

            //Add/Remove filter from facets categories menu
            var addRemoveFilterTimeout;
            $scope.addRemoveFilter = function(categoryIndex, facetIndex) {
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
                if (angular.isUndefined($scope.searchData[$scope.filtersCategories[categoryIndex].searchString])) {
                    $scope.searchData[$scope.filtersCategories[categoryIndex].searchString] = newFilter.facetName;
                } else {
                    var temp = $scope.searchData[$scope.filtersCategories[categoryIndex].searchString].split(',');
                    temp.push(newFilter.facetName);
                    $scope.searchData[$scope.filtersCategories[categoryIndex].searchString] = temp.join(",");
                }

                $scope.searchData.offset = 0;
                addRemoveFilterTimeout = $timeout(function() {
                    $location.search($scope.searchData);
                }, 1000);

                $scope.$on("$destroy", function(event) {
                    $timeout.cancel(addRemoveFilterTimeout);
                });
            }

            //Click on article to add it to search filters
            $scope.addSearchArticle = function(article) {
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
            $scope.removeArticle = function(article) {
                var article = encodeURIComponent(article.split(' ').join('_')),
                    articles = $scope.searchData.article.split(',');
                if (articles.length == 1) {
                    delete $scope.searchData.article;
                } else {
                    for (var i = 0; i < articles.length; i++) {
                        if (articles[i] == article) {
                            articles.splice(i, 1);
                            $scope.searchData.article = articles.join(',');
                            break;
                        }
                    }
                }
                $location.search($scope.searchData);
            }

            //Click on Category to add it to search filters
            $scope.addCategory = function(category) {
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
            $scope.removeCategory = function(category) {
                var category = encodeURIComponent(category.split(' ').join('_')),
                    categories = $scope.searchData.category.split(',');
                if (categories.length == 1) {
                    delete $scope.searchData.category;
                } else {
                    for (var i = 0; i < categories.length; i++) {
                        if (categories[i] == category) {
                            categories.splice(i, 1);
                            $scope.searchData.category = categories.join(',');
                            break;
                        }
                    }
                }
                $location.search($scope.searchData);
            }

            //Click on Place to add it to search filters
            $scope.addPlace = function(place) {
                var place = encodeURIComponent(place.split(' ').join('_'));
                if ($scope.searchData.place) {
                    var places = $scope.searchData.place.split(',');
                    for (var i = 0; i < places.length; i++) {
                        if (places[i] == place) {
                            return;
                        }
                    }
                    places.push(place);
                    $scope.searchData.place = places.join(',');
                } else {
                    $scope.searchData.place = place;
                }
                $location.search($scope.searchData);
            }

            //Remove place from search filters
            $scope.removePlace = function(place) {
                var place = encodeURIComponent(place.split(' ').join('_')),
                    places = $scope.searchData.place.split(',');
                if (places.length == 1) {
                    delete $scope.searchData.place;
                } else {
                    for (var i = 0; i < places.length; i++) {
                        if (places[i] == place) {
                            places.splice(i, 1);
                            $scope.searchData.place = places.join(',');
                            break;
                        }
                    }
                }
                $location.search($scope.searchData);
            }

            //Open/Close category // Show/Hide Subcategories and articles
            $scope.openCategory = function(category, path, clickEvent) {
                clickEvent.stopImmediatePropagation();
                var category = category,
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
                    if (category == categories[i].facetName) {
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
                                categories[i].open = true;
                                $scope.getCategories(category, categories, index, path);
                            }
                        }
                        return;
                    }
                }
            }

            //Open/Close place // Show/Hide Subplaces
            $scope.openPlace = function(place, path, clickEvent) {
                clickEvent.stopImmediatePropagation();
                var place = place,
                    path = path,
                    places = $scope.places.categoryFacet;

                //Get current level of categories
                for (var i = 0; i < path.length; i++) {
                    for (var j = 0; j < places.length; j++) {
                        if (path[i] == places[j].facetName) {
                            places = places[j].subElements.categoryFacet;
                            break;
                        }
                    }
                }


                for (var i = 0; i < places.length; i++) {
                    //Find current category
                    if (place == places[i].facetName) {
                        if (places[i].open) {
                            //Close category
                            places[i].open = false;
                            localStorageService.set('places', $scope.places);
                        } else {
                            //Open category
                            if (places[i].subElements) {
                                //Subcategories are loaded from API before - only change open/close state
                                places[i].open = true;
                                localStorageService.set('places', $scope.places);
                            } else {
                                //Load subcategories from API
                                var index = i;
                                places[i].open = true;
                                $scope.getPlaces(place, places, index, path);
                            }
                        }
                        return;
                    }
                }
            }

            //Change result page
            var changePageTimeout;
            $scope.changePage = function(number) {
                if (number && $scope.results.page) {
                    if (number > 0 && $scope.results.page < $scope.totalPages) {
                        $scope.results.page++;
                    } else if (number < 0 && $scope.results.page > 1) {
                        $scope.results.page--;
                    }
                }
                //Cancel previous timeout to wait for user to choose page
                $timeout.cancel(changePageTimeout);
                //Wait 2 seconds before change page (offset) in URL to give time to user to add more digits or click several times on page selector
                changePageTimeout = $timeout(function() {
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
            $scope.loadResource = function(resource) {
                $location.path('/app/resource/' + encodeURIComponent(resource)).search($scope.searchData);
            }

            //First load
            $scope.searchQuery = $scope.searchData.query;
            $scope.search(false);


            $scope.getCategories();
            $scope.getPlaces();

            //Load panelSettings (is panel is open or closed)
            if (localStorageService.get('panelSettings')) {
                //Reload page
                $scope.panelSettings = localStorageService.get('panelSettings');
            } else {
                //First load
                $scope.panelSettings = {
                    foodAndDrink: true,
                    places: true
                }
                localStorageService.set('panelSettings', $scope.panelSettings);
            }

            $scope.toggleOpen = function(index) {
                $scope.panelSettings[index] = !$scope.panelSettings[index];
                localStorageService.set('panelSettings', $scope.panelSettings);
            }

            $scope.removeQuery = function() {
                $scope.searchData.query = '';
                $location.path('/app/search').search($scope.searchData);
            }
        }
    ]);

    efdControllers.controller('ResourceCtrl', ["$scope",
        '$routeParams',
        '$http',
        '$location',
        '$window',
        '$timeout',
        '$modal',
        'toastr',
        'localStorageService',
        function($scope, $routeParams, $http, $location, $window, $timeout, $modal, toastr, localStorageService) {

            $scope.loader = true;
            $scope.params = $routeParams;
            $scope.resourceId = decodeURIComponent($scope.params.resourceId);
            $scope.searchData = $location.search();
            $scope.searchQuery = $scope.searchData.query;
            $scope.resource = {};
            $scope.aggregatedCHO = decodeURIComponent($scope.params.resourceId);

            $scope.getResource = function() {
                $http.get('/app/rest/resource?uri=' + $scope.params.resourceId).
                then(function(response) {
                    $scope.resource = response.data;
                    $scope.loader = false;
                }, function(response) {
                    toastr.error('Resource not found', '');
                    var t = $timeout(function() {
                        $location.path('/app/search').search($scope.searchData);
                    }, 2000);
                    $scope.$on("$destroy", function(event) {
                        $timeout.cancel(t);
                    });
                })
            }

            $scope.toSearchResults = function() {
                $location.path('/app/search').search($scope.searchData);
            }

            $scope.search = function() {
                $location.path('/app/search').search({ query: $scope.searchQuery, limit: 24 });
            }

            ///Copy to clipboard popover options
            $scope.copyToClipboard = function(URI) {
                var modalInstance = $modal.open({
                    templateUrl: 'app/resources/templates/copyToClipboard.html',
                    controller: 'CopyToClipboardModalCtrl',
                    resolve: {
                        URI: function() {
                            return URI;
                        }
                    }
                });

                modalInstance.opened.then(function() {
                    $timeout(function() {
                        document.getElementById('clipboardURI').select();
                    }, 100)
                })
            }

            $scope.getResource();
        }
    ]);

    efdControllers.controller('CopyToClipboardModalCtrl', ["$scope", "$modalInstance", "URI", function($scope, $modalInstance, URI) {

        $scope.clipboardURI = URI;

        $scope.ok = function() {
            $modalInstance.close();
        };

        $scope.cancel = function() {
            $modalInstance.dismiss('cancel');
        };
    }]);

    return efdControllers;

});
