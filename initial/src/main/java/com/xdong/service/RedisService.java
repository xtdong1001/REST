package com.xdong.service;

import org.json.JSONObject;

public interface RedisService {
	public JSONObject getPlan(final String key);
	public void postPlan(JSONObject jo);
	public void updatePlan(final String key, final String value);
	public void patchPlan(String key, JSONObject newjo);
	public JSONObject deletePlan(final String key);
	public boolean validate(JSONObject jo);
	public boolean exist(String key);
	public void enqueue(String key, JSONObject jo, String requestType);
	public void enqueue(String key);
}
