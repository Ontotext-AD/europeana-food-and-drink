/**
 * Created by Rostislav on 23-Sep-15.
 */


define(['angularJS'], function(){
    var efdControllers = angular.module('efdApp.controllers', []);

    efdControllers.controller('MainCtrl', ["$scope",  function($scope) {

        $scope.test = 'MainCtrl';
    }]);

    efdControllers.controller('SearchCtrl', ["$scope", function($scope) {

        $scope.test = 'SearchCtrl';
    }]);

    efdControllers.controller('ResultCtrl', ["$scope", function($scope) {

        $scope.test = 'ResultCtrl';
    }]);

    efdControllers.controller('ResourceCtrl', ["$scope", function($scope) {

        $scope.test = 'ResourceCtrl';
    }]);

    return efdControllers;

});
