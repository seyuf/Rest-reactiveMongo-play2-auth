angular.module('cc.services.session', ['ngResource'])
.factory('CCSession', function($resource, localStorageService, Login, Me, lodash) {

  var session = localStorageService.get('session');
  //console.log("session", session);
  var isCookieSet = localStorageService.cookie.get('PLAY_SESSION');

  return {
    'destroy': function() {
        //TODO implement pop up choosing
        session = null;
        localStorageService.remove('session');
    },
    'getSession': function() {
      return session;
    },
    'updateSession': function(data){
      if(session !== null){
        localStorageService.set('session', data);
      }
      return;
    },
    'isLogged': function(callbackSuccess) {
       return Me.get({}, function(response){
            session = response.user;
            localStorageService.set('session', session);
            callbackSuccess(true);

        }, function(error){
            callbackSuccess(false);
        });
    },
    'login': function(userInfo, successCallback, failedCallback) {
      return Login.save(userInfo, function(logged, SShead, d) {
        Me.get({}, function(response){
            session = response.user;
            localStorageService.set('session', session);
            console.log("setting the session", SShead(), d, logged);
            successCallback(response);
        });
      }, failedCallback);
    },
    'isAdmin': function(){
       return (session && session.role && session.role === "Administrator")? true: false;
    },
    'isAuthorized': function(authorizedRoles){
       var tmpSession = {};
       if(session === null || session.role === undefined){
        return false;
       }
       //TODO user's role could be a list (array) in the future
       if(typeof session.role === 'string'){
         tmpSession.role = [session.role];
       }

       return lodash.some(tmpSession.role, function(value) {
                    return lodash.contains(authorizedRoles, value);
                  });
    },
    'isNormal': function(){
       return (session && session.role && session.role === "NormalUser")? true: false;
    }
  };
});