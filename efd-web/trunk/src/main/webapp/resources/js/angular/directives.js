/**
 * Created by Rostislav on 28-Sep-15.
 */

define(['angular'], function(){
    var efdDirectives = angular.module('efdApp.directives', []);

    efdDirectives.directive('searchForm', function () {
        return {
            restrict: 'A',
            templateUrl: 'app/resources/js/angular/templates/search-form.html'
        };
    });

    efdDirectives.directive('ontoLoader', function () {
        return {
            template: function (elem, attr) {return '<object width="' + attr.size + '" height="' + attr.size + '" data="app/resources/images/ot-loader.svg">Loading...</object>'}
        };
    });

    efdDirectives.directive('disableAnimation', function($animate){
        return {
            restrict: 'A',
            link: function($scope, $element, $attrs){
                $attrs.$observe('disableAnimation', function(value){
                    $animate.enabled(!value, $element);
                });
            }
        }
    });


    efdDirectives.directive('placesCollection', function () {
        return {
            restrict: "E",
            replace: true,
            scope: {
                collection: '=',
                addClass: '='
            },
            controller: function($scope){
                $scope.openPlace = $scope.$parent.openPlace;
                $scope.addPlace = $scope.$parent.addPlace;
            },
            template: '<div class="{{addClass}}">' +
            '<ul class="list-unstyled"><place ng-repeat="place in collection.categoryFacet | orderBy:\'facetName\'" place="place" path="collection.path"></place></ul>' +
            '</div>'
        }
    });

    efdDirectives.directive('place', function ($compile) {
        return {
            restrict: "E",
            replace: true,
            scope: {
                place: '=',
                path: '='
            },
            controller: function($scope){
                $scope.openPlace = $scope.$parent.openPlace;
                $scope.addPlace = $scope.$parent.addPlace;
            },
            template: '<li class="" style="min-height: 26px; padding: 0 0 0 10px; margin: 0;" ng-class="place.open ? \'open-subcategories\' : \'\'">' +
            '<div class="clearfix">' +
            '<div class="pull-left pointer" ng-click="openPlace(place.facetName, path, $event)">' +
            '<span ng-hide="place.open" class="fa-stack">' +
            '<i class="fa fa-square-o fa-stack-2x"></i>' +
            '<i class="fa fa-plus fa-stack-1x"></i>' +
            '</span>' +
            '<span ng-show="place.open" class="fa-stack">' +
            '<i class="fa fa-square-o fa-stack-2x"></i>' +
            '<i class="fa fa-minus fa-stack-1x"></i>' +
            '</span>' +
            '</div>' +
            '<span class="pointer category-name" ng-click="addPlace(place.facetName)">' +
            ' {{place.facetName}} ' +
            '<span class="badge">{{place.facetValue}}</span>' +
            '</span>' +
            '</div>' +
            '</li>',
            link: function (scope, element, attrs) {
                scope.$watch(function(scope) { return scope.place.subElements },
                    function(newValue, oldValue) {
                        if (scope.place.subElements) {
                            element.append('<places-collection collection="place.subElements"></places-collection>');
                            $compile(element.contents())(scope)
                        }
                    }
                );
            }
        }
    });







    efdDirectives.directive('categoryCollection', function () {
        return {
            restrict: "E",
            replace: true,
            scope: {
                collection: '=',
                addClass: '='
            },
            controller: function($scope){
                $scope.openCategory = $scope.$parent.openCategory;
                $scope.addCategory = $scope.$parent.addCategory;
                $scope.addSearchArticle = $scope.$parent.addSearchArticle;
            },
            template: '<div class="{{addClass}}">' +
                '<ul class="list-unstyled"><category ng-repeat="category in collection.categoryFacet | orderBy:\'facetName\'" category="category" path="collection.path"></category></ul>' +
                '<ul class="list-unstyled"><category-article ng-repeat="article in collection.articleModel | orderBy:\'facetName\'" article="article"></category-article></ul>' +
            '</div>'
        }
    });


    efdDirectives.directive('category', function ($compile) {
        return {
            restrict: "E",
            replace: true,
            scope: {
                category: '=',
                path: '='
            },
            controller: function($scope){
                $scope.openCategory = $scope.$parent.openCategory;
                $scope.addCategory = $scope.$parent.addCategory;
                $scope.addSearchArticle = $scope.$parent.addSearchArticle;
            },
            template: '<li class="" style="min-height: 26px; padding: 0 0 0 10px; margin: 0;" ng-class="category.open ? \'open-subcategories\' : \'\'">' +
            '<div class="clearfix">' +
                '<div class="pull-left pointer" ng-click="openCategory(category.facetName, path, $event)">' +
                    '<span ng-hide="category.open" class="fa-stack">' +
                        '<i class="fa fa-square-o fa-stack-2x"></i>' +
                        '<i class="fa fa-plus fa-stack-1x"></i>' +
                    '</span>' +
                    '<span ng-show="category.open" class="fa-stack">' +
                        '<i class="fa fa-square-o fa-stack-2x"></i>' +
                        '<i class="fa fa-minus fa-stack-1x"></i>' +
                    '</span>' +
                '</div>' +
                '<span class="pointer category-name" ng-click="addCategory(category.facetName)">' +
                    ' {{category.facetName}} ' +
                    '<span class="badge">{{category.facetValue}}</span>' +
                '</span>' +
            '</div>' +
            '</li>',
            link: function (scope, element, attrs) {
                scope.$watch(function(scope) { return scope.category.subElements },
                    function(newValue, oldValue) {
                        if (scope.category.subElements) {
                            element.append('<category-collection collection="category.subElements"></category-collection>');
                            $compile(element.contents())(scope)
                        }
                    }
                );
            }
        }
    });

    efdDirectives.directive('categoryArticle', function () {
        return {
            restrict: "E",
            replace: true,
            scope: {
                article: '=',
                addSearchArticle: '&'
            },
            controller: function($scope){
                $scope.addSearchArticle = $scope.$parent.addSearchArticle;
            },
            template: '<li class="pointer" ng-click="addSearchArticle(article.facetName)" style="min-height: 30px; padding: 0 0 0 16px; margin: 0;">' +
            '<i class="fa fa-file-text-o fa-lg"></i>' +
            '<span class="pointer">' +
            ' {{article.facetName}} ' +
            '<span class="badge">{{article.facetValue}}</span>' +
            '</span>' +
            '</li>'
        }
    });

    efdDirectives.directive('logo', function () {
        return {
            restrict: "E",
            replace: true,
            controller: function($scope, localStorageService, $location){
                $scope.logoClick = function(){
                    //Clear sessionStorage categories so you can take categories for empty category
                    localStorageService.remove('categories');
                    $location.path('/');
                }
            },
            template: '<span ng-click="logoClick()">' +
                '<img src="/app/resources/images/efd.png" class="img-responsive pointer"  alt="Europeana Food and Drink" style="margin: 0 auto;"/>' +
            '</span>'
        }
    });

    efdDirectives.directive('addHref', function(){
        return {
            restrict: 'E',
            scope: {
                hrefData: '='
            },
            link: function(scope, element, attrs) {
                var el = '';
                if (typeof scope.hrefData === "string" && (scope.hrefData.substr(0,4) == 'http' || scope.hrefData.substr(0,4) == 'www.')) {
                    var el = angular.element('<a href="' + scope.hrefData + '" target="_blank">' +  scope.hrefData + '</a>');
                } else {
                    el = scope.hrefData;
                }

                element.replaceWith(el);
            }
        }
    })

    return efdDirectives;
})