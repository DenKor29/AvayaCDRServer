function timeConverter(strdate)
{
 if (strdate.length != 19) return 0;
  
  
  var date  = parseInt(strdate.substr(0,2));
  var month = parseInt(strdate.substr(3,2))-1;
  var year  = parseInt(strdate.substr(6,4));
  var hour  = parseInt(strdate.substr(11,2));
  var min   = parseInt(strdate.substr(14,2));
  var sec   = parseInt(strdate.substr(17,2));
  
  var a = new Date(year,month,date,hour,min,sec);
  return a.getTime()/1000;
}

function parseDateFormat(strdate)
{
 if (strdate.length != 19) return false;
 if (strdate.substr(2,1) != '.') return false;
 if (strdate.substr(5,1) != '.') return false;
 if (strdate.substr(10,1) != ' ') return false;
 if (strdate.substr(13,1) != ':') return false;
 if (strdate.substr(16,1) != ':') return false;
  
 return true;
}

function tablesorter(case_sensitive) {
  this.sort_case_sensitive = case_sensitive;
    }

tablesorter.prototype.sort_numbers = function (a, b) {
    return a - b;
}

tablesorter.prototype.sort_insensitive = function (a, b) {
    var anew = a.toLowerCase();
    var bnew = b.toLowerCase();
    if (anew < bnew) return -1;
    if (anew > bnew) return 1;
    return 0;
}

tablesorter.prototype.sort_sensitive = function (a, b) {
    if (a < b) return -1;
    if (a > b) return 1;
    return 0;
}

tablesorter.prototype.sort_date = function (a, b) {
	
	var anew = timeConverter(a);
    var bnew = timeConverter(b);
   
    if (anew < bnew) return -1;
    if (anew > bnew) return 1;
    	
	return 0;
    
}

 


tablesorter.prototype._sort = function (a, b) {
	    
	var a = a[0];
    var b = b[0];
    var _a = (a + '').replace(/,/, '.').trim();
    var _b = (b + '').replace(/,/, '.').trim();
    if (parseDateFormat(_a) && parseDateFormat(_b)) return TableSorter.sort_date(a, b);
    else if (parseInt(_a) && parseInt(_b)) return TableSorter.sort_numbers(parseInt(_a), parseInt(_b));
    else if (!this.sort_case_sensitive) return TableSorter.sort_insensitive(a, b);
    else return TableSorter.sort_sensitive(a, b);

}

tablesorter.prototype.getConcatenedTextContent = function (node) {
    var _result = "";
    if (node == null) {
        return _result;
    }
    var childrens = node.childNodes;
    var i = 0;
    while (i < childrens.length) {
        var child = childrens.item(i);
        switch (child.nodeType) {
            case 1: // ELEMENT_NODE
            case 5: // ENTITY_REFERENCE_NODE
                _result += this.getConcatenedTextContent(child);
                break;
            case 3: // TEXT_NODE
            case 2: // ATTRIBUTE_NODE
            case 4: // CDATA_SECTION_NODE
                _result += child.nodeValue;
                break;
            case 6: // ENTITY_NODE
            case 7: // PROCESSING_INSTRUCTION_NODE
            case 8: // COMMENT_NODE
            case 9: // DOCUMENT_NODE
            case 10: // DOCUMENT_TYPE_NODE
            case 11: // DOCUMENT_FRAGMENT_NODE
            case 12: // NOTATION_NODE
            // skip
            break;
        }
        i++;
    }
    return _result;
}
tablesorter.prototype.sort_table = function (targ) 
{
    var el = targ;

    while (el.tagName.toLowerCase() != "td") el = el.parentNode;

    var a = new Array();
    var name = el.lastChild.nodeValue;
    var dad = el.parentNode;
    var table = dad.parentNode.parentNode;
    
	var up = table.up; // no set/getAttribute!
	
    var node, arrow, curcol;
    for (var i = 0; (node = dad.getElementsByTagName("td").item(i)); i++) {
        if (node.lastChild.nodeValue == name){
            curcol = i;
            if (node.className == "curcol"){
                arrow = node.firstChild;
                table.up = Number(!up);
            }else{
                node.className = "curcol";
                arrow = node.insertBefore(document.createElement("span"),node.firstChild);
                arrow.appendChild(document.createTextNode(""));
                table.up = 0;
            }
            arrow.innerHTML=((table.up==0)?"&#8595;":"&#8593;")+"&nbsp;";
        }else{
            if (node.className == "curcol"){
                node.className = "";
                if (node.firstChild) node.removeChild(node.firstChild);
            }
        }
    }

    var tbody = table.getElementsByTagName("tbody").item(0);
    for (var i = 0; (node = tbody.getElementsByTagName("tr").item(i)); i++) {
        a[i] = new Array();
        a[i][0] = this.getConcatenedTextContent(node.getElementsByTagName("td").item(curcol));
        a[i][1] = this.getConcatenedTextContent(node.getElementsByTagName("td").item(1));
        a[i][2] = this.getConcatenedTextContent(node.getElementsByTagName("td").item(0));
        a[i][3] = node;
    }

    a.sort(this._sort);

    if (table.up) a.reverse();

    for (var i = 0; i < a.length; i++) {
        tbody.appendChild(a[i][3]);
    }
}

tablesorter.prototype.sort_column = function (id_table,num,sort_up) 
{
	var targ = document.getElementById(id_table);
	
	var thead = targ.getElementsByTagName("thead")[0];
	var e1 = thead.getElementsByTagName("td")[num];
	
	//Прямая сортировка
	this.sort_table(e1);
	
	//Обратная сортировка
	if (sort_up == -1) this.sort_table(e1);
	
}

tablesorter.prototype.sort_event = function (e) 
{
    var el = window.event ? window.event.srcElement : e.currentTarget;
	TableSorter.sort_table(el);
}



tablesorter.prototype.click_elem = function (elem)
{
        var evt = document.createEvent("MouseEvents");
		evt.initMouseEvent("click", false, false, window, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, elem);
        elem.dispatchEvent(evt);
		
 }

tablesorter.prototype.init = function () 
{
		
    for (var j = 0; (thead = document.getElementsByTagName("thead").item(j)); j++) 
	{
        var node;
        for (var i = 0; (node = thead.getElementsByTagName("td").item(i)); i++) 
		{
            node.addEventListener("click", this.sort_event, false);
            node.title = "Нажмите на заголовок, чтобы отсортировать колонку";
        }
        thead.parentNode.up = 0;
    
    }
	
}

var TableSorter = new tablesorter(false);
TableSorter.init();
