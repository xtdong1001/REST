package com.xdong.dao;

import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Repository;

@Repository
public class RedisDaoImpl implements RedisDao  {

	private RedisTemplate<String, Object> redisTemplate;
    private HashOperations hashOperations;
    private SetOperations setOperations;
	
    @Autowired
    public RedisDaoImpl(RedisTemplate<String, Object> redisTemplate){
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    private void init(){
        hashOperations = redisTemplate.opsForHash();
        setOperations = redisTemplate.opsForSet();
    }

	@Override
	public void delete(String key) {
		// TODO Auto-generated method stub
		redisTemplate.delete(key);
	}

	//insert an item into a set (key - set)
	@Override
	public boolean insertSet(String key, Object value) {
		if(setOperations.isMember(key, value))
			return false;
		else {
			setOperations.add(key, value);
			return true;
		}
	}

	// insert a key-value pair into a Map (key - map) -> (id - value)
	@Override
	public boolean insertMap(String key, String id, Object value) {
		return hashOperations.putIfAbsent(key, id, value);
	}

	//delete an item from a set
	@Override
	public void deleteSet(String key, Object value) {
		// TODO Auto-generated method stub
		setOperations.remove(key, value);
	}

	//delete a key from a map
	@Override
	public void deleteMap(String key, String hashKey) {
		// TODO Auto-generated method stub
		hashOperations.delete(key, hashKey);
	}

	@Override
	public Map<Object, Object> findMap(String key) {
		// TODO Auto-generated method stub
		return hashOperations.entries(key);
	}

	@Override
	public Set findSet(String key) {
		// TODO Auto-generated method stub
		return setOperations.members(key);
	}

	@Override
	public Set<String> getKeys(String pattern) {
		return redisTemplate.keys(pattern);
	}

}
