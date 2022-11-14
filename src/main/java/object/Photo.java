package object;

import org.bson.types.Binary;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * 
 * @author https://www.baeldung.com/spring-boot-mongodb-upload-file
 * BSON file ONLY accept the file in 16MB
 * So the photo limit to 16mb if we use this class to store the photo
 */
@Document(collection = "photos")
public class Photo {
	@Id
	private String id;
	
	private String title;
	
	private Binary image;
	
	public Photo() {
		
	}
	
	public Photo(String id, String title, Binary image) {
		this.id = id;
		this.title = title;
		this.image = image;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public void setTitle(String title){
		this.title = title;
	}
	
	public void setImage(Binary image) {
		this.image = image;
	}
	
	public String getId() {
		return this.id;
	}
	
	public String getTitle() {
		return this.title;
	}
	
	public Binary getImage() {
		return this.image;
	}
}
