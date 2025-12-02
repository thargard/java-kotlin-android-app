var app = angular.module('myApp', []);

app.controller('MainCtrl', function($scope, $http) {
  $scope.userName = "";
  $scope.serverResponse = "";

  $scope.sendName = function() {
    $http.get('/api/greet?name=' + $scope.userName,{
      transformResponse: [function (data) {
        return data;
      }]
    })
      .then(function(response) {
        $scope.serverResponse = response.data;
      }, function(error) {
        $scope.serverResponse = "Ошибка: " + error.status;
      });
  };
});