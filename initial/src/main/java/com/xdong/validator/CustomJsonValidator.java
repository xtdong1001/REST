package com.xdong.validator;

import java.io.IOException;
import java.io.InputStream;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;

@Repository
public class CustomJsonValidator {
	
	private Schema schema;
	
	public CustomJsonValidator() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		InputStream inputStream = classLoader.getResourceAsStream("schema.json");
        JSONObject rawSchema = new JSONObject(new JSONTokener(inputStream));
        schema = SchemaLoader.load(rawSchema);
	}

	public Schema getSchema() {
		return schema;
	}

	public void setSchema() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		InputStream inputStream = classLoader.getResourceAsStream("schema.json");
        JSONObject rawSchema = new JSONObject(new JSONTokener(inputStream));
        schema = SchemaLoader.load(rawSchema);
	}
	
	public boolean validate(JSONObject jo) {
		try {
			schema.validate(jo);
			System.out.println("Validation success");
			return true;
		}
		catch (ValidationException e) {
			e.printStackTrace();
			return false;
		}
	}

}
