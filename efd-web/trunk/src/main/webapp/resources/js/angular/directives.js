/**
 * Created by Rostislav on 28-Sep-15.
 */

define(['angular'], function(){
    var efdDirectives = angular.module('efdApp.directives', []);

    efdDirectives.directive('searchForm', function () {
        return {
            restrict: 'A',
            templateUrl: 'resources/js/angular/templates/search-form.html'
        };
    });

    efdDirectives.directive('ontoLoader', function () {
        return {
            template: function (elem, attr) {return '<object width="' + attr.size + '" height="' + attr.size + '" data="resources/images/ot-loader.svg">Loading...</object>'}
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

    return efdDirectives;
})