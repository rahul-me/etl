package com.gcn.etl.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.gcn.etl.pojo.MissingPoint;

public class JsonToMap {
	public static Map<String, Object> convert(JSONObject json) {
		Map<String, Object> retMap = new HashMap<String, Object>();
		if (json != JSONObject.NULL) {
			retMap = toMap(json);
		}
		return retMap;	
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> toMap(JSONObject object) {
		Map<String, Object> map = new HashMap<String, Object>();
		Iterator<String> keysItr = object.keys();
		while (keysItr.hasNext()) {
			String key = keysItr.next();
			Object value = object.get(key);

			if (value instanceof JSONArray) {
				value = toList((JSONArray) value);
			}

			else if (value instanceof JSONObject) {
				value = toMap((JSONObject) value);
			}
			map.put(key, value);
		}
		return map;
	}

	private static Object toList(JSONArray array) {
		List<MissingPoint> list = new ArrayList<MissingPoint>();
		for (int i = 0; i < array.length(); i++) {
			Object value = array.get(i);
			if (value instanceof JSONArray) {
				value = toList((JSONArray) value);
			}
			else if (value instanceof JSONObject) {
				value = toMap((JSONObject) value);
			}
			list.add((MissingPoint) value);
		}
		return list;
	}
}
