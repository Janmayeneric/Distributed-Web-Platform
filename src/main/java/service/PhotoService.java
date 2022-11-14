package service;

import java.io.IOException;

import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import object.Photo;
import repository.PhotoRepository;

@Service
public class PhotoService {
	
	@Autowired
	private PhotoRepository repository;
	
	/**
	 * add photo into the database and return its id inside the mongoRepository
	 * @param title
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public String addPhoto(String title, MultipartFile file) throws IOException{
		Photo photo = new Photo();
		photo.setTitle(title);
		photo.setImage(new Binary(BsonBinarySubType.BINARY,file.getBytes()));
		photo = repository.insert(photo);
		return photo.getId();
	}
	
	
}
