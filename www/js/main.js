document.getElementById("divDate").addEventListener('click', anichange,false);
document.getElementById("divNumber").addEventListener('click', anichange,false);
$("#table_cdr").trigger("update");
TableSorter.sort_column("table_cdr",1,1);

function anichange() 
{
  var objName = "";
  switch (this.id)
	{
		case "divDate": objName = "#divIdDate";break;
		case "divNumber": objName = "#divIdNumber";break;
		default:return false;
	};
	
 if ( $(objName).css('display') == 'none' ) 
 {
 $(objName).animate({height: 'show'}, 400);
 } else 
 {
 $(objName).animate({height: 'hide'}, 200);
 };
return false;
 }
