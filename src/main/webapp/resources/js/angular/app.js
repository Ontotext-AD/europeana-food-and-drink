require.config({
    // alias libraries paths
    paths: {
        'angularJS': '../../../webjars/angularjs/1.4.4/angular.min',
        'angularRoute': '../../../webjars/angularjs/1.4.4/angular-route.min',
        'toastr': '../../../webjars/angular-toastr/1.5.0/angular-toastr',
        'ui-bootstrap-tpls': '../../../webjars/angular-ui-bootstrap/0.13.3/ui-bootstrap-tpls.min'
    },

    // angular does not support AMD out of the box, put it in a shim
    shim: {
        'angularJS': {
            exports: 'angular'
        },
        'angularRoute': {
            deps: ['angularJS'],
            exports: 'angular'
        },
        'toastr': {
            deps: ['angularJS'],
            exports: 'angular'
        },
        'ui-bootstrap-tpls': {
            deps: ['angularJS'],
            exports: 'angular'
        }
    }
});

define([
        'angularJS',
        'controllers',
        'angularRoute',
        'ui-bootstrap-tpls',
        'toastr',
        '../../../webjars/angular-ui-bootstrap/0.13.3/ui-bootstrap.min'
    ],
    function() {
        (function(angular){

            var efd = angular.module('efdApp', [
                'ngRoute',
                'efdApp.controllers',
                'ui.bootstrap',
                'toastr'
            ]);

            efd.config([
                '$routeProvider',
                '$locationProvider',
                'toastrConfig',
                function($routeProvider, $locationProvider, toastrConfig){

                angular.extend(toastrConfig, {
                    timeOut: 5000,
                    positionClass: 'toast-bottom-right'
                });

                $routeProvider.when('/', {
                    templateUrl : 'resources/pages/home.html',
                    controller : 'SearchCtrl'
                }).when('/searching', {
                    templateUrl : 'resources/pages/results.html',
                    controller : 'ResultCtrl'
                }).when('/record/:resourceId', {
                    templateUrl : 'resources/pages/resource.html',
                    controller : 'ResourceCtrl'
                }).otherwise({
                    templateUrl : 'resources/pages/home.html',
                    controller : 'SearchCtrl'
                });

                $locationProvider.html5Mode(true);
            }]);

            angular.bootstrap(document, ['efdApp']);

        })(angular)
    })