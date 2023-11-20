package ru.zazhig.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.zazhig.entity.AppPhoto;

public interface AppPhotoDao extends JpaRepository<AppPhoto, Long> {
}
