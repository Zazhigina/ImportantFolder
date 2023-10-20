package ru.zazhig.service;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface UpdateProducer {
    void produce(String rebbitQueue, Update update);


}
