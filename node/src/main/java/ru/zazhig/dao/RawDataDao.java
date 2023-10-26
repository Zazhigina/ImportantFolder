package ru.zazhig.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.zazhig.entiti.RawData;

public interface RawDataDao extends JpaRepository<RawData, Long> {
}