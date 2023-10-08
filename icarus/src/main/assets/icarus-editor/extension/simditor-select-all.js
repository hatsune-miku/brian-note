(function (root, factory) {
  if (typeof define === 'function' && define.amd) {
    // AMD. Register as an anonymous module unless amdModuleId is set
    define('simditor-select-all', ["jquery", "simditor"], function (a0, b1) {
      return (root['SimditorSelectAll'] = factory(a0, b1));
    });
  } else if (typeof exports === 'object') {
    // Node. Does not work with strict CommonJS, but
    // only CommonJS-like environments that support module.exports,
    // like Node.
    module.exports = factory(require("jquery"), require("simditor"));
  } else {
    root['SimditorSelectAll'] = factory(jQuery, Simditor);
  }
}(this, function ($, Simditor) {

  var SimditorSelectAll,
    extend = function (child, parent) { for (var key in parent) { if (hasProp.call(parent, key)) child[key] = parent[key]; } function ctor() { this.constructor = child; } ctor.prototype = parent.prototype; child.prototype = new ctor(); child.__super__ = parent.prototype; return child; },
    hasProp = {}.hasOwnProperty;

    SimditorSelectAll = (function (superClass) {
    extend(SimditorSelectAll, superClass);

    function SimditorSelectAll() {
      return SimditorSelectAll.__super__.constructor.apply(this, arguments);
    }

    SimditorSelectAll.prototype.name = 'selectall';
    SimditorSelectAll.prototype.icon = 'selectall';

    SimditorSelectAll.prototype.htmlTag = '';
    SimditorSelectAll.prototype.disableTag = '';
    SimditorSelectAll.prototype.shortcut = 'cmd+a';

    SimditorSelectAll.prototype._init = function () {
      if (this.editor.util.os.mac) {
        this.title = this.title + ' ( Cmd + a )';
      } else {
        this.title = this.title + ' ( Ctrl + a )';
        this.shortcut = 'ctrl+a';
      }
      return SimditorSelectAll.__super__._init.call(this);
    };

    SimditorSelectAll.prototype._activeStatus = function () {
      var active;
      active = document.queryCommandState('selectAll') === true;
      this.setActive(active);
      return this.active;
    };

    SimditorSelectAll.prototype.command = function () {
      document.execCommand('selectAll');
      return $(document).trigger('selectionchange');
    };

    return SimditorSelectAll;

  })(Simditor.Button);

  Simditor.Toolbar.addButton(SimditorSelectAll);

  return SimditorSelectAll;

}));
