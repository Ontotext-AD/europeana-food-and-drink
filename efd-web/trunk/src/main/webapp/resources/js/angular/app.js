require.config({
    // alias libraries paths
    paths: {
        'angularJS': '../../../webjars/angularjs/1.4.4/angular.min',
        'angularRoute': '../../../webjars/angularjs/1.4.4/angular-route.min',
        'toastr': '../../../webjars/angular-toastr/1.5.0/angular-toastr',
        'ui-bootstrap-tpls': '../../../webjars/angular-ui-bootstrap/0.13.3/ui-bootstrap-tpls.min',
        'ui-bootstrap': '../../../webjars/angular-ui-bootstrap/0.13.3/ui-bootstrap-tpls'
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
        },
        'ui-bootstrap': {
            deps: ['angularJS', 'ui-bootstrap-tpls'],
            exports: 'angular'
        }
    }
});

define([
        'angularJS',
        'controllers',
        'angularRoute',
        'ui-bootstrap-tpls',
        'ui-bootstrap',
        'toastr'
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
                    templateUrl : 'resources/templates/home.html',
                    controller : 'SearchCtrl'
                }).when('/search', {
                    templateUrl : 'resources/templates/results.html',
                    controller : 'ResultCtrl'
                }).when('/resource/:resourceId', {
                    templateUrl : 'resources/templates/resource.html',
                    controller : 'ResourceCtrl'
                }).otherwise({
                    redirectTo: '/'
                });

                $locationProvider.html5Mode(true);
            }]);

            angular.bootstrap(document, ['efdApp']);

        })(angular)
    })