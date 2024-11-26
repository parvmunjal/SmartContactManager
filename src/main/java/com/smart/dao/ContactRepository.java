package com.smart.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smart.entities.Contact;
import com.smart.entities.User;

public interface ContactRepository extends JpaRepository<Contact, Integer>{
	@Query("select c from Contact c where c.user.id=:userId")
	//Current Page- page
	//No of contacts per page- 11
	public List<Contact> findContactsByUser(@Param("userId") int userId);
	
	public List<Contact> findByNameContainingAndUser(String name,User user);
}
