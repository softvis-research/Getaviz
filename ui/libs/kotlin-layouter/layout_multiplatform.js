(function (root, factory) {
  if (typeof define === 'function' && define.amd)
    define(['exports', 'kotlin'], factory);
  else if (typeof exports === 'object')
    factory(module.exports, require('kotlin'));
  else {
    if (typeof kotlin === 'undefined') {
      throw new Error("Error loading module 'layout_multiplatform'. Its dependency 'kotlin' was not found. Please, check whether 'kotlin' is loaded prior to 'layout_multiplatform'.");
    }root.layout_multiplatform = factory(typeof layout_multiplatform === 'undefined' ? {} : layout_multiplatform, kotlin);
  }
}(this, function (_, Kotlin) {
  'use strict';
  var Kind_CLASS = Kotlin.Kind.CLASS;
  var Comparable = Kotlin.kotlin.Comparable;
  var Unit = Kotlin.kotlin.Unit;
  var emptyList = Kotlin.kotlin.collections.emptyList_287e2$;
  var ArrayList_init = Kotlin.kotlin.collections.ArrayList_init_287e2$;
  var sortedDescending = Kotlin.kotlin.collections.sortedDescending_exjks8$;
  var first = Kotlin.kotlin.collections.first_2p1efm$;
  var to = Kotlin.kotlin.to_ujzrz7$;
  var Pair = Kotlin.kotlin.Pair;
  var ensureNotNull = Kotlin.ensureNotNull;
  var collectionSizeOrDefault = Kotlin.kotlin.collections.collectionSizeOrDefault_ba2ldo$;
  var ArrayList_init_0 = Kotlin.kotlin.collections.ArrayList_init_ww73n8$;
  var JsMath = Math;
  var sortedWith = Kotlin.kotlin.collections.sortedWith_eknfly$;
  var wrapFunction = Kotlin.wrapFunction;
  var Comparator = Kotlin.kotlin.Comparator;
  CityRectangle.prototype = Object.create(Rectangle.prototype);
  CityRectangle.prototype.constructor = CityRectangle;
  function Node(id, name, x, y, width, length, children, isDistrict) {
    if (x === void 0)
      x = 0.0;
    if (y === void 0)
      y = 0.0;
    if (width === void 0)
      width = 1.0;
    if (length === void 0)
      length = 1.0;
    if (children === void 0) {
      children = emptyList();
    }if (isDistrict === void 0)
      isDistrict = children.isEmpty();
    this.id = id;
    this.name = name;
    this.x = x;
    this.y = y;
    this.width = width;
    this.length = length;
    this.children = children;
    this.isDistrict = isDistrict;
  }
  Object.defineProperty(Node.prototype, 'centerX', {
    configurable: true,
    get: function () {
      return this.x + this.width / 2;
    }
  });
  Object.defineProperty(Node.prototype, 'centerY', {
    configurable: true,
    get: function () {
      return this.y + this.length / 2;
    }
  });
  Node.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'Node',
    interfaces: []
  };
  function Rectangle(width, length, x, y) {
    if (width === void 0)
      width = 1.0;
    if (length === void 0)
      length = 1.0;
    if (x === void 0)
      x = 0.0;
    if (y === void 0)
      y = 0.0;
    this.width = width;
    this.length = length;
    this.x = x;
    this.y = y;
    this.area = this.length * this.width;
    this.maxX = this.x + this.width;
    this.maxY = this.y + this.length;
    this.centerX = this.x + this.width / 2;
    this.centerY = this.y + this.length / 2;
  }
  Rectangle.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'Rectangle',
    interfaces: []
  };
  function CityRectangle(node, width, length, x, y) {
    if (x === void 0)
      x = 0.0;
    if (y === void 0)
      y = 0.0;
    Rectangle.call(this, width, length, x, y);
    this.node = node;
  }
  CityRectangle.prototype.compareTo_11rb$ = function (other) {
    var tmp$, tmp$_0;
    var tmp$_1;
    var $receiver = Kotlin.compareTo(this.area, other.area);
    if ((tmp$ = $receiver !== 0 ? $receiver : null) != null)
      tmp$_1 = tmp$;
    else {
      var $receiver_0 = Kotlin.compareTo(this.width, other.width);
      tmp$_1 = $receiver_0 !== 0 ? $receiver_0 : null;
    }
    return (tmp$_0 = tmp$_1) != null ? tmp$_0 : Kotlin.compareTo(this.node.name, other.node.name);
  };
  CityRectangle.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'CityRectangle',
    interfaces: [Comparable, Rectangle]
  };
  function KDTreeNode(rectangle) {
    this.rectangle = rectangle;
    this.leftChild = null;
    this.rightChild = null;
    this.occupied = false;
  }
  KDTreeNode.prototype.getFittingNodes_vm2z6a$ = function (element) {
    var list = ArrayList_init();
    this.getFittingsNodesMutable_0(element, list);
    return list;
  };
  KDTreeNode.prototype.getFittingsNodesMutable_0 = function (element, list) {
    var tmp$, tmp$_0;
    if (!this.occupied && this.rectangle.length >= element.length && this.rectangle.width >= element.width) {
      list.add_11rb$(this);
    }(tmp$ = this.leftChild) != null ? (tmp$.getFittingsNodesMutable_0(element, list), Unit) : null;
    (tmp$_0 = this.rightChild) != null ? (tmp$_0.getFittingsNodesMutable_0(element, list), Unit) : null;
  };
  KDTreeNode.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'KDTreeNode',
    interfaces: []
  };
  var compareBy$lambda = wrapFunction(function () {
    var compareValues = Kotlin.kotlin.comparisons.compareValues_s00gnj$;
    return function (closure$selector) {
      return function (a, b) {
        var selector = closure$selector;
        return compareValues(selector(a), selector(b));
      };
    };
  });
  function LightMapLayouterConfig(buildingHorizontalGap, trimEpsilon, emptyDistrictSize) {
    this.buildingHorizontalGap = buildingHorizontalGap;
    this.trimEpsilon = trimEpsilon;
    this.emptyDistrictSize = emptyDistrictSize;
  }
  LightMapLayouterConfig.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'LightMapLayouterConfig',
    interfaces: []
  };
  function LightMapLayouterConfig_init($this) {
    $this = $this || Object.create(LightMapLayouterConfig.prototype);
    LightMapLayouterConfig.call($this, 3.0, 0.001, 5.0);
    return $this;
  }
  LightMapLayouterConfig.prototype.component1 = function () {
    return this.buildingHorizontalGap;
  };
  LightMapLayouterConfig.prototype.component2 = function () {
    return this.trimEpsilon;
  };
  LightMapLayouterConfig.prototype.component3 = function () {
    return this.emptyDistrictSize;
  };
  LightMapLayouterConfig.prototype.copy_yvo9jy$ = function (buildingHorizontalGap, trimEpsilon, emptyDistrictSize) {
    return new LightMapLayouterConfig(buildingHorizontalGap === void 0 ? this.buildingHorizontalGap : buildingHorizontalGap, trimEpsilon === void 0 ? this.trimEpsilon : trimEpsilon, emptyDistrictSize === void 0 ? this.emptyDistrictSize : emptyDistrictSize);
  };
  LightMapLayouterConfig.prototype.toString = function () {
    return 'LightMapLayouterConfig(buildingHorizontalGap=' + Kotlin.toString(this.buildingHorizontalGap) + (', trimEpsilon=' + Kotlin.toString(this.trimEpsilon)) + (', emptyDistrictSize=' + Kotlin.toString(this.emptyDistrictSize)) + ')';
  };
  LightMapLayouterConfig.prototype.hashCode = function () {
    var result = 0;
    result = result * 31 + Kotlin.hashCode(this.buildingHorizontalGap) | 0;
    result = result * 31 + Kotlin.hashCode(this.trimEpsilon) | 0;
    result = result * 31 + Kotlin.hashCode(this.emptyDistrictSize) | 0;
    return result;
  };
  LightMapLayouterConfig.prototype.equals = function (other) {
    return this === other || (other !== null && (typeof other === 'object' && (Object.getPrototypeOf(this) === Object.getPrototypeOf(other) && (Kotlin.equals(this.buildingHorizontalGap, other.buildingHorizontalGap) && Kotlin.equals(this.trimEpsilon, other.trimEpsilon) && Kotlin.equals(this.emptyDistrictSize, other.emptyDistrictSize)))));
  };
  function LightMapLayouter(config) {
    this.config = config;
  }
  LightMapLayouter.prototype.calculateWithVirtualRoot = function (nodes) {
    var virtualRoot = new Node('root', 'root', void 0, void 0, void 0, void 0, void 0, true);
    virtualRoot.children = nodes;
    var rootRectangle = this._arrangeChildren(virtualRoot);
    this._resolveAbsolutePositions(virtualRoot, 0.0, 0.0);
    return rootRectangle;
  };
  LightMapLayouter.prototype.calculate = function (node) {
    var rootRectangle = this._arrangeChildren(node);
    this._resolveAbsolutePositions(node, 0.0, 0.0);
    return rootRectangle;
  };
  LightMapLayouter.prototype._resolveAbsolutePositions = function (node, absoluteX, absoluteY) {
    node.x = node.x + absoluteX;
    node.y = node.y + absoluteY;
    var tmp$;
    tmp$ = node.children.iterator();
    while (tmp$.hasNext()) {
      var element = tmp$.next();
      this._resolveAbsolutePositions(element, node.x, node.y);
    }
  };
  LightMapLayouter.prototype._arrangeChildren = function (parent) {
    var tmp$, tmp$_0;
    if (parent.children.isEmpty()) {
      if (parent.isDistrict) {
        parent.width = this.config.emptyDistrictSize;
        parent.length = this.config.emptyDistrictSize;
        return new CityRectangle(parent, this.config.emptyDistrictSize, this.config.emptyDistrictSize);
      } else {
        return new CityRectangle(parent, parent.width + this.config.buildingHorizontalGap, parent.length + this.config.buildingHorizontalGap);
      }
    }var $receiver = parent.children;
    var destination = ArrayList_init_0(collectionSizeOrDefault($receiver, 10));
    var tmp$_1;
    tmp$_1 = $receiver.iterator();
    while (tmp$_1.hasNext()) {
      var item = tmp$_1.next();
      destination.add_11rb$(this._arrangeChildren(item));
    }
    var arrangedChildren = sortedDescending(destination);
    var maxRectangle = this._calculateMaxArea(arrangedChildren);
    var covRectangle = new Rectangle();
    var tree = new KDTreeNode(maxRectangle);
    tmp$ = arrangedChildren.iterator();
    while (tmp$.hasNext()) {
      var element = tmp$.next();
      var possibleNodes = tree.getFittingNodes_vm2z6a$(element);
      var tmp$_2 = this._mapElementsToPreserversExpanders(possibleNodes, element, covRectangle);
      var preservers = tmp$_2.component1()
      , expanders = tmp$_2.component2();
      if (preservers.isEmpty()) {
        tmp$_0 = first(expanders).first;
      } else {
        tmp$_0 = first(preservers).first;
      }
      var targetNode = tmp$_0;
      var fittedNode = this._trimNode(targetNode, element);
      fittedNode.occupied = true;
      covRectangle = this._expandCovrecToInclude(fittedNode.rectangle, covRectangle);
      this._transferCoordsToNode(fittedNode.rectangle, element.node);
    }
    var parentRectangle = new CityRectangle(parent, covRectangle.width + this.config.buildingHorizontalGap, covRectangle.length + this.config.buildingHorizontalGap);
    this._transferSizeToNode(covRectangle, parent);
    return parentRectangle;
  };
  LightMapLayouter.prototype._expandCovrecToInclude = function (newElement, oldCovrec) {
    var a = oldCovrec.width;
    var b = newElement.maxX - oldCovrec.x;
    var tmp$ = JsMath.max(a, b);
    var a_0 = oldCovrec.length;
    var b_0 = newElement.maxY - oldCovrec.y;
    return new Rectangle(tmp$, JsMath.max(a_0, b_0), oldCovrec.x, oldCovrec.y);
  };
  LightMapLayouter.prototype._transferCoordsToNode = function (sourceRectangle, targetNode) {
    targetNode.x = sourceRectangle.x + this.config.buildingHorizontalGap / 2;
    targetNode.y = sourceRectangle.y + this.config.buildingHorizontalGap / 2;
  };
  LightMapLayouter.prototype._transferSizeToNode = function (sourceRectangle, targetNode) {
    targetNode.width = sourceRectangle.width;
    targetNode.length = sourceRectangle.length;
  };
  LightMapLayouter.prototype._calculateMaxArea = function (children) {
    var tmp$;
    var widthSum = 0.0;
    var lengthSum = 0.0;
    tmp$ = children.iterator();
    while (tmp$.hasNext()) {
      var node = tmp$.next();
      widthSum += node.width;
      lengthSum += node.length;
    }
    var totalPadding = this.config.buildingHorizontalGap * children.size;
    widthSum += totalPadding;
    lengthSum += totalPadding;
    return new Rectangle(widthSum, lengthSum);
  };
  function LightMapLayouter$mapElementsToPreserversExpanders$lambda(it) {
    return it.second;
  }
  function LightMapLayouter$mapElementsToPreserversExpanders$lambda_0(it) {
    var x = it.second - 1;
    return JsMath.abs(x);
  }
  LightMapLayouter.prototype._mapElementsToPreserversExpanders = function (nodes, insertedElement, covrec) {
    var tmp$;
    var first = ArrayList_init();
    var second = ArrayList_init();
    tmp$ = nodes.iterator();
    while (tmp$.hasNext()) {
      var element = tmp$.next();
      if (element.rectangle.x + insertedElement.width <= covrec.maxX && element.rectangle.y + insertedElement.length <= covrec.maxY) {
        first.add_11rb$(element);
      } else {
        second.add_11rb$(element);
      }
    }
    var tmp$_0 = new Pair(first, second);
    var preserverList = tmp$_0.component1()
    , expanderList = tmp$_0.component2();
    var destination = ArrayList_init_0(collectionSizeOrDefault(preserverList, 10));
    var tmp$_1;
    tmp$_1 = preserverList.iterator();
    while (tmp$_1.hasNext()) {
      var item = tmp$_1.next();
      destination.add_11rb$(to(item, item.rectangle.area - insertedElement.area));
    }
    var preserverMap = sortedWith(destination, new Comparator(compareBy$lambda(LightMapLayouter$mapElementsToPreserversExpanders$lambda)));
    var destination_0 = ArrayList_init_0(collectionSizeOrDefault(expanderList, 10));
    var tmp$_2;
    tmp$_2 = expanderList.iterator();
    while (tmp$_2.hasNext()) {
      var item_0 = tmp$_2.next();
      var tmp$_3 = destination_0.add_11rb$;
      var a = item_0.rectangle.x + insertedElement.width;
      var b = covrec.maxX;
      var tmp$_4 = JsMath.max(a, b);
      var a_0 = item_0.rectangle.y + insertedElement.length;
      var b_0 = covrec.maxY;
      tmp$_3.call(destination_0, to(item_0, tmp$_4 / JsMath.max(a_0, b_0)));
    }
    var expanderMap = sortedWith(destination_0, new Comparator(compareBy$lambda(LightMapLayouter$mapElementsToPreserversExpanders$lambda_0)));
    return new Pair(preserverMap, expanderMap);
  };
  LightMapLayouter.prototype._trimNode = function (node, insertedElement) {
    var nodeRec = node.rectangle;
    var x = nodeRec.length - insertedElement.length;
    if (JsMath.abs(x) > this.config.trimEpsilon) {
      node.leftChild = new KDTreeNode(new Rectangle(nodeRec.width, insertedElement.length, nodeRec.x, nodeRec.y));
      node.rightChild = new KDTreeNode(new Rectangle(nodeRec.width, nodeRec.length - insertedElement.length, nodeRec.x, nodeRec.y + insertedElement.length));
      node.occupied = true;
      return this._trimNode(ensureNotNull(node.leftChild), insertedElement);
    } else {
      var x_0 = nodeRec.width - insertedElement.width;
      if (JsMath.abs(x_0) > this.config.trimEpsilon) {
        node.leftChild = new KDTreeNode(new Rectangle(insertedElement.width, nodeRec.length, nodeRec.x, nodeRec.y));
        node.rightChild = new KDTreeNode(new Rectangle(nodeRec.width - insertedElement.width, nodeRec.length, nodeRec.x + insertedElement.width, nodeRec.y));
        node.occupied = true;
        return this._trimNode(ensureNotNull(node.leftChild), insertedElement);
      } else {
        return node;
      }
    }
  };
  LightMapLayouter.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'LightMapLayouter',
    interfaces: []
  };
  var package$org = _.org || (_.org = {});
  var package$getaviz = package$org.getaviz || (package$org.getaviz = {});
  var package$generator = package$getaviz.generator || (package$getaviz.generator = {});
  var package$city = package$generator.city || (package$generator.city = {});
  var package$kotlin = package$city.kotlin || (package$city.kotlin = {});
  package$kotlin.Node = Node;
  package$kotlin.Rectangle = Rectangle;
  package$kotlin.CityRectangle = CityRectangle;
  package$kotlin.KDTreeNode = KDTreeNode;
  package$kotlin.LightMapLayouterConfig_init = LightMapLayouterConfig_init;
  package$kotlin.LightMapLayouterConfig = LightMapLayouterConfig;
  package$kotlin.LightMapLayouter = LightMapLayouter;
  Kotlin.defineModule('layout_multiplatform', _);
  return _;
}));

//# sourceMappingURL=layout_multiplatform.js.map
