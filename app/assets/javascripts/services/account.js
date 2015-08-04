angular.module('cc.services.account',['ngResource'])
.factory('Admin', function($resource){
    return $resource('/account/admin/:id', {id:'@id'});
})
.factory('Login', function($resource){
    return $resource('/login/:id', {id:'@id'});
})
.factory('Logout', function($resource){
    return $resource('/logout/:id', {id:'@id'});
})
.factory('Account', function($resource){
return $resource('/account/create',{},{
   getUserList:{
    method: 'GET',
    isArray: true,
    url: '/account/list'
   },
   updateUser:{
    method: 'POST',
    url: '/account/update'
   },
   removeByIds:{
     method: 'DELETE',
     url: '/account/remove'
   },
   byAteliers:{
    method: 'GET',
    isArray: true,
    url: '/account/atelier/:id'
   },
   byId:{
    method: 'GET',
    isArray: true,
    url: '/account/:id'
   },
   modifyBasics:{
    method: 'POST',
    url: '/account/basics'
   }
});
})
.factory('Me', function($resource){
return $resource('/account/current:id', {id:'@id'});
});
