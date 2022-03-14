package com.spacedev.board.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.spacedev.board.entity.Contact;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {
	
	// findBy 컬럼이름(컬럼 타입); => 정의가능
	Contact findTop1ByEmailOrderByWdateDesc(String email);
}
