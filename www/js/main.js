
function anichange(str) 
{
  var objName = "";
  
   switch (str)
	{
		case "divTblDate": objName = "#divIdTblDate";break;
		case "divTblNumber": objName = "#divIdTblNumber";break;
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
