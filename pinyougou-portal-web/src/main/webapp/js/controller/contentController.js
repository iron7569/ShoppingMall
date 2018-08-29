app.controller("contentController",function ($scope, $controller, contentService) {

    $controller("baseController",{$scope:$scope});

    $scope.contentList = [];

    $scope.findByCategoryId = function (categoryId) {
        contentService.findByCategoryId(categoryId).success(
            function (response) {
                $scope.contentList[categoryId] = response;
            }
        )
    }

})