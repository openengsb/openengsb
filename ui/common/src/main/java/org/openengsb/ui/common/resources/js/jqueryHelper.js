/*
 * Licensed to the Austrian Association for Software Tool Integration (AASTI)
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. The AASTI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

 $(function() {
 
   $(".ddTrigger").click(function() {
   
     var $actList = $(this).find(".dropDownContainer");
     
     var callback = function() {
       $actList.toggle("slide",{direction: 'up'}, 100);
       $actList.parent().parent().toggleClass('activeTrigger');
     };
     
     if($(".ddTrigger .dropDownContainer:visible").not($actList).length > 0) {
       $(".ddTrigger .dropDownContainer:visible").not($actList).hide("slide",{direction: 'up'}, 100, callback);
     } else {
        callback();
      } 
   
   });
 
 });
 
$(function() {

  $(".dropdownTrigger").click(function() {
        var $actList = $(this).find("ul");

        var callback = function() {
            $actList.toggle("slide",{direction: 'up'}, 100);
        };        

        if($(".dropdownTrigger ul:visible").not($actList).length > 0) {
            $(".dropdownTrigger ul:visible").not($actList).hide("slide",{direction: 'up'}, 100, callback);
        } else {
            callback();
        }
    });
  
$(function () {

	$("table.dataTable > tr").click(function(){
		alert('serwas');
		$actRow.find(".inlistButton").toggleClass(".hidden");
	});
	
}); 

$("body").click(function(event) {
var $target = $(event.target);

if($target.is(":not(.dropdownTrigger, .dropdownTrigger *)")) {
    $(".dropdownTrigger ul:visible").hide("slide",{direction: 'up'}, 100);
    }
});

 });
 
 $(function() {
 
   $(".showUserDialog").click(function () {
     $("#userDialogue").dialog({
            modal: true,
            title: "Create user",
            resizable: false,
            draggable: false,
            width:300,
            heigth: 200
        });
   }); 
 
 });

function showModalDialogue(containerId,title,resizable, draggable, width,height) {
	
	if(draggable==null)
		draggable=true;
	
	$(function() {
		$("#"+containerId).dialog({
			modal: true,
			title: title,
			resizable: resizable,
			draggable: draggable,
			width: width,
			height: height
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
	
function toggleContainerBySlideing(containerId,speed) {

	$("#"+containerId).toggle("slide",{direction: 'up'}, speed);

}
