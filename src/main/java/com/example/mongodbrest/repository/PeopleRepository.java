package com.example.mongodbrest.repository;

import com.example.mongodbrest.model.Person;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "people", path = "people")
public interface PeopleRepository extends PagingAndSortingRepository<Person, String> {
}
