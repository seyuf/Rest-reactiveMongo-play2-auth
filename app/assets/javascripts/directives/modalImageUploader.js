angular.module('cc.directive.modalImageUploader',[])
.directive('modalImageUploader', function(lodash){
  return{
     restrict: 'E',
     templateUrl:'/assets/partials/directives/modal-image-uploader.html',
     scope:{
      modal: '='
     },
     controller: function ($scope, Upload) {

       console.log("user modal", $scope.modal);
       $scope.entity = $scope.modal.data.entity;
       var baseUrl = '';
       if($scope.modal.data.type == 'atelier'){
          baseUrl = '/ateliers/logos/';
       }else if($scope.modal.data.type == 'proposition'){
          baseUrl = '/propositions/logos/';
       }else if($scope.modal.data.type == 'carousel'){
          baseUrl = '/carousel/logos/';
       }

       var successUpload = function (data) {
         $scope.modal.data.tabRef.reload();
         $scope.progressIcon = false;
         $scope.fileUploadedSuccess = true;
       };

       var progressUpload = function (evt) {
         $scope.showUploadSection = false;
         $scope.progressPercentage = parseInt(100.0 * evt.loaded / evt.total);
         $scope.progressIcon = true;
       };

       var errorUpload = function(error){
         console.log("error uploading !!"+error);
         $scope.progressIcon = false;
         $scope.fileUploadedError=true;
       };

       $scope.$watch('imageToUp', function () {
         $scope.upload($scope.imageToUp);
       });

       $scope.upload = function (files) {
         console.log(files);
         if (files && files.length) {
            for (var i = 0; i < files.length; i++) {
                var file = files[i];
                Upload.upload({
                    url: baseUrl+$scope.entity._id.$oid,
                    file: file
                }).progress(progressUpload).success(successUpload).error(errorUpload);
            }
         }
       };

       $scope.modifyImage = function(entityIn){
         $scope.currentEntity = entityIn;
         $scope.modifyImageAt = false;
         $scope.fileUploadedSuccess = false;
         $scope.fileUploadedError = false;
         $scope.progressIcon = false;
         $scope.showUploadSection = true;
       };

       $scope.cancel = function () {
         $scope.modal.instance.dismiss('cancel');
       };


     }
  };
});
