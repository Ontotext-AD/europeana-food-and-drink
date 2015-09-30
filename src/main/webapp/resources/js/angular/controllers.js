/**
 * Created by Rostislav on 23-Sep-15.
 */


define(['angularJS'], function(){
    var efdControllers = angular.module('efdApp.controllers', [
        'ngRoute'
    ]);

    efdControllers.controller('MainCtrl', ["$scope",  function($scope) {

        $scope.test = 'MainCtrl';
    }]);

    efdControllers.controller('SearchCtrl', ["$scope", function($scope) {
        $scope.test = 'SearchCtrl';
    }]);

    efdControllers.controller('ResultCtrl', ["$scope", '$routeParams', function($scope, $routeParams) {
        $scope.params = $routeParams;
        $scope.test = 'ResultCtrl';
    }]);

    efdControllers.controller('ResourceCtrl', ["$scope", '$routeParams', function($scope, $routeParams) {

        $scope.params = $routeParams;
        $scope.test = 'ResourceCtrl';
    }]);

    return efdControllers;

});
