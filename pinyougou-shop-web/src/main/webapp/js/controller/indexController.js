app.controller("indexController", function ($scope, indexService) {

    $scope.loginName = indexService.getLoginName().success(
        function (response) {
            $scope.loginName = response.loginName;
        }
    )
})