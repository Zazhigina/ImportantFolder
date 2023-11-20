package ru.zazhig.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.zazhig.entity.AppDocument;

public interface AppDocumentDao extends JpaRepository<AppDocument, Long> {
}
