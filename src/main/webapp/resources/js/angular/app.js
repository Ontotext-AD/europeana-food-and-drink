require.config({
    // alias libraries paths
    paths: {
        'angular': '../../../webjars/angularjs/1.4.4/angular.min',
        'angularRoute': '../../../webjars/angularjs/1.4.4/angular-route.min',
        'angularMessages': '../../../webjars/angularjs/1.4.4/angular-messages.min',
        'angularLocalStorage': '../../../webjars/angular-local-storage/0.2.1/angular-local-storage.min',
        'toastr': '../../../webjars/angular-toastr/1.5.0/angular-toastr.tpls',
        'ui-bootstrap-tpls': '../../../webjars/angular-ui-bootstrap/0.13.3/ui-bootstrap-tpls.min',
        'ui-bootstrap': '../../../webjars/angular-ui-bootstrap/0.13.3/ui-bootstrap-tpls'
    },

    // angular does not support AMD out of the box, put it in a shim
    shim: {
        'angular': {
            exports: 'angular'
        },
        'angularRoute': {
            deps: ['angular'],
            exports: 'angular'
        },
        'angularMessages': {
            deps: ['angular'],
            exports: 'angular'
        },
        'angularLocalStorage': {
            deps: ['angular'],
            exports: 'angular'
        },
        'toastr': {
            deps: ['angular'],
            exports: 'angular'
        },
        'ui-bootstrap-tpls': {
            deps: ['angular'],
            exports: 'angular'
        },
        'ui-bootstrap': {
            deps: ['angular', 'ui-bootstrap-tpls'],
            exports: 'angular'
        }
    }
});

define([
        'angular',
        'controllers',
        'directives',
        'angularRoute',
        'angularMessages',
        'angularLocalStorage',
        'ui-bootstrap-tpls',
        'ui-bootstrap',
        'toastr'
    ],
    function() {
        (function(angular){

            var efd = angular.module('efdApp', [
                'ngRoute',
                'ngMessages',
                'efdApp.controllers',
                'efdApp.directives',
                'ui.bootstrap',
                'toastr',
                'LocalStorageModule'
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

                $routeProvider.when('/app', {
                    templateUrl : 'app/resources/templates/home.html',
                    controller : 'HomeCtrl'
                }).when('/app/search', {
                    templateUrl : 'app/resources/templates/results.html',
                    controller : 'ResultCtrl'
                }).when('/app/resource/:resourceId', {
                    templateUrl : 'app/resources/templates/resource.html',
                    controller : 'ResourceCtrl'
                }).when('/app/contact-us', {
                    templateUrl : 'app/resources/templates/contact-us.html'
                }).when('/app/about-us', {
                    templateUrl : 'app/resources/templates/about-us.html'
                }).otherwise({
                    redirectTo: '/app'
                });

                $locationProvider.html5Mode(true);
            }]);

            angular.bootstrap(document, ['efdApp']);

        })(angular)
    })