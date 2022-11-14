package controller;

import java.io.IOException;
import java.util.Optional;

import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import object.Photo;
import repository.PhotoRepository;


@RestController
@RequestMapping("/photos")
public class PhotoController {
	
	@Autowired
	private PhotoRepository repository;
	
	@PostMapping
	public ResponseEntity<String> addPhoto(@RequestParam("title") String title,
			@RequestParam("image") MultipartFile image){
		try {
			Photo photo = new Photo();
			photo.setTitle(title);
			photo.setImage(new Binary(BsonBinarySubType.BINARY,image.getBytes()));
			photo = repository.insert(photo);
			return new ResponseEntity<String>(photo.getId(),HttpStatus.OK);
		}
		catch(IOException e) {
			return new ResponseEntity<String>(HttpStatus.SERVICE_UNAVAILABLE);
		}
		
	}
	
	@GetMapping(value = "/{id}", produces = MediaType.IMAGE_JPEG_VALUE)
	public ResponseEntity<byte[]> getPhoto(@PathVariable String id){
		Optional<Photo> photoOptional = repository.findById(id);
		if(photoOptional.isEmpty()) {
			return new ResponseEntity<byte[]>(HttpStatus.NOT_FOUND);
		}
		Photo photo = photoOptional.get();
		return new ResponseEntity<byte[]>(photo.getImage().getData(),HttpStatus.OK);
	}
}
