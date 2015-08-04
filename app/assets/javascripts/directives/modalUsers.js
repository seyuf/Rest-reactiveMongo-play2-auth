angular.module('cc.directive.modalUsers',['cc.filters.properties'])
.directive('modalUsers', function(lodash, Account){
    return{
        restrict: 'E',
        templateUrl:'/assets/partials/directives/modal-users.html',
        scope:{
         modal: '='
        },
        controller: function ($scope) {

         console.log("user modal", $scope.modal);
         $scope.user = $scope.modal.data.user;
         var successCb = function successCb(success){
            $scope.modal.data.tabRef.reload();
         };


         var errorCb = function errorCb(error){
           console.log("error: ", error);
         };

         $scope.valider = function valider(userMod){
            if($scope.user._id){
              Account.updateUser($scope.user,successCb, errorCb);
            }else{
               $scope.user.avatar_path = '/assets/images/avatars/default1.png';
               Account.save($scope.user, successCb, errorCb);
            }
           $scope.modal.instance.close();
         };

         $scope.cancel = function () {
           $scope.modal.instance.dismiss('cancel');
         };

      }
    };
});
