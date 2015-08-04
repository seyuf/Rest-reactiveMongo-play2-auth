angular.module('cc.controllers.dashboard.users',[])
.controller('CcDashboardUsersCtrl', function($scope, $state, lodash, Upload, $rootScope, Account, ngTableParams, $filter, $modal){

  $rootScope.currentStateTitle =  $state.current.title;



   var errorCallback = function(error){console.log("error:", error);};
  console.log("users controllers");
  var initUsers = function initUsers(){
  $scope.userPagination = {};
  $scope.userPagination.page = 1;
  $scope.userPagination.numberByPage = 25;
  var getUsers = function(callback){
    console.log("geting users");
    Account.getUserList($scope.userPagination,
     function(userList){
        lodash.forEach(userList, function(user){
         user.fullName = user.lastName+' '+user.firstName;
        });
       callback(userList);
     },
     function(error){
       console.log("error", error);
       callback([]);
     });
  };


  $scope.tableUsers = new ngTableParams({
       page: $scope.userPagination.page,            // show first page
       count: $scope.userPagination.numberByPage,          // count per page
       filter: {
           fullName: '',
           email:''
       },
       sorting:{
           fullName: 'asc',
           role:'asc',
           atl_count:'asc'
       }
   }, {
       total: 0,           // length of data
       getData: function($defer, params) {
           getUsers(function(rows){
               var orderedData = params.filter() ?$filter('orderBy')(rows, params.orderBy()) :rows;
               orderedData = params.filter() ?$filter('filter')(orderedData, params.filter()) :orderedData;
               params.total(orderedData.length);
               $defer.resolve(orderedData.slice((params.page() - 1) * params.count(), params.page() * params.count()));
           });

       }
   });
   };



   $scope.createOrModifyUser = function createOrModifyUser(userIn) {

    $scope.modal = {
      instance: null,
      data:{
         user: userIn,
         tabRef: $scope.tableUsers
      }
     };
     $scope.modal.instance = $modal.open({
       template: '<modal-users modal="modal"></modal-users>',
       backdrop : 'static',
       scope: $scope
     });
    };


    $scope.removeUser = function removeUser(user){
      Account.removeByIds({ids: [user._id.$oid]},
       function(success){ $scope.tableUsers.reload();},
       errorCallback);
    };


  initUsers();
});
