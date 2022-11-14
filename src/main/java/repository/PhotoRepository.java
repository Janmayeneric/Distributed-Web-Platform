package repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import object.Photo;

public interface PhotoRepository extends MongoRepository<Photo, String>{

}
