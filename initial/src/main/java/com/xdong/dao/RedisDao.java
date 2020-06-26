package com.xdong.dao;

import java.util.Map;
import java.util.Set;

public interface RedisDao {
    /**
     * Add key-value pair to Redis.
     */
	public boolean insertSet(String key, Object value);
	public boolean insertMap(String key, String hashKey, Object value);

    /**
     * Delete a key-value pair in Redis.
     */
	public void deleteSet(String key, Object value);
	public void deleteMap(String key, String hashKey);
    
	public void delete(String key);
    
    /**
     * find a plan
     */
    Map<Object, Object> findMap(String key);
    
    Set findSet(String key);
    
    Set<String> getKeys(String pattern);
}
