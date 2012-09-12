package org.openengsb.ui.common.modaldialog; 

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AbstractAjaxBehavior;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.PackageResourceReference;

/**
* Absolute base widget. This widget does nothing else than attaching
the required css
* <p/>
* TODO: nothing to test here since we can test the render head method
properly without integration tests :-(
* <p/>
* TODO: we extend from panel here since best that every widget can
set itself into it's own container as wished.
*/
public abstract class YesNoModalDialog extends Panel {
   
	private static final long serialVersionUID = 1L;

	private boolean closeOnEscape = true;
	private boolean draggable = true;
	private double width = 200.0;
	private double height = 150.0;
	private double minWidth = 200.0;
	private double maxWidth = 200.0;
	private double minHeight = 150.0;
	private double maxHeight = 150.0;
	private boolean resizable = false;
	private String title = "Modal Dialog";
	private String yesTitle = "No";
	private String noTitle = "Yes";
	
	
	public YesNoModalDialog(String id) {
       super(id);
       initializeBaseCss();
    }
	
	public YesNoModalDialog(String id, IModel<?> model) {
	       super(id, model);
	       initializeBaseCss();
	   }


   private final void initializeBaseCss() {
	   
       add(new AbstractBehavior() {
    	   private static final long serialVersionUID = 1L;

    	   @Override
    	   public void renderHead(final Component component, final IHeaderResponse response) {
    		   response.renderJavaScriptReference(new
    				   PackageResourceReference(YesNoModalDialog.class, "YesNoModalDialog.css"));
           }
       });
       add(new AbstractAjaxBehavior() {
    	   
    	   private static final long serialVersionUID = 1L;

    	    @Override
    	    public void renderHead(final Component component, final IHeaderResponse response)
    	    {
    	        super.renderHead(component, response);

    	        response.renderJavaScript(new StringBuilder()
                .append("displayYesNoModalDialgog_YesCB =")
                .append("$(this).dialog('close');\n")
                .append("}\n")
                .toString(), "myComponentCallbackMethod");
    	    }
  
    	    public void onRequest() {
    	    	YesNoModalDialog.this.onFailure(null);
    	    }
       });
       
               /*
       add(new AbstractAjaxBehavior() {
       private static final long serialVersionUID = 1L;

		@Override
           public void renderHead(IHeaderResponse response) {
               super.renderHead(response);
               response.renderJavascript(new StringBuilder()
                       .append("strangeMethodCallbackOnSuccess =
function () {\n")
                       .append("wicketAjaxGet(\"").append(getCallbackUrl())
                       .append("\");\n")
                       .append("}\n")
                       .toString(), "myComponentCallbackMethod");
           }

           public void onRequest() {
               ResourcexWidget.this.onSuccess();
           }
       });
       add(new Label("","").add()) */
   }

   public void showDialog() {
	   this.getOutputMarkupId();
	   add(new AbstractAjaxBehavior() {
		 
		private static final long serialVersionUID = 1L;

		@Override
		   public void renderHead(final Component component, final IHeaderResponse response)
		   {
		       super.renderHead(component, response);
		       response.renderJavaScript("displayYesNoModalDialgog(" +
		    		closeOnEscape + "," +
		    		draggable + "" +
		    		width + "" +
		       		height +"" +
		       		minWidth + "" +
		       		maxWidth + "" +
		       		minHeight + "" +
		       		maxHeight + "" +
		       		resizable + "" +
		       		title + "" +
		       		yesTitle + "" +
		       		noTitle + "" +
		       		");", "myScriptID");
		   }

		   @Override
		   public void onRequest() {
		}  
	   });
   }

   protected abstract void onSuccess(AjaxRequestTarget target);

   protected abstract void onFailure(AjaxRequestTarget target);
}