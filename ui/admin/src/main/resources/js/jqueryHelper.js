function showModalDialogue(containerId,title,resizable, draggable) {
	
	if(draggable==null)
		draggable=true;
	
	$(function() {
		$("#"+containerId).dialog({
			modal: true,
			title: title,
			resizable: resizable,
			draggable: draggable
		});
		
	});
	
	}
	
function showModalButtonCloseDialogue(containerId,title,resizable,draggable,width,height) {

		if(draggable==null)
			draggable=true;
		if(width==null || width<=0)
			width=300;
		if(height==null || height<=0)
			height=250;

		$(function() {
		$("#"+containerId).dialog({
			modal: true,
			title: title,
			resizable: resizable,
			draggable: draggable,
			buttons: [{
        		text: "Ok",
        		click: function() { $(this).dialog("close"); }
    		}],
    		width: width,
    		height: height
		});
		
	});
	
	}
