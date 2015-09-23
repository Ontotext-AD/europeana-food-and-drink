<!DOCTYPE html>
<HTML>
<HEAD>
	<%--<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>--%>
	<%--<c:set var="contextPath" value="${pageContext.request.contextPath}"/>--%>


	<TITLE>Europeana Food and Drink Category Tool</TITLE>
	<meta http-equiv="content-type" content="text/html; charset=UTF-8">
	<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/demo.css" type="text/css">
	<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/zTreeStyle/zTreeStyle.css" type="text/css">
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/jquery-1.11.3.min.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/jquery.ztree.core-3.5.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/jquery.ztree.excheck-3.5.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/jquery.ztree.exedit-3.5.js"></script>
	<SCRIPT type="text/javascript">
		<!--
		var setting = {
			view: {
				selectedMulti: false
			},
			edit: {
				enable: true,
				showRemoveBtn: true,
				showRenameBtn: false
			},
			data: {
				keep: {
					parent:false,
					leaf:false
				},
				simpleData: {
					enable: true
				}
			},
			callback: {
				beforeDrag: beforeDrag,
				beforeRemove: beforeRemove,
				onRemove: onRemove
			}
		};

		var zNodes =[
			{ id:1, pId:0, name:"parent node 1", open:true},
			{ id:11, pId:1, name:"leaf node 1-1"},
			{ id:12, pId:1, name:"leaf node 1-2"},
			{ id:13, pId:1, name:"leaf node 1-3"},
			{ id:2, pId:0, name:"parent node 2", open:true},
			{ id:21, pId:2, name:"leaf node 2-1"},
			{ id:22, pId:2, name:"leaf node 2-2"},
			{ id:23, pId:2, name:"leaf node 2-3"},
			{ id:3, pId:0, name:"parent node 3", open:true },
			{ id:31, pId:3, name:"leaf node 3-1"},
			{ id:32, pId:3, name:"leaf node 3-2"},
			{ id:33, pId:3, name:"leaf node 3-3"}
		];
		var log, className = "dark";
		function beforeDrag(treeId, treeNodes) {
			return false;
		}
		function beforeRemove(treeId, treeNode) {
			className = (className === "dark" ? "":"dark");
			showLog("[ "+getTime()+" beforeRemove ]&nbsp;&nbsp;&nbsp;&nbsp; " + treeNode.name);
//			return confirm("Confirm delete node '" + treeNode.name + "' it?");
		}
		function onRemove(e, treeId, treeNode) {

			$.ajax({
				url: "/tree/remove?category=http://dbpedia.org/resource/" + treeNode.name
			}).done(function (data) {
				if(data.length != 0) alert("Exception occur");
			});

			showLog("[ "+getTime()+" onRemove ]&nbsp;&nbsp;&nbsp;&nbsp; " + treeNode.name);
		}
		function beforeRename(treeId, treeNode, newName) {
			if (newName.length == 0) {
				alert("Node name can not be empty.");
				var zTree = $.fn.zTree.getZTreeObj("treeDemo");
				setTimeout(function(){zTree.editName(treeNode)}, 10);
				return false;
			}
			return true;
		}
		function showLog(str) {
			if (!log) log = $("#log");
			log.append("<li class='"+className+"'>"+str+"</li>");
			if(log.children("li").length > 8) {
				log.get(0).removeChild(log.children("li")[0]);
			}
		}
		function getTime() {
			var now= new Date(),
					h=now.getHours(),
					m=now.getMinutes(),
					s=now.getSeconds(),
					ms=now.getMilliseconds();
			return (h+":"+m+":"+s+ " " +ms);
		}

		var newCount = 1;
		function add(e) {
			var zTree = $.fn.zTree.getZTreeObj("treeDemo"),
					isParent = e.data.isParent,
					nodes = zTree.getSelectedNodes(),
					treeNode = nodes[0];
			if (treeNode) {
				treeNode = zTree.addNodes(treeNode, {id:(100 + newCount), pId:treeNode.id, isParent:isParent, name:"new node" + (newCount++)});
			} else {
				treeNode = zTree.addNodes(null, {id:(100 + newCount), pId:0, isParent:isParent, name:"new node" + (newCount++)});
			}
			if (treeNode) {
				zTree.editName(treeNode[0]);
			} else {
				alert("Leaf node is locked and can not add child node.");
			}
		};
		function edit() {
			var zTree = $.fn.zTree.getZTreeObj("treeDemo"),
					nodes = zTree.getSelectedNodes(),
					treeNode = nodes[0];
			if (nodes.length == 0) {
				alert("Please select one node at first...");
				return;
			}
			zTree.editName(treeNode);
		};
		function remove(e) {
			var zTree = $.fn.zTree.getZTreeObj("treeDemo"),
					nodes = zTree.getSelectedNodes(),
					treeNode = nodes[0];
			if (nodes.length == 0) {
				alert("Please select one node at first...");
				return;
			}
			var callbackFlag = $("#callbackTrigger").attr("checked");
			zTree.removeNode(treeNode, callbackFlag);
		};
		function clearChildren(e) {
			var zTree = $.fn.zTree.getZTreeObj("treeDemo"),
					nodes = zTree.getSelectedNodes(),
					treeNode = nodes[0];
			if (nodes.length == 0 || !nodes[0].isParent) {
				alert("Please select one parent node at first...");
				return;
			}
			zTree.removeChildNodes(treeNode);
		};

		var counter = 2;

		$(document).ready(function(){
			$.ajax({
				url: "/tree/jdummy?category=http://dbpedia.org/resource/Category:${category}"
			}).done(function (data) {
				zNodes = createDummy(data, null);
				$.fn.zTree.init($("#treeDemo"), setting, zNodes);
				$("#addParent").bind("click", {isParent:true}, add);
				$("#addLeaf").bind("click", {isParent:false}, add);
				$("#edit").bind("click", edit);
				$("#remove").bind("click", remove);
				$("#clearChildren").bind("click", clearChildren);


			});


			$("#treeDemo_1_a").promise().done(function(){
				var myChecker = setInterval(function () {
					if ($("#treeDemo_1_a").length > 0) {
						$("#treeDemo_1_span").dblclick();
						$("#treeDemo_1_span").bind('click', function(){
							attack();
						});
						$("#treeDemo_1_span").click();
						clearInterval(myChecker);

					}
					else {
					}
				}, 500);

			});

		});

		function attack(){
			$('#treeDemo_1_ul').each(function(){
				$(this).find('li').each(function(){
					var current = $(this);
					var ch = current.find('a').children();

//					console.log(ch.text());
					if(ch.text().search('articles:') > 0) {
						var icon = ch[0];
						$(icon).attr('class', 'button ico_close');
					}
				});
			});
		}


		function createDummy(obj, parentNodeId) {
			var arr = [];
			var arrCount = 0;
			var children = obj.children;
			var localArticles = obj.localArticles;
			if (parentNodeId == null) {
				arr[arrCount++] = {id: obj.treeLevel + 1, pId: obj.treeLevel, name: obj.prefLabel + " , articles:" + obj.descArticleCount };
				$.each(children, function(i, val){
					arr[arrCount++] = {id: counter++, pId: obj.treeLevel + 1, name: processCategoryTitle(val.uri.localName) + ", articles:" + val.artCount , click: "newNodes(this)"};
				})
				$.each(localArticles, function(i, val){
					arr[arrCount++] = {id: counter++, pId: obj.treeLevel + 1, name: removeUnderscores(val.localName)};
				})

			}
			else {
				var children = obj.children;
				$.each(children, function(i, val){
					arr[arrCount++] = {id: counter++, pId: parentNodeId, name: processCategoryTitle(val.uri.localName) + ", articles:" + val.artCount , click: "newNodes(this)"};
				})
				$.each(localArticles, function(i, val){
					arr[arrCount++] = {id: counter++, pId: parentNodeId, name: removeUnderscores(val.localName)};
				})
			}

			return arr;
		};

		function processCategoryTitle(obj) {
			var newTitle = obj.split(':')[1];
			return newTitle.replace(/_/g, ' ');
		//	return newTitle;
		}

		function revertCategoryTitle(obj) {
			var newTitle = obj.split(',');
			var category = newTitle[0];
			return category.replace(/ /g, '_') + ', ' + newTitle[1];
		}

		function removeUnderscores(obj) {
			return  obj.replace(/_/g, ' ');
		}

var nodeID;
		function newNodes(objId) {
			nodeID = objId.id;
			var title = revertCategoryTitle(objId.title);


			var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
			var selectedNode = treeObj.getNodeByTId($(objId).parent().attr('id'));
			if(selectedNode.isParent != undefined && !selectedNode.isParent) {
				$.ajax({
					url: "/tree/jdummy?category=http://dbpedia.org/resource/Category:" + title
				}).done(function (data) {
					zNod = createDummy(data, selectedNode.id );
					zNod = treeObj.addNodes(treeObj.getSelectedNodes()[0], zNod, false)
					attack();
				});
			} else {
				treeObj.expandNode(selectedNode, true, true, true)
			};
		};

		//-->
	</SCRIPT>
</HEAD>

<BODY>

<div class="content_wrap">
	<div class="zTreeDemoBackground left">
		<ul id="treeDemo" class="ztree"></ul>
	</div>

</div>
</BODY>
</HTML>