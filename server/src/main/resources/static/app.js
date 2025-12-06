var app = angular.module('myApp', ['pascalprecht.translate']);

app.config(function($translateProvider) {
  // Translations
  var translations = {
    en: {
      TITLE: "Spring + AngularJS Auth",
      LOGIN: "Login",
      REGISTER: "Register",
      WELCOME: "Welcome",
      FULL_NAME: "Full name",
      LOGIN_FIELD: "Login",
      EMAIL: "Email",
      PASSWORD: "Password",
      CONFIRM_PASSWORD: "Confirm Password",
      ENTER_LOGIN: "Enter your login",
      ENTER_PASSWORD: "Enter your password",
      YOUR_FULL_NAME: "Your full name",
      CHOOSE_LOGIN: "Choose a login",
      YOUR_EMAIL: "Your email",
      CREATE_PASSWORD: "Create a password",
      CONFIRM_YOUR_PASSWORD: "Confirm your password",
      BACK: "Back",
      DONT_HAVE_ACCOUNT: "Don't have an account?",
      LOG_OUT: "Log out",
      HELLO: "Hello, {{name}}!",
      LOGIN_SUCCESSFUL: "Login successful",
      REGISTRATION_SUCCESSFUL: "Registration successful",
      LOGIN_FAILED: "Login failed",
      REGISTRATION_FAILED: "Registration failed",
      PASSWORDS_DO_NOT_MATCH: "Passwords do not match",
      TEST_GREETING_ENDPOINT: "Test greeting endpoint",
      NAME_FOR_GREETING: "Name for greeting",
      NAME_FOR_API_GREET: "Name for /api/greet",
      SEND: "Send",
      SERVER_RESPONSE: "Server response:",
      ERROR: "Error:",
      LANGUAGE: "Language",
      ENGLISH: "English",
      RUSSIAN: "Russian"
    },
    ru: {
      TITLE: "Spring + AngularJS Auth",
      LOGIN: "Вход",
      REGISTER: "Регистрация",
      WELCOME: "Добро пожаловать",
      FULL_NAME: "Полное имя",
      LOGIN_FIELD: "Логин",
      EMAIL: "Email",
      PASSWORD: "Пароль",
      CONFIRM_PASSWORD: "Подтвердите пароль",
      ENTER_LOGIN: "Введите ваш логин",
      ENTER_PASSWORD: "Введите ваш пароль",
      YOUR_FULL_NAME: "Ваше полное имя",
      CHOOSE_LOGIN: "Выберите логин",
      YOUR_EMAIL: "Ваш email",
      CREATE_PASSWORD: "Создайте пароль",
      CONFIRM_YOUR_PASSWORD: "Подтвердите ваш пароль",
      BACK: "Назад",
      DONT_HAVE_ACCOUNT: "Нет аккаунта?",
      LOG_OUT: "Выйти",
      HELLO: "Привет, {{name}}!",
      LOGIN_SUCCESSFUL: "Вход выполнен успешно",
      REGISTRATION_SUCCESSFUL: "Регистрация выполнена успешно",
      LOGIN_FAILED: "Ошибка входа",
      REGISTRATION_FAILED: "Ошибка регистрации",
      PASSWORDS_DO_NOT_MATCH: "Пароли не совпадают",
      TEST_GREETING_ENDPOINT: "Тест приветствия",
      NAME_FOR_GREETING: "Имя для приветствия",
      NAME_FOR_API_GREET: "Имя для /api/greet",
      SEND: "Отправить",
      SERVER_RESPONSE: "Ответ сервера:",
      ERROR: "Ошибка:",
      LANGUAGE: "Язык",
      ENGLISH: "Английский",
      RUSSIAN: "Русский"
    }
  };
  
  $translateProvider.translations('en', translations.en);
  $translateProvider.translations('ru', translations.ru);
  
  // Set default language
  var savedLang = localStorage.getItem('preferredLanguage') || 'en';
  $translateProvider.preferredLanguage(savedLang);
  $translateProvider.fallbackLanguage('en');
});

app.controller('MainCtrl', function($scope, $http, $translate) {
  // Simple page state: 'login' | 'register' | 'welcome'
  $scope.currentPage = 'login';
  
  // Language management
  var savedLang = localStorage.getItem('preferredLanguage') || 'en';
  $scope.currentLang = savedLang;
  $translate.use(savedLang);
  
  $scope.changeLanguage = function(lang) {
    $translate.use(lang);
    localStorage.setItem('preferredLanguage', lang);
  };

  $scope.loginForm = {
    login: '',
    password: ''
  };

  $scope.registerForm = {
    fullName: '',
    login: '',
    email: '',
    password: '',
    confirmPassword: ''
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
      $translate('LOGIN_SUCCESSFUL').then(function(translation) {
        $scope.successMessage = translation;
      });
      $scope.currentPage = 'welcome'; // redirect to welcome page
    }, function(error) {
      var errorMsg = error.data && error.data.error;
      if (errorMsg) {
        $scope.errorMessage = errorMsg;
      } else {
        $translate('LOGIN_FAILED').then(function(translation) {
          $scope.errorMessage = translation;
        });
      }
    }).finally(function() {
      $scope.loading = false;
    });
  };

  $scope.doRegister = function() {
    $scope.errorMessage = '';
    $scope.successMessage = '';
    
    // Validate that passwords match
    if ($scope.registerForm.password !== $scope.registerForm.confirmPassword) {
      $translate('PASSWORDS_DO_NOT_MATCH').then(function(translation) {
        $scope.errorMessage = translation;
      });
      return;
    }
    
    $scope.loading = true;

    $http.post('/api/auth/register', {
      fullName: $scope.registerForm.fullName,
      login: $scope.registerForm.login,
      email: $scope.registerForm.email,
      password: $scope.registerForm.password
    }).then(function(response) {
      $scope.currentUser = response.data || {};
      $translate('REGISTRATION_SUCCESSFUL').then(function(translation) {
        $scope.successMessage = translation;
      });
      $scope.currentPage = 'welcome'; // redirect to welcome page
    }, function(error) {
      var errorMsg = error.data && error.data.error;
      if (errorMsg) {
        $scope.errorMessage = errorMsg;
      } else {
        $translate('REGISTRATION_FAILED').then(function(translation) {
          $scope.errorMessage = translation;
        });
      }
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
      $translate('ERROR').then(function(translation) {
        $scope.serverResponse = translation + ' ' + error.status;
      });
    }).finally(function() {
      $scope.loadingGreet = false;
    });
  };

  $scope.logout = function() {
    $scope.currentUser = {};
    $scope.loginForm = { login: '', password: '' };
    $scope.registerForm = { fullName: '', login: '', email: '', password: '', confirmPassword: '' };
    $scope.greetName = '';
    $scope.serverResponse = '';
    $scope.errorMessage = '';
    $scope.successMessage = '';
    $scope.currentPage = 'login';
  };
});