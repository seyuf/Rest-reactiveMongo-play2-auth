dependencies = [
    'ngRoute',
    'ngResource',
    'ngSanitize',
    'ngFileUpload',
    'cc.controllers.dashboard.main',
    'cc.controllers.dashboard.users',
    'cc.controllers.login',
    'cc.directive.focusme',
    'cc.directive.modalUsers',
    'cc.directive.modalImageUploader',
    'cc.filters.properties',
    'cc.filters.time',
    'ui.bootstrap',
    'cc.services.session',
    'cc.services.account',
    'LocalStorageModule',
    'ngAnimate',
    'ngLodash',
    'http-auth-interceptor',
    'ngTable',
    'ui.select',
    'ui.router'
    ];

angular.module('reactive-auth',dependencies)
.config(function($stateProvider, $urlRouterProvider) {
$urlRouterProvider.otherwise("app/users");
 $stateProvider
  .state('app', {
    url: "/app",
    abstract: true,
    templateUrl: "/assets/partials/main.html",
    controller: 'CcDashboardMainCtrl',
     data:{
          authorizedRoles: ['Administrator']
     }
  })
  .state('app.users', {
    url: "/users",
    views:{
     'menuContent': {
        templateUrl: "/assets/partials/users.html",
        controller: 'CcDashboardUsersCtrl'
     }
    },
    title:"Users"
  });

})

.config(function($httpProvider) {
    $httpProvider.defaults.withCredentials = true;

})
.config(function (localStorageServiceProvider) {
  localStorageServiceProvider
  .setPrefix('reactive-auth')
  .setNotify(true, true);
})
.run(function($rootScope, CCSession, $route, Logout, $location){
   $rootScope.logged = true;

   CCSession.isLogged(function(isConnected){
    $rootScope.logged = isConnected;
    if(isConnected){
       $rootScope.loggedUser = CCSession.getSession();
    }
   });
   $rootScope.logUser = function logUser() {
        $('#login-modal').modal();
        return;
   };


   $rootScope.mainDomain = window.location.host;
   $rootScope.$on('event:auth-loginConfirmed', function(event, data){
        console.log("successfully log in");
      });

   $rootScope.$on('event:auth-loginRequired', function(event, data){
        $('#login-modal').modal();
     $rootScope.logUser();
     CCSession.destroy();
     return;
   });
   $rootScope.$on('event:auth-forbidden', function(event, data){
        $('#login-modal').modal();
     $rootScope.logUser();
     CCSession.destroy();
     return;
   });

    $rootScope.$on('event:not-authorized', function(event, data){
    console.log("unauthorized");
    CCSession.destroy();
    $rootScope.logUser();
     return;
   });

   $rootScope.cancelLogin = function cancelLogin(){
     CCSession.destroy();
     $rootScope.logUser();
     return;
   };

   $rootScope.$on('$stateChangeStart', function (event, next, toParams, fromState) {

     var authorizedRoles = next.data ? next.data.authorizedRoles : null;
     if(authorizedRoles === null) return;
       if ($rootScope.logged) {
         if (!CCSession.isAuthorized(authorizedRoles)) {
           $rootScope.$broadcast('event:not-authorized');
         }
       }
       else{
           $rootScope.$broadcast('event:not-authorized');
       }
   });

   $rootScope.logOut =function logOut() {
        Logout.get({}, function(res){
            $rootScope.logged = false;
            CCSession.destroy();
            $rootScope.logUser();
        }, function(error){
         console.log("logout", error);
        });
   };
});
