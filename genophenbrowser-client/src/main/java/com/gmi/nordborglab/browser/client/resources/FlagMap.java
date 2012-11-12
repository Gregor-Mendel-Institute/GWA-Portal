package com.gmi.nordborglab.browser.client.resources;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.resources.client.ImageResource;
import com.google.inject.Inject;

public class FlagMap {

	private static Map<String,ImageResource> map; 
	
	@Inject
	public FlagMap(IconResources resources) {
		if (map == null) {
			map = new HashMap<String, ImageResource>();
			map.put("ARM", resources.flag_am());
			map.put("AT", resources.flag_at());
			map.put("AUT", resources.flag_at());
			map.put("AZE", resources.flag_az());
			map.put("BEL", resources.flag_be());	
			map.put("BUL", resources.flag_bg());
			map.put("CAN", resources.flag_ca());
			map.put("CHN", resources.flag_cn());
			map.put("CPV", resources.flag_cv());
			map.put("CRO", resources.flag_hr());
			map.put("CZE", resources.flag_cz());
			map.put("DEN", resources.flag_dk());
			map.put("ES", resources.flag_ee());
			map.put("ESP", resources.flag_es());
			map.put("FIN", resources.flag_fi());
			map.put("FR", resources.flag_fr());
			map.put("FRA", resources.flag_fr());
			map.put("GEO", resources.flag_ge());
			map.put("GER", resources.flag_de());
			map.put("GR", resources.flag_gr());
			map.put("IND", resources.flag_in());
			map.put("IRL", resources.flag_ie());
			map.put("ITA", resources.flag_it());
			map.put("JPN", resources.flag_jp());
			map.put("KAZ", resources.flag_kz());
			map.put("KGZ", resources.flag_kg());
			map.put("LIB", resources.flag_lb());
			map.put("LTU", resources.flag_lb());
			map.put("MAR", resources.flag_ma());
			map.put("NED", resources.flag_nl());
			map.put("NOR", resources.flag_no());
			map.put("NZL", resources.flag_nz());
			map.put("POL", resources.flag_pl());
			map.put("POR", resources.flag_pt());
			map.put("ROU", resources.flag_ru());
			map.put("RUS", resources.flag_ru());
			map.put("SRB", resources.flag_si());
			map.put("SUI", resources.flag_ch());
			map.put("SWE", resources.flag_se());
			map.put("TJK", resources.flag_tz());
			map.put("TUR", resources.flag_tr());
			map.put("TZ", resources.flag_tz());
			map.put("UK", resources.flag_gb());
			map.put("UKR", resources.flag_ua());
			map.put("UNK", resources.flag_UNK());
			map.put("USA", resources.flag_us());
			map.put("UZB", resources.flag_uz());
		}
	}
	
	public Map<String,ImageResource> getMap() {
		return map;
	}
}
