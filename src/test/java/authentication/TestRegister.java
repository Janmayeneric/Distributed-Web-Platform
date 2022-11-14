package authentication;



import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import application.Starter;
import config.Generator;

import org.springframework.boot.test.web.client.TestRestTemplate;

import object.User;
import repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
/**
 * sample code to test the function
 * Can run in Spring Boot directly also can use two command to Run the code, "./mvnw test" or "./gradlew test" depend on the maven or gradle 
 * 
 * @author HU
 *
 */
//tell Spring Boot to look for a main configuration class @SpringApplication
//start the server with a random port (useful to avoid conflicts in test environments)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT,classes = Starter.class)
public class TestRegister {
	
	@LocalServerPort
	private int port;
	
	
	Generator generator = new Generator();
	
	@Autowired
	private TestRestTemplate restTemplate;
	
	@Autowired
	private  UserRepository userRepository;
	
	
	/**
	 * To test the registration of one user account
	 * it check the HTTP response after the post request
	 * and then check the database if the account actually inserted
	 * @throws Exception
	 */
	@Test
	public void isRegisterPostSingle() throws Exception {
		// generate the username and password with random stirngs
		String username = generator.string(10, 12);
		String password = generator.string(10, 20);
		
		// create the formed data 
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		MultiValueMap<String, String> map= new LinkedMultiValueMap<String, String>();
		map.add("username", username);
		map.add("password", password);
		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers);
		
		// then check the status code
		assertThat(restTemplate.postForEntity("http://localhost:" + port + "/users", request, ResponseEntity.class).getStatusCode().equals(HttpStatus.OK));
		
	}
	
	@Test
	public void isResigerPost100() throws Exception{
		// generate the set of 100 usernames and password
		HashSet<String> usernameSet = generator.stringsSet(100,10,12);
		HashSet<String> passwordSet = generator.stringsSet(100,10,12);
		
		// switch to arraylist for easier iteration
		ArrayList<String> usernameList = new ArrayList<String>(usernameSet);
		ArrayList<String> passwordList = new ArrayList<String>(passwordSet);
		
		for(int i = 0 ; i< usernameList.size();i++) {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
			MultiValueMap<String, String> map= new LinkedMultiValueMap<String, String>();
			map.add("username", usernameList.get(i));
			map.add("password", passwordList.get(i));
			HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers);
			assertThat(restTemplate.postForEntity("http://localhost:" + port + "/users", request, ResponseEntity.class).getStatusCode().equals(HttpStatus.OK));
		}
		
		
	}
	
	/**
	 * it register two identical account
	 * expect to receive the HTTP OK and then the HTTP CONFLICT
	 * @throws Exception
	 */
	@Test
	public void registerDuplicate() throws Exception{
		// generate the username and password with random stirngs
		String username = generator.string(10, 12);
		String password = generator.string(10, 12);
				
		// create the formed data 
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		MultiValueMap<String, String> map= new LinkedMultiValueMap<String, String>();
		map.add("username", username);
		map.add("password", password);
		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers);
				
		// then check the status code
		assertThat(restTemplate.postForEntity("http://localhost:" + port + "/users", request, ResponseEntity.class).getStatusCode().equals(HttpStatus.OK));
		
		// assert again expect the conflict code
		assertThat(restTemplate.postForEntity("http://localhost:" + port + "/users", request, ResponseEntity.class).getStatusCode().equals(HttpStatus.CONFLICT));	
	}
	
}
