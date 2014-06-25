package com.konakart.app;

import com.konakart.db.KKBasePeer;
import java.util.List;
import com.workingdogs.village.Record;

public class CustomAddressFields {
	public static String[] getCities() {
		String[] cities = {};
		try {
			List<Record> records = KKBasePeer
					.executeQuery("SELECT cities_id AS id, cities_name AS city FROM cities");

			if (records == null || records.size() == 0) {
				return null;
			}
			cities = new String[records.size()];
			for (int i = 0; i < records.size(); i++) {
				cities[i] = records.get(i).getValue(2).asString();
			}
			return cities;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return cities;
	}

	public static String[] getPostalCodes(String cityName) {
		System.out.println("inside getpostalcodes():"+cityName);
		String[] codes = {};
		try {
			List<Record> records = KKBasePeer
					.executeQuery("SELECT DISTINCT(city_areas_zip_code) AS zipCode FROM city_areas WHERE city_id = (SELECT cities_id from cities WHERE cities_name = "
							+ "'"+ cityName + "'" + ")");

			if (records == null || records.size() == 0) {
				return null;
			}
			codes = new String[records.size()];
			for (int i = 0; i < records.size(); i++) {
				codes[i] = records.get(i).getValue(1).toString();
			}
			return codes;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return codes;
	}

}
