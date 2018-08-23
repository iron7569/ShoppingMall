//控制层 
app.controller('goodsController', function ($scope, $controller, goodsService, uploadService) {

	$controller('baseController', { $scope: $scope });//继承

	//读取列表数据绑定到表单中  
	$scope.findAll = function () {
		goodsService.findAll().success(
			function (response) {
				$scope.list = response;
			}
		);
	}

	//分页
	$scope.findPage = function (page, rows) {
		goodsService.findPage(page, rows).success(
			function (response) {
				$scope.list = response.rows;
				$scope.paginationConf.totalItems = response.total;//更新总记录数
			}
		);
	}

	//查询实体 
	$scope.findOne = function (id) {
		goodsService.findOne(id).success(
			function (response) {
				$scope.entity = response;
			}
		);
	}

	//保存 
	$scope.add = function () {

		//获取文本编辑器里的内容,存储到entity对象中
		$scope.entity.goodsDesc.introduction = editor.html();

		goodsService.add($scope.entity).success(
			function (response) {
				if (response.success) {
					//清空
					alert("添加成功!")
					$scope.entity = {};
					editor.html('');
				} else {
					alert(response.message);
				}
			}
		);
	}


	//批量删除 
	$scope.dele = function () {
		//获取选中的复选框			
		goodsService.dele($scope.selectIds).success(
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
		goodsService.search(page, rows, $scope.searchEntity).success(
			function (response) {
				$scope.list = response.rows;
				$scope.paginationConf.totalItems = response.total;//更新总记录数
			}
		);
	}

	//文件图片上传
	$scope.image_entity = { url: "", color: "" }
	$scope.uploadFile = function () {
		uploadService.fileUpload().success(
			function (response) {
				if (response.success) {
					$scope.image_entity.url = response.message;
				} else {
					alert(response.message)
				}
			})
	}

	//添加图片信息到列表
	$scope.entity = { goods: {}, goodsDesc: { itemImages: [] } }
	$scope.add_image_entity = function () {
		if ($scope.image_entity != { url: "", color: "" }) {
			$scope.entity.goodsDesc.itemImages.push($scope.image_entity);
		}
	}

	//移除图片列表
	$scope.remove_image_entity = function (index) {
		$scope.entity.goodsDesc.itemImages.splice(index, 1);
	}
});	
