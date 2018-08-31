app.controller("searchController",function ($scope, $controller, searchService) {

    //继承
    $controller("baseController",{$scope:$scope});

    $scope.search = function () {
        searchService.search($scope.searchMap).success(
            function (response) {
                $scope.resultMap = response;
            }
        )
    }

    //构建搜索对象
    $scope.searchMap = {keywords:"",category:"",brand:"",spec:{}};

    //添加搜索项
    $scope.addSearchItem = function (key, value) {
        if (key=="category" || key=="brand"){
            $scope.searchMap[key] = value;
        } else {
            $scope.searchMap.spec[key] = value;
        }
        $scope.search();
    }


    //移除搜索项
    $scope.removeSearchItem = function (key) {
        if (key=="category" || key=="brand"){
            $scope.searchMap[key] = "";
        } else {
            delete $scope.searchMap.spec[key] ;
        }
        $scope.search();
    }
})