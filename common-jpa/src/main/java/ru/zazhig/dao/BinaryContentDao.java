package ru.zazhig.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.zazhig.entity.BinaryContent;

public interface BinaryContentDao extends JpaRepository<BinaryContent, Long> {
}
