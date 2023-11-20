package ru.zazhig.service;

import org.telegram.telegrambots.meta.api.objects.Message;
import ru.zazhig.entity.AppDocument;
import ru.zazhig.entity.AppPhoto;

public interface FileService {
    AppDocument processDoc(Message telegramMessage);
    AppPhoto processPhoto(Message telegramMessage);
}
