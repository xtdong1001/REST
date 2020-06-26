package com.xdong.controller;

import org.springframework.web.bind.annotation.RestController;

import com.xdong.service.RedisService;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@RestController
@RequestMapping(value = "/plan")
public class PlanController {

	private static final String SEP = "____";
	private static final String TYPE = "objectType";
	private static final String ID = "objectId";
	
	@Autowired
	private RedisService redisService;
	

	@RequestMapping(value = "", method = RequestMethod.POST)
	public ResponseEntity<String> post(@RequestBody String entity) {
		try {
			JSONObject jo = new JSONObject(entity.trim());
			if(!redisService.validate(jo))
				return new ResponseEntity<>("{\"message\": \"Invalid JSON\"}", HttpStatus.BAD_REQUEST);
			
			String key = jo.getString(TYPE) + SEP + jo.getString(ID);
			if(redisService.exist(key))
				return new ResponseEntity<>("{\"message\": \"A plan already exists with id " + jo.getString(ID) +"\"}",HttpStatus.CONFLICT);
			redisService.postPlan(jo);
			return new ResponseEntity<>("{\"objectId\": \""+ jo.getString(ID) + "\", \"objectType\": \"" + jo.getString(TYPE) + "\", \"message\": \"Created Successfully\", }", HttpStatus.CREATED);
		}
		catch(Exception e) {
			System.out.print(e.getMessage());
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR); //500
		}
		
	}
	
	
	@RequestMapping(value = "/{type}/{id}", method = RequestMethod.GET)
	public ResponseEntity<String> get(@PathVariable("type") String type, @PathVariable("id") String id) {
		JSONObject jo = redisService.getPlan(type + SEP + id);
		if(jo.length() == 0)
			return new ResponseEntity<String>("{\"message\": \"No Data Found\"}", HttpStatus.NOT_FOUND); // 404
		String plan = jo.toString();	
		return ResponseEntity.ok().body(jo.toString()); // 200
	}
	

	@RequestMapping(value = "/{type}/{id}", method = RequestMethod.DELETE)
	public ResponseEntity<String> delete(@PathVariable("type") String type, @PathVariable("id") String id) {
		JSONObject jo = redisService.deletePlan(type + SEP + id);
		if(jo.length() == 0)
			return new ResponseEntity<String>("{\"message\": \"No Data Found\"}", HttpStatus.GONE); //410
			
		return ResponseEntity.ok().body(jo.toString()); //200
	}
	@RequestMapping(value = "/{type}/{id}", method = RequestMethod.PATCH)
	public ResponseEntity<String> patch(@RequestBody String entity, @PathVariable("type") String type, @PathVariable("id") String id) {
		try {
			JSONObject newjo = new JSONObject(entity.trim());
			String key = type + SEP + id;
			JSONObject jo = redisService.getPlan(key);
			if(jo.length() == 0)
				return new ResponseEntity<String>("{\"message\": \"No Data Found\"}", HttpStatus.NOT_FOUND); // 404
			
			JSONObject updatedjo = redisService.patchPlan(key, newjo);
			return new ResponseEntity<>("{\"objectId\": \""+ jo.getString(ID) + "\", \"objectType\": \"" + jo.getString(TYPE) + "\", \"message\": \"Created Successfully\", }", HttpStatus.CREATED);
		}
		catch(Exception e) {
			System.out.print(e.getMessage());
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR); //500
		}
		
		
	}
}
