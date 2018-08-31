var app = angular.module("pinyougouApp",[]);

//设置html过滤器,引入$sce服务
app.filter("trustHtml",['$sce',function ($sce) {

    return function (data) {
        return $sce.trustAsHtml(data);
    }

}])