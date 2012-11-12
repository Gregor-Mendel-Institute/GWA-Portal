package com.gmi.nordborglab.browser.client.resources;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;

public interface MainResources extends ClientBundle{
	
	
	public interface MainStyle extends CssResource {
		String transplant_logo_footer();
		String eu_logo_footer();
		String fp7_logo_footer();
		String gmi_logo_footer();
		String arrow_down();
		String avatar();
		String logout();
		String profile();
		String button();
		String button_grey();
		String button_blue();
		String button_red();
		String cursor();
		String piechart();
		String earth();
		String columnchart();
		String motionchart();
		String chartActionRow();
		String chartIconContainer();
		String iconContainer();
		String iconItem();
		String iconContainer_right();
		String iconContainer_active();
		String plant_icon();
	}

	@Source("images/transplant_logo_small.png")
	ImageResource transplant_logo_footer();
	
	@Source("images/fp7_logo_small.png")
	ImageResource fp7_logo_footer();
	
	
	@Source("images/eu_logo_small.png")
	ImageResource eu_logo_footer();
	
	@Source("images/gmi_logo_small.png")
	ImageResource gmi_logo_footer();
	
	@Source("images/arrow_down.png")
	ImageResource arrow_down();
	
	@Source("images/avatar.png")
	ImageResource avatar();
	
	@Source("images/logout.png")
	ImageResource logout();
	
	@Source("images/profile.png")
	ImageResource profile();
	
	@Source("images/piechart.jpg")
	ImageResource piechart();
	
	@Source("images/earth.png")
	ImageResource earth();
	
	@Source("images/motionchart.png")
	ImageResource motionchart();
	
	@Source("images/columnchart.png")
	ImageResource columnchart();
	
	@Source("images/plant1.png")
	ImageResource plant1();
	
	@Source("images/plant2.png")
	ImageResource plant2();
	
	@Source("images/plant3.png")
	ImageResource plant3();
	
	@Source("style.css")
	MainStyle style();
}
