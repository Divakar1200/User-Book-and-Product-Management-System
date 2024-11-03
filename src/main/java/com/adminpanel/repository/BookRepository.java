package com.adminpanel.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.adminpanel.model.Book;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {
    List<Book> findByUserId(Long userId);
}

