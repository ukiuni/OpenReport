myApp.controller("editProfileController", [ "$rootScope", "$scope", "$http", "$location", "$upload", function($rootScope, $scope, $http, $location, $upload) {
	if (!$rootScope.loginAccount) {
		$location.path('/');
		return;
	}
	$scope.editingAccount = $rootScope.clone($rootScope.loginAccount);
	$scope.onIconImageSelect = function($file) {
		(function() {
			var value = $upload.upload({
				url : "api/account/registImage",
				method : "POST",
				data : {
					accountAccessKey : $rootScope.loginAccount.accessKey
				},
				file : $file
			}).progress(function(evt) {
				console.log('percent: ' + parseInt(100.0 * evt.loaded / evt.total));
			}).success(function(imageKey, status, headers, config) {
				$scope.editingAccount.iconUrl = "api/account/icon/" + imageKey;
			}).error(function(data, status, headers, config) {
				console.log("uploadFailed = " + data);
			});
			console.log("uploaded = " + value);
		})();
	}
	$scope.cancel = function(action) {
		$location.path('/myPage');
	}
	$scope.save = function(action) {
		var accountAccessKey = $rootScope.loginAccount.accessKey;
		$scope.editingAccount.accessKey = accountAccessKey;
		$http.put("api/account/update", $scope.editingAccount).success(function(account) {
			$rootScope.loginAccount = $rootScope.clone(account);
			$rootScope.loginAccount.accessKey = accountAccessKey;
			$location.path('/myPage');
		}).error(function(e) {
			console.log(e)
		});
	}
} ]);