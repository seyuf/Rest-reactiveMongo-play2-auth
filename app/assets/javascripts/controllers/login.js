

angular.module('cc.controllers.login', [])
  .controller('ccLoginCtrl', function($scope, CCSession, $route, authService, $rootScope) {

    $scope.master = {};
    $scope.loading = false;

    $scope.login = function(credentials) {
      $scope.loading = true;

      CCSession.login(credentials, function(session){
        console.log("stored user is: ", session);
        $('#login-modal').modal('hide');
        window.location.reload();

         authService.loginConfirmed();
      },
         function(error){
         $rootScope.logErrorMsg = "Wrong identifier or password...";
         console.log("failed to log in: ", error);
         });

      $scope.master = angular.copy(credentials);
    };

    $scope.reset = function() {
      $scope.credentials = angular.copy($scope.master);
    };

    $scope.reset();

  });
