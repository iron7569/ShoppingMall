//控制层 
app.controller('typeTemplateController', function ($scope, $controller, typeTemplateService, brandService, specificationService) {

	$controller('baseController', { $scope: $scope });//继承

	//读取列表数据绑定到表单中  
	$scope.findAll = function () {
		typeTemplateService.findAll().success(
			function (response) {
				$scope.list = response;
			}
		);
	}

	//分页
	$scope.findPage = function (page, rows) {
		typeTemplateService.findPage(page, rows).success(
			function (response) {
				$scope.list = response.rows;
				$scope.paginationConf.totalItems = response.total;//更新总记录数
			}
		);
	}

	//查询实体 
	$scope.findOne = function (id) {
		typeTemplateService.findOne(id).success(
			function (response) {
				$scope.entity = response;
				//响应内为属性值字符串格式,需转换为json格式封装
				//{"brandIds":"[{\"id\":5,\"text\":\"OPPO\"},{\"id\":10,\"text\":\"VIVO\"}]","customAttributeItems":"[{\"text\":\"小鲜肉代言\"},{\"text\":\"垃圾手机\"}]","id":40,"name":"蓝绿大厂","specIds":"[{\"id\":27,\"text\":\"网络\"},{\"id\":28,\"text\":\"手机屏幕尺寸\"},{\"id\":32,\"text\":\"机身内存\"}]"}
				$scope.entity.customAttributeItems = JSON.parse($scope.entity.customAttributeItems);
				$scope.entity.brandIds = JSON.parse($scope.entity.brandIds);
				$scope.entity.specIds = JSON.parse($scope.entity.specIds);
			}
		);
	}

	//保存 
	$scope.save = function () {
		var serviceObject;//服务层对象  				
		if ($scope.entity.id != null) {//如果有ID
			serviceObject = typeTemplateService.update($scope.entity); //修改  
		} else {
			serviceObject = typeTemplateService.add($scope.entity);//增加 
		}
		serviceObject.success(
			function (response) {
				if (response.success) {
					//重新查询 
					$scope.reloadList();//重新加载
				} else {
					alert(response.message);
				}
			}
		);
	}


	//批量删除 
	$scope.dele = function () {
		//获取选中的复选框			
		typeTemplateService.dele($scope.selectIds).success(
			function (response) {
				if (response.success) {
					$scope.reloadList();//刷新列表
					$scope.selectIds = [];
				}
			}
		);
	}

	$scope.searchEntity = {};//定义搜索对象 

	//搜索
	$scope.search = function (page, rows) {
		typeTemplateService.search(page, rows, $scope.searchEntity).success(
			function (response) {
				$scope.list = response.rows;
				$scope.paginationConf.totalItems = response.total;//更新总记录数
			}
		);
	}

	//品牌下拉框展示
	$scope.brandList = { data: [] };

	$scope.getBrandOptionList = function () {
		brandService.getBrandOptionList().success(
			function (response) {
				$scope.brandList = { data: response };
			}
		)
	}

	//规格下拉框展示
	$scope.specifaicationList = { data: [] };

	$scope.getSpecificationOptionList = function () {
		specificationService.getSpecificationOptionList().success(
			function (response) {
				$scope.specifaicationList = { data: response };
			}
		)
	}

	//增加行
	$scope.addTableRow = function () {
		$scope.entity.customAttributeItems.push({});
	}

	//删除行
	$scope.delRow = function (index) {
		$scope.entity.customAttributeItems.splice(index, 1);
	}
});	
