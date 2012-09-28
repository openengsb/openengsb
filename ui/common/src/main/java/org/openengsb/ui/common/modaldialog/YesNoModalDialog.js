displayYesNoModalDialgog = function(id, closeOnEscape, draggable, height, width,
		minHeight, minWidth, maxHeight, maxWidth, resizable, title, yesTitle, noTitle) {
	
	$("#"+containerId).dialog({
		modal: true,
		closeOnEscape: closeOnEscape
		title: title,
		resizable: resizable,
		draggable: draggable,
		width: width,
		height: height,
		minHeight: minHeight,
		maxHeight: maxHeight,
		minWidth: minWidth,
		maxWidth: maxWidth
		buttons: [
		    {
			    text: yesTitle,
		        click: displayYesNoModalDialgog_YesCB();
		    }
	    ]
	});
}