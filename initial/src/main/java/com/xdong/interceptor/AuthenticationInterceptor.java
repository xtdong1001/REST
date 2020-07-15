package com.xdong.interceptor;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

@Component
public class AuthenticationInterceptor implements HandlerInterceptor {
	private static final String CLIENT_ID = "698297633559-evamuktr4v2ue84cnaskpfupo4fi3459.apps.googleusercontent.com";
	private static final HttpTransport transport = new NetHttpTransport();
	private static final JsonFactory jsonFactory = new JacksonFactory();
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		String bearerToken =  request.getHeader("Authorization");
		if(bearerToken == null || bearerToken.length() == 0) {
			response.getWriter().write("{\"message\": \"Unauthorized\"}");
	        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return false;
		}
			
		String idTokenString = bearerToken.substring(7);
		
		GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
			    .setAudience(Collections.singletonList(CLIENT_ID))
			    .build();
		
		GoogleIdToken idToken = null;
		try {
			idToken = verifier.verify(idTokenString);
			if (idToken != null) {
				Payload payload = idToken.getPayload();

				// Print user identifier
				String userId = payload.getSubject();
				System.out.println("User ID: " + userId);

				// Get profile information from payload
				/*String email = payload.getEmail();
				String name = (String) payload.get("name");
				String pictureUrl = (String) payload.get("picture");
				String locale = (String) payload.get("locale");
				String familyName = (String) payload.get("family_name");
				String givenName = (String) payload.get("given_name");
				
				System.out.println("Email: " + email);*/
				
				return true;
			} 
			else {
				System.out.println("Invalid ID token.");
				response.getWriter().write("{\"message\": \"Invalid ID token\"}");
		        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				return false;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("Invalid ID token.");
			response.getWriter().write("{\"message\": \"Invalid ID token\"}");
	        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return false;
		}		
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		// TODO Auto-generated method stub
		
	}
}
