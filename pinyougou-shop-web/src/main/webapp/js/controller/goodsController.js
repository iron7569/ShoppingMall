//控制层 
app.controller('goodsController', function ($scope, $controller, $location, goodsService, uploadService, itemCatService, typeTemplateService) {

    $controller('baseController', {$scope: $scope});//继承

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
        //$location内置服务,search()方法获取页面传过来参数数组
        //http://localhost:9102/admin/goods_edit.html#?id=149187842867968
        var id = $location.search()['id'];
        if (id) {
            goodsService.findOne(id).success(
                function (response) {
                    $scope.entity = response;
                    //文本编辑器内容回显
                    editor.html($scope.entity.goodsDesc.introduction);
                    //图片列表回显
                    $scope.entity.goodsDesc.itemImages = JSON.parse($scope.entity.goodsDesc.itemImages);
                    //扩展信息回显
                    $scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.entity.goodsDesc.customAttributeItems);
                    //规格信息回显
                    $scope.entity.goodsDesc.specificationItems = JSON.parse($scope.entity.goodsDesc.specificationItems);

                    //sku列表数据回显
                    for (var i = 0; i < $scope.entity.items.length; i++) {
                        $scope.entity.items[i].spec = JSON.parse($scope.entity.items[i].spec);
                    }

                }
            );
        }
    }

    //specName规格名称  optionName选项名称
    $scope.checkAttributeValue = function(specName,optionName){
        //调用方法
        var object = $scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems,"attributeName",specName);
        if (object==null){
            return false;
        }
        if(object.attributeValue.indexOf(optionName)>=0){
            return true;
        }else {
            return false;
        }
    }

    //保存
    $scope.save=function(){
        //获取文本编辑器里的内容,存储到entity对象中
        $scope.entity.goodsDesc.introduction = editor.html();

        var serviceObject;//服务层对象
        if($scope.entity.goods.id!=null){//如果有ID
            serviceObject=goodsService.update( $scope.entity ); //修改
        }else{
            serviceObject=goodsService.add( $scope.entity  );//增加
        }
        serviceObject.success(
            function(response){
                if(response.success){
                    //清空
                    alert("操作成功!")
                    window.location.href="goods.html"
                }else{
                    alert(response.message);
                }
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
    $scope.image_entity = {url: "", color: ""}
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
    $scope.entity = {goods: {}, goodsDesc: {itemImages: [], specificationItems: []}}
    $scope.add_image_entity = function () {
        if ($scope.image_entity != {url: "", color: ""}) {
            $scope.entity.goodsDesc.itemImages.push($scope.image_entity);
        }
    }

    //移除图片列表
    $scope.remove_image_entity = function (index) {
        $scope.entity.goodsDesc.itemImages.splice(index, 1);
    }

    //查询一级商品分类列表
    $scope.findItemCat1List = function () {
        itemCatService.findByParentId(0).success(
            function (response) {
                $scope.itemCat1List = response;
            })
    }

    //查询二级商品分类列表
    $scope.$watch('entity.goods.category1Id', function (newValue, oldValue) {
        if (newValue) {
            itemCatService.findByParentId(newValue).success(
                function (response) {
                    $scope.itemCat2List = response;
                })
        }
    })

    //查询三级商品分类列表
    $scope.$watch('entity.goods.category2Id', function (newValue, oldValue) {
        if (newValue) {
            itemCatService.findByParentId(newValue).success(
                function (response) {
                    $scope.itemCat3List = response;
                })
        }
    })

    //查询模板id
    $scope.$watch('entity.goods.category3Id', function (newValue, oldValue) {
        if (newValue) {
            itemCatService.findOne(newValue).success(
                function (response) {
                    $scope.entity.goods.typeTemplateId = response.typeId;
                })
        }
    })

    //查询品牌列表,根据模板id查模板表
    //查询规格信息列表
    $scope.$watch('entity.goods.typeTemplateId', function (newValue, oldValue) {
        if (newValue) {
            typeTemplateService.findOne(newValue).success(
                function (response) {
                    $scope.typeTemplate = response;
                    $scope.typeTemplate.brandIds = JSON.parse($scope.typeTemplate.brandIds);
                    //[{"text":"内存大小"},{"text":"颜色"}]
                    //以下代码与商品修改参数回显部分重复冲突,加判断解决冲突
                    if ($location.search()['id'] == null) {
                        $scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.typeTemplate.customAttributeItems);
                    }
                })
            //查询规格信息列表
            typeTemplateService.getSpecList(newValue).success(
                function (response) {
                    $scope.specList = response;
                })
        }
    })

    //更新选择的规格选项数据
    $scope.updateSpecAttribute = function ($event, name, value) {
        //[{"attributeName":"网络制式","attributeValue":["移动3G","移动4G"]},{"attributeName":"屏幕尺寸","attributeValue":["6寸","5.5寸"]}]
        var object = $scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems, "attributeName", name);

        if (object != null) {
            console.log(object);
            //判断勾选or取消勾选
            if ($event.target.checked) {
                object.attributeValue.push(value);
            } else {
                //移除
                object.attributeValue.splice(object.attributeValue.indexOf(value), 1);
            }
            //如果选项都取消了，将此条记录移除
            if (object.attributeValue.length == 0) {
                $scope.entity.goodsDesc.specificationItems.splice($scope.entity.goodsDesc.specificationItems.indexOf(object), 1);
            }

        } else {
            $scope.entity.goodsDesc.specificationItems.push({"attributeName": name, "attributeValue": [value]})
        }
    }

    //更新列表  生成sku列表,克隆方式
    $scope.createItemsList = function () {
        //根据页面表格的列属性初始化变量
        //spec:{"机身内存":"16G","网络":"联通3G"}
        //价格-price:0  库存-num:9999  是否启用-status:'0'  是否默认- isDefault:'0'
        $scope.entity.items = [{spec: {}, price: 0, num: 99999, status: '0', isDefault: '0'}];

        //根据勾选的规格详情数据生成列表
        var items = $scope.entity.goodsDesc.specificationItems;
        //[{"attributeName":"网络制式","attributeValue":["移动3G","移动4G"]},{"attributeName":"屏幕尺寸","attributeValue":["6寸","5.5寸"]}]
        for (var i = 0; i < items.length; i++) {
            //调用克隆方法
            $scope.entity.items = addRow($scope.entity.items, items[i].attributeName, items[i].attributeValue)
        }

    }

    //抽取方法生成列
    //参数:list----克隆前列表 初始为[{spec:{},price:0,num:99999,status:'0',isDefault:'0'}]
    //    columnName 列名..如( 网络制式 )   columnValues 列下面的值的集合 .. ["移动3G","移动4G"]
    addRow = function (list, columnName, columnValues) {
        //初始化返回列表
        var newList = [];
        //遍历列表
        for (var i = 0; i < list.length; i++) {
            // list[i] --- {spec:{},price:0,num:99999,status:'0',isDefault:'0'}
            var oldRow = list[i];
            //集合中添加数据
            for (var j = 0; j < columnValues.length; j++) {
                //深克隆
                var newRow = JSON.parse(JSON.stringify(oldRow));
                console.log("oldRow:" + oldRow);
                console.log("newRow:" + newRow);
                //添加属性和值
                newRow.spec[columnName] = columnValues[j];
                //添加到新的集合
                newList.push(newRow);
            }
        }
        return newList;
    }

    $scope.status = ["未审核", "已审核通过", "已驳回", "已关闭"];

    $scope.itemCatList = [];
    $scope.findItemCatList = function () {
        itemCatService.findAll().success(
            function (response) {
                for (var i = 0; i < response.length; i++) {
                    $scope.itemCatList[response[i].id] = response[i].name;
                }
            })
    }
});
