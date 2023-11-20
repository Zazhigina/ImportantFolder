package ru.zazhig.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.zazhig.entity.AppUser;

public interface AppUserDao extends JpaRepository<AppUser,Long> {
    AppUser findAppUserByTelegramUserId(Long id);
}
