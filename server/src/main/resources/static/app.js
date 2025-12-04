var app = angular.module('myApp', []);

app.controller('MainCtrl', function($scope, $http) {
  // Simple page state: 'login' | 'register' | 'welcome'
  $scope.currentPage = 'login';

  $scope.loginForm = {
    login: '',
    password: ''
  };

  $scope.registerForm = {
    fullName: '',
    login: '',
    email: '',
    password: ''
  };

  $scope.currentUser = {};
  $scope.errorMessage = '';
  $scope.successMessage = '';

  $scope.greetName = '';
  $scope.serverResponse = '';

  $scope.loading = false;
  $scope.loadingGreet = false;

  $scope.goTo = function(page) {
    $scope.errorMessage = '';
    $scope.successMessage = '';
    $scope.currentPage = page;
  };

  $scope.doLogin = function() {
    $scope.errorMessage = '';
    $scope.successMessage = '';
    $scope.loading = true;

    $http.post('/api/auth/login', {
      login: $scope.loginForm.login,
      password: $scope.loginForm.password
    }).then(function(response) {
      $scope.currentUser = response.data || {};
      $scope.successMessage = 'Login successful';
      $scope.currentPage = 'welcome'; // redirect to welcome page
    }, function(error) {
      $scope.errorMessage = (error.data && error.data.error) || 'Login failed';
    }).finally(function() {
      $scope.loading = false;
    });
  };

  $scope.doRegister = function() {
    $scope.errorMessage = '';
    $scope.successMessage = '';
    $scope.loading = true;

    $http.post('/api/auth/register', {
      fullName: $scope.registerForm.fullName,
      login: $scope.registerForm.login,
      email: $scope.registerForm.email,
      password: $scope.registerForm.password
    }).then(function(response) {
      $scope.currentUser = response.data || {};
      $scope.successMessage = 'Registration successful';
      $scope.currentPage = 'welcome'; // redirect to welcome page
    }, function(error) {
      $scope.errorMessage = (error.data && error.data.error) || 'Registration failed';
    }).finally(function() {
      $scope.loading = false;
    });
  };

  $scope.sendName = function() {
    $scope.loadingGreet = true;
    $scope.serverResponse = '';

    $http.get('/api/greet?name=' + encodeURIComponent($scope.greetName), {
      transformResponse: [function (data) {
        return data;
      }]
    }).then(function(response) {
      $scope.serverResponse = response.data;
    }, function(error) {
      $scope.serverResponse = 'Error: ' + error.status;
    }).finally(function() {
      $scope.loadingGreet = false;
    });
  };

  $scope.logout = function() {
    $scope.currentUser = {};
    $scope.loginForm = { login: '', password: '' };
    $scope.registerForm = { fullName: '', login: '', email: '', password: '' };
    $scope.greetName = '';
    $scope.serverResponse = '';
    $scope.errorMessage = '';
    $scope.successMessage = '';
    $scope.currentPage = 'login';
  };
});