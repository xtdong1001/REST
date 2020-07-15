package com.xdong.controller;

import org.springframework.web.bind.annotation.RestController;

import com.xdong.service.RedisService;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
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
			
			HttpHeaders resHeaders = new HttpHeaders();
			resHeaders.setETag("\"" + getSHAString(jo.toString()) + "\"");
			
			return new ResponseEntity<>("{\"objectId\": \""+ jo.getString(ID) + "\", \"objectType\": \"" + jo.getString(TYPE) + "\", \"message\": \"Created Successfully\", }", resHeaders, HttpStatus.CREATED);
		}
		catch(Exception e) {
			System.out.print(e.getMessage());
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR); //500
		}
		
	}
	
	
	@RequestMapping(value = "/{type}/{id}", method = RequestMethod.GET)
	public ResponseEntity<String> get(@PathVariable("type") String type, @PathVariable("id") String id, @RequestHeader HttpHeaders headers) {		
		String oldETag = headers.getFirst("If-None-Match");
		JSONObject jo = redisService.getPlan(type + SEP + id);
		if(jo.length() == 0)
			return new ResponseEntity<String>("{\"message\": \"No Data Found\"}", HttpStatus.NOT_FOUND); // 404
		String plan = jo.toString();
		
		try {
			String newETag = "\"" + getSHAString(plan) + "\"";
			
			HttpHeaders resHeaders = new HttpHeaders();
			resHeaders.setETag(newETag);
			
			
			if(oldETag == null || oldETag.length() == 0 || !oldETag.equals(newETag)) 
				return new ResponseEntity<>(plan, resHeaders, HttpStatus.OK);
			
			else
				return new ResponseEntity<String>("{\"objectId\": \""+ jo.getString(ID) + "\", \"objectType\": \"" + jo.getString(TYPE) + "\", \"message\": \"Not Modified\"}", resHeaders, HttpStatus.NOT_MODIFIED);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
	}
	

	@RequestMapping(value = "/{type}/{id}", method = RequestMethod.DELETE)
	public ResponseEntity<String> delete(@PathVariable("type") String type, @PathVariable("id") String id) {
		JSONObject jo = redisService.deletePlan(type + SEP + id);
		if(jo.length() == 0)
			return new ResponseEntity<String>("{\"message\": \"No Data Found\"}", HttpStatus.GONE); //410
			
		return ResponseEntity.ok().body(jo.toString()); //200
	}
	
	
	@RequestMapping(value = "/{type}/{id}", method = RequestMethod.PATCH)
	public ResponseEntity<String> patch(@RequestBody String entity, @PathVariable("type") String type, @PathVariable("id") String id, @RequestHeader HttpHeaders headers) {
		//conditional write
		String oldETag = headers.getFirst("If-Match");
		
		try {
			JSONObject newjo = new JSONObject(entity.trim());
			String key = type + SEP + id;
			JSONObject jo = redisService.getPlan(key);
			if(jo.length() == 0)
				return new ResponseEntity<String>("{\"message\": \"No Data Found\"}", HttpStatus.NOT_FOUND); // 404
			
			HttpHeaders resHeaders = new HttpHeaders();
			
			String newETag = "\"" + getSHAString(jo.toString()) + "\"";
			resHeaders.setETag(newETag);
			
			if(oldETag != null && oldETag.length() > 0 && !oldETag.equals(newETag))
				return new ResponseEntity<>("{\"objectId\": \""+ jo.getString(ID) + "\", \"objectType\": \"" + jo.getString(TYPE) + "\", \"message\": \"Error! The plan has been modified\", }", resHeaders, HttpStatus.CONFLICT);
			
			redisService.patchPlan(key, newjo);
			
			newETag = "\"" + getSHAString(redisService.getPlan(key).toString()) + "\"";
			
			resHeaders.setETag(newETag);
			return new ResponseEntity<>("{\"objectId\": \""+ jo.getString(ID) + "\", \"objectType\": \"" + jo.getString(TYPE) + "\", \"message\": \"Updated Successfully\", }", resHeaders, HttpStatus.OK);
		}
		catch(Exception e) {
			System.out.print(e.getMessage());
			return new ResponseEntity<>("{\"message\": \""+ e.getMessage() + "\"}", HttpStatus.BAD_REQUEST); //
		}
	}
	
    // generate ETag using SHA256 Hash algorithm
	private String getSHAString(String input) throws NoSuchAlgorithmException { 
		MessageDigest md = MessageDigest.getInstance("SHA-256");  
        byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
        
        BigInteger number = new BigInteger(1, hash);  
  
        StringBuilder hexString = new StringBuilder(number.toString(16));  
  
        while (hexString.length() < 32) {  
            hexString.insert(0, '0');  
        }  
  
        return hexString.toString();  
    } 
}
