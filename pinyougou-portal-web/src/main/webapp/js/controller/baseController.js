//品牌控制层 
app.controller('baseController', function ($scope) {

	//重新加载列表 数据
	$scope.reloadList = function () {
		//切换页码  
		$scope.search($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
	}

	//分页控件配置 
	$scope.paginationConf = {
		currentPage: 1,
		totalItems: 10,
		itemsPerPage: 10,
		perPageOptions: [10, 20, 30, 40, 50],
		onChange: function () {
			$scope.reloadList();//重新加载
		}
	};

	$scope.selectIds = [];//选中的ID集合 

	//更新复选
	$scope.updateSelection = function ($event, id) {
		if ($event.target.checked) {//如果是被选中,则增加到数组
			$scope.selectIds.push(id);
		} else {
			var idx = $scope.selectIds.indexOf(id);
			$scope.selectIds.splice(idx, 1);//删除 
		}
	}

	//json字符串提取转换
	$scope.jsonToString = function (jsonString, key) {
		var json = JSON.parse(jsonString);
		var value = "";
		for (var i = 0; i < json.length; i++) {
			value += json[i][key] + ",";
		}
		//substr() 方法可在字符串中抽取从 start 下标开始的指定数目的字符。
		return value.substr(0, value.length - 1);
	}

	//在list集合中根据key查找对象
	$scope.searchObjectByKey = function (list, key, value) {
		for (var i = 0; i < list.length; i++) {
			if (list[i][key] == value) {
				return list[i];
			}
		}
		return null;
	}

});	