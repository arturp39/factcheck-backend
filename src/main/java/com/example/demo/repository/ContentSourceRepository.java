package com.example.demo.repository;

import com.example.demo.entity.content.ContentSource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContentSourceRepository extends JpaRepository<ContentSource, Long> {

    List<ContentSource> findAllByOrderByNameAsc();

    List<ContentSource> findAllByEnabledTrueOrderByNameAsc();
}
