package com.xdong.service;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.xdong.dao.RedisDao;
import com.xdong.validator.CustomJsonValidator;

@Service
public class RedisServiceImpl implements RedisService {

	@Autowired
	RedisDao redisDao;
	
	CustomJsonValidator customJsonValidator;
	
	public static final String SEP = "____";
	public static final String TYPE = "objectType";
	public static final String ID = "objectId";
	
	@Autowired
	public RedisServiceImpl(CustomJsonValidator customJsonValidator) {
		this.customJsonValidator = customJsonValidator;
	}
	
	@Override
	public JSONObject getPlan(String key) {
		// 
		JSONObject jo = new JSONObject();
		Set<String> keys = redisDao.getKeys("*" + key + "*");
		keys.remove(key);
		Map<Object, Object> map = redisDao.findMap(key);
		loadMap(jo, map);
		
		for(String edgeKey: keys) {
			Set subObjs = redisDao.findSet(edgeKey);
			String attribute = edgeKey.split(SEP)[2];
			if(subObjs.size() == 1) {
				String subKey = null;
				for(Object str: subObjs) {
					subKey = (String)str;
				}
				jo.put(attribute, getPlan(subKey));
			}
			else {
				JSONArray array = new JSONArray();
				jo.put(attribute, array);
				for(Object str: subObjs) {
					String subKey = (String)str;
					array.put(getPlan(subKey));
				}
			}
		}
		
		return jo;
	}
	
	private void loadMap(JSONObject jo, Map<Object, Object> map) {
		for(Map.Entry<Object, Object> entry : map.entrySet()) {
			String val = (String)entry.getValue();
			try {
				jo.put((String)entry.getKey(), Integer.parseInt(val));
			}
			catch(Exception e) {
				jo.put((String)entry.getKey(), val);
			}
		}
	}

	@Override
	public void postPlan(JSONObject jo) {
		
		//BFS
		Queue<JSONObject> queue = new LinkedList<>();
		queue.add(jo);
		while(!queue.isEmpty()) {
			JSONObject cur = queue.poll();
			String objectkey = cur.getString(TYPE) + SEP + cur.getString(ID);
			
			for(String attribute : cur.keySet()) {
				Object obj = cur.get(attribute);
				if(obj instanceof JSONObject) {
					JSONObject subObj = (JSONObject)obj;
					String edgeKey = objectkey + SEP + attribute;
					String subObjKey = subObj.getString(TYPE) + SEP + subObj.getString(ID);
					redisDao.insertSet(edgeKey, subObjKey);
					queue.offer((JSONObject)obj);
				}
				else if(obj instanceof JSONArray) {
					String edgeKey = objectkey + SEP + attribute;
					for(int i = 0; i < ((JSONArray)obj).length(); i++) {
						JSONObject subObj = ((JSONArray)obj).getJSONObject(i);
						String subObjKey = subObj.getString(TYPE) + SEP + subObj.getString(ID);
						redisDao.insertSet(edgeKey, subObjKey);
						queue.offer(subObj);
					}
				}
				else {
					redisDao.insertMap(objectkey, attribute, obj);
				}
			}
		}
	}
	

	@Override
	public void updatePlan(String key, String value) {
		// TODO Auto-generated method stub

	}

	@Override
	public JSONObject deletePlan(String key) {
		JSONObject jo = new JSONObject();
		Set<String> keys = redisDao.getKeys("*" + key + "*");
		keys.remove(key);
		Map<Object, Object> map = redisDao.findMap(key);
		loadMap(jo, map);
		redisDao.delete(key);
		
		for(String edgeKey: keys) {
			Set subObjs = redisDao.findSet(edgeKey);
			redisDao.delete(edgeKey);
			String attribute = edgeKey.split(SEP)[2];
			if(subObjs.size() == 1) {
				String subKey = null;
				for(Object str: subObjs) {
					subKey = (String)str;
				}
				jo.put(attribute, deletePlan(subKey));
			}
			else {
				JSONArray array = new JSONArray();
				jo.put(attribute, array);
				for(Object str: subObjs) {
					String subKey = (String)str;
					array.put(deletePlan(subKey));
				}
			}
		}
		return jo;
	}

	@Override
	public boolean validate(JSONObject jo) {
		return customJsonValidator.validate(jo);
	}

	@Override
	public boolean exist(String key) {
		Map map = redisDao.findMap(key);
		if(map == null || map.size() == 0)
			return false;
		return true;
	}

	@Override
	public void patchPlan(String key, JSONObject newjo) {
		for(String attribute : newjo.keySet()) {
			Object obj = newjo.get(attribute);
			String edgeKey = key + SEP + attribute;
			if(obj instanceof JSONObject) {
				JSONObject subObj = (JSONObject)obj;
				String subObjKey = subObj.getString(TYPE) + SEP + subObj.getString(ID);
				
				Map<Object, Object> map = redisDao.findMap(subObjKey);
				if(map == null || map.size() == 0) {
					redisDao.insertSet(edgeKey, subObjKey);
					postPlan(subObj);
				}
				else {
					deletePlan(subObjKey);
					postPlan(subObj);
				}
			}
			else if(obj instanceof JSONArray) {
				for(int i = 0; i < ((JSONArray)obj).length(); i++) {
					JSONObject subObj = ((JSONArray)obj).getJSONObject(i);
					String subObjKey = subObj.getString(TYPE) + SEP + subObj.getString(ID);
					
					Map<Object, Object> map = redisDao.findMap(subObjKey);
					if(map == null || map.size() == 0) {
						redisDao.insertSet(edgeKey, subObjKey);
						postPlan(subObj);
					}
					else {
						deletePlan(subObjKey);
						postPlan(subObj);
					}
				}
			}
		}
	}
	/*
	private boolean exists(JSONObject newjo) {
		Queue<JSONObject> queue = new LinkedList<>();
		for(String attribute : newjo.keySet()) {
			Object obj = newjo.get(attribute);
			if(obj instanceof JSONObject) {
				JSONObject subObj = (JSONObject)obj;
				queue.offer(subObj);
			}
			else if(obj instanceof JSONArray) {
				for(int i = 0; i < ((JSONArray)obj).length(); i++) {
					JSONObject subObj = ((JSONArray)obj).getJSONObject(i);
					queue.offer(subObj);
				}
			}
		}
		
		while(!queue.isEmpty()) {
			JSONObject cur = queue.poll();
			String objectkey = cur.getString(TYPE) + SEP + cur.getString(ID);
			Map<Object, Object> map = redisDao.findMap(objectkey);
			if( map!= null && map.size() > 0)
				return true;
			for(String attribute : cur.keySet()) {
				Object obj = cur.get(attribute);
				if(obj instanceof JSONObject) {
					queue.offer((JSONObject)obj);
				}
				else if(obj instanceof JSONArray) {
					for(int i = 0; i < ((JSONArray)obj).length(); i++) {
						JSONObject subObj = ((JSONArray)obj).getJSONObject(i);
						queue.offer(subObj);
					}
				}
			}
		}
		return false;
	}*/

	/**
	 * patch and post request
	 */
	@Override
	public void enqueue(String key, JSONObject jo, String requestType) {
		JSONObject baseObj = new JSONObject();
		String curKey;
		if(jo.has(ID))
			curKey = jo.getString(ID);
		else
			curKey = key;
		
		for(String attribute : jo.keySet()) {
			Object obj = jo.get(attribute);
			if(obj instanceof JSONObject) {
				JSONObject subObj = (JSONObject)obj;
				JSONObject joinObj = new JSONObject();
				JSONObject packet = new JSONObject();
				//change membercostshare to planservice_membercostshare
				if(attribute.equals("planserviceCostShares") && subObj.getString(TYPE).equals("membercostshare")) {
					joinObj.put("name", "planservice_membercostshare");
				}
				else {
					joinObj.put("name", subObj.getString(TYPE));
				}
				joinObj.put("parent", curKey);
				subObj.put("plan_service", joinObj);
				packet.put("parent_id", curKey);
				packet.put("document", subObj);
				packet.put("id", subObj.getString(ID));
				packet.put("request", requestType);
				redisDao.enqueue(packet.toString());
			}
			else if (obj instanceof JSONArray) {
				for(int i = 0; i < ((JSONArray)obj).length(); i++) {
					JSONObject subObj = ((JSONArray)obj).getJSONObject(i);
					//recursively enqueue
					enqueue(curKey, subObj, requestType);
				}
			}
			else {
				baseObj.put(attribute, (String)obj);
			}
		}
		if(!baseObj.has(TYPE))
			return;
		JSONObject packet = new JSONObject();
		JSONObject joinObj = new JSONObject();
		joinObj.put("name", baseObj.getString(TYPE));
		//top level objects don't have parent
		if(!baseObj.getString(TYPE).equals("plan")) {
			joinObj.put("parent", key);
			packet.put("parent_id", key);
		}
		baseObj.put("plan_service", joinObj);
		/*
		 * {
		 *   "parent_id": "",
		 *   "id" : "",
		 *   "request" : "post",
		 *   "document": {
		 *     "objectType" : "",
		 *     "objectId" : "",
		 *     "plan_service" : {
		 *         "name" : "",
		 *         "parent" : ""
		 *     }
		 *   }
		 * }
		 * */
		packet.put("document", baseObj);
		packet.put("id", baseObj.getString(ID));
		packet.put("request", requestType);
		redisDao.enqueue(packet.toString());
	}

	/**
	 * delete request
	 */
	@Override
	public void enqueue(String key) {
		JSONObject packet = new JSONObject();
		Set<String> keys = redisDao.getKeys("*" + key + "*");
		keys.remove(key);
		
		packet.put("id", key.split(SEP)[1]);
		packet.put("request", "delete");
		
		for(String edgeKey: keys) {
			Set subObjs = redisDao.findSet(edgeKey);
			for(Object str: subObjs) {
				String subKey = (String)str;
				enqueue(subKey);
			}
		}
		redisDao.enqueue(packet.toString());
	}
}
