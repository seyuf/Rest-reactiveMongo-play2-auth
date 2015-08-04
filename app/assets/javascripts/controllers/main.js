angular.module('cc.controllers.dashboard.main',[])
.controller('CcDashboardMainCtrl', function($scope, $rootScope, $state, $modal){
  $scope.menus = [
   {name:'Users', url: "users"}
  ];
  $scope.currentMenu = $scope.menus[0];
  $rootScope.currentStateTitle =  $state.current.title;

  console.log("current menu", $scope.currentMenu);
  $scope.upateMenu = function(menuName){
     $scope.currentMenu = menuName;
  };

});
