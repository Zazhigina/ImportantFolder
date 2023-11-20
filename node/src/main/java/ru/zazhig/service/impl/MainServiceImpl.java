package ru.zazhig.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.zazhig.dao.AppUserDao;
import ru.zazhig.dao.RawDataDao;
import ru.zazhig.entity.AppDocument;
import ru.zazhig.entity.AppPhoto;
import ru.zazhig.entity.AppUser;
import ru.zazhig.entity.RawData;
import ru.zazhig.exceptions.UploadFileException;
import ru.zazhig.service.FileService;
import ru.zazhig.service.MainService;
import ru.zazhig.service.ProducerService;
import ru.zazhig.service.enams.ServiceCommands;

import static ru.zazhig.dao.UserState.BASIC_STATE;
import static ru.zazhig.dao.UserState.WAIT_FOR_EMAIL_STATE;
import static ru.zazhig.service.enams.ServiceCommands.*;


@Service
@Slf4j
@RequiredArgsConstructor
public class MainServiceImpl implements MainService {
    private final RawDataDao rawDataDao;
    private final ProducerService producerService;
    private final AppUserDao appUserDao;
    private final FileService fileService;

    @Override
    public void processTextMessage(Update update) {
        saveRawData(update);
        var appUser = findOrSaveAppUser(update);
        var userState = appUser.getState();
        var text = update.getMessage().getText();
        var output = "";

        var serviceCommands = ServiceCommands.fromValue(text);
        if (CANCEL.equals(serviceCommands)) {
            output = cancelProcess(appUser);
        } else if (BASIC_STATE.equals(userState)) {
            output = producerServiceCommand(appUser, text);

        } else if (WAIT_FOR_EMAIL_STATE.equals(userState)) {
            //TODO добавить обраьботку емайла
        } else {
            log.error("Unknown user state: " + userState);
            output = "Неизвестная ошибка! Введите /cancel и попробуйте снова!";
        }

        var chatId = update.getMessage().getChatId();
        sendAnswer(output, chatId);

    }

    @Override
    public void processDocMessage(Update update) {
        saveRawData(update);
        var appUser = findOrSaveAppUser(update);
        var chatId = update.getMessage().getChatId();
        if (isNotAllowToSendContent(chatId, appUser)) {
            return;
        }
        try {
            AppDocument doc = fileService.processDoc(update.getMessage());
            //todo добавить сохранение документа
            var answer = "Документ успешно загружен! Ссылка для скачивания: http://test.ru/get-doc/777";
            sendAnswer(answer, chatId);
        } catch (UploadFileException e) {
            log.error("Документ не загружен");
            throw new UploadFileException("Ошибка загрузки документа. Документ не загружен", e);
        }
    }

    @Override
    public void processPhotoMessage(Update update) {
        saveRawData(update);
        var appUser = findOrSaveAppUser(update);
        var chatId = update.getMessage().getChatId();
        if (isNotAllowToSendContent(chatId, appUser)) {
            return;
        }
        try {
            AppPhoto photo = fileService.processPhoto(update.getMessage());
            //todo добавить генерацию ссылки для скачивания фото
            var answer = "Фото успешно загружено! Ссылка для скачивания: http://test.ru/get-photo/777";
            sendAnswer(answer, chatId);
        } catch (UploadFileException e) {
            log.error("Фото не загружено");
            throw new UploadFileException("Ошибка загрузки фото. Фото не загружено", e);
        }

    }

    private boolean isNotAllowToSendContent(Long chatId, AppUser appUser) {
        var userState = appUser.getState();
        if (!appUser.getIsActive()) {
            var error = "Зарегистрируйтесь или активируйте свою учетную запись для загрузки контента";
            sendAnswer(error, chatId);
            return true;
        } else if (!BASIC_STATE.equals(userState)) {
            var error = "Отмените текущую команду с помощью /cancel для отправки файлаю";
            sendAnswer(error, chatId);
            return true;
        }
        return false;
    }


    private void sendAnswer(String output, Long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(output);
        producerService.producerAnswer(sendMessage);
    }

    private String producerServiceCommand(AppUser appUser, String cmd) {
        var serviceCommand = ServiceCommands.fromValue(cmd);
        if (REGISTRATION.equals(serviceCommand)) {
            //TODO добавить регистрацию
            return "Временно недоступно";
        } else if (HELP.equals(serviceCommand)) {
            return help();
        } else if (START.equals(serviceCommand)) {
            return "Приветствую! Чтобы посмотреть список доступных команд введите /help";

        } else {
            return "Неизвестная команда! Чтобы посмотреть список доступных команд введите /help";
        }
    }

    private String help() {
        return "Список доступных команд: \n" +
                "/cancel - отмена выполнения текущей команды;\n" +
                "/registration - регистрация пользователяж \n";
    }

    private String cancelProcess(AppUser appUser) {
        appUser.setState(BASIC_STATE);
        appUserDao.save(appUser);
        return "Команда отменена";
    }

    private AppUser findOrSaveAppUser(Update update) {
        User telegramUser = update.getMessage().getFrom();
        AppUser persistentAppUser = appUserDao.findAppUserByTelegramUserId(telegramUser.getId());
        if (persistentAppUser == null) {
            AppUser transientAppUser = AppUser.builder()
                    .telegramUserId(telegramUser.getId())
                    .userName(telegramUser.getUserName())
                    .firstName(telegramUser.getFirstName())
                    .lastName(telegramUser.getLastName())
                    //TODO изменить значение по умолчанию после добавления регистрации
                    .isActive(true)
                    .state(BASIC_STATE)
                    .build();
            return appUserDao.save(transientAppUser);
        }
        return persistentAppUser;
    }

    private void saveRawData(Update update) {
        RawData rawData = RawData.builder()
                .event(update)
                .build();
        rawDataDao.save(rawData);
    }
}
