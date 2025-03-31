package ru.eddyz.sellautorestapi.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.eddyz.sellautorestapi.entities.*;
import ru.eddyz.sellautorestapi.enums.*;
import ru.eddyz.sellautorestapi.payloads.ChatListPayload;
import ru.eddyz.sellautorestapi.payloads.MessagePayload;
import ru.eddyz.sellautorestapi.repositories.*;

import java.io.*;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CsvDataService {

    private final UserRepository userRepository;
    private final ColorRepository colorRepository;
    private final BrandRepository brandRepository;
    private final AdRepository adRepository;
    private final AccountRepository accountRepository;
    private final ModelRepository modelRepository;
    private final CarRepository carRepository;
    private final PhotoRepository photoRepository;
    private final PriceRepository priceRepository;
    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;

    private final ObjectMapper objectMapper;

    private static final String USER_FILENAME = "user.csv";
    private static final String BRAND_FILENAME = "brands.csv";
    private static final String COLOR_FILENAME = "colors.csv";
    private static final String ADS_FILENAME = "ads.csv";

    private static final String BACKUP_ZIP = "backup-%s.zip";
    private static final String BACKUP_DIR = "backup";

    @Value("${ad.photo.directory}")
    private static String AD_PHOTO_DIR;


    public Optional<File> exportToCsv() {
        try {
            log.debug("Начат экспорт таблицы user");
            var users = exportUserToCsv();
            log.debug("Экспорт юзеров закончен");

            log.debug("Начат экспорт брендов и моделей");
            var brands = exportBrandAndModels();
            log.debug("Экспорт брендов и моделей закончен");

            log.debug("Начат экспорт созданных цветов");
            var colors = exportColors();
            log.debug("Экспорт цветов закончен");

            log.debug("Начат экспорт объявлений и чатов");
            var ads = exportAd();
            log.debug("Экспорт объявлений и чатов закончен");

            return Optional.of(createZip(List.of(users, brands, colors, ads)));
        } catch (IOException e) {
            log.error("Ошибка экспорта данных {}", e.toString());
            return Optional.empty();
        }
    }

    public void importFromCsv(InputStream zipFile) {
        try {
            unZip(zipFile);
            log.debug("Начинаю импорт юзеров из csv");
            importUserFromCsv();
            log.debug("Импорт юзеров закончен");

            log.debug("Начинаю импорт брендов и моделей");
            importBrandAndModels();
            log.debug("Импорт брендов и моделей закончен");

            log.debug("Начинаю импорт цветов");
            importColors();
            log.debug("Импорт цветов закончен");

            log.debug("Начинаю импорт объявлений");
            importAds();
            log.debug("Импорт объявлений закончен");

        } catch (IOException e) {
            log.error("Ошибка при импорте базы данных {}", e.toString());
        }
    }

    private void importColors() throws IOException {
        try (CSVReader reader = new CSVReader(new FileReader(getBackupDir(COLOR_FILENAME)))) {
            String[] line;
            while ((line = reader.readNext()) != null) {
                if (line[0].equals("colorName")) continue;

                final var colorName = line[0];

                if (colorRepository.findByTitle(colorName).isEmpty())
                    colorRepository.save(Color.builder()
                            .title(colorName).build());
            }
        } catch (CsvValidationException e) {
            log.error("Ошибка при чтении файла {} : {} ", COLOR_FILENAME, e.toString());
        }
    }

    private void importBrandAndModels() throws IOException {
        try (CSVReader reader = new CSVReader(new FileReader(getBackupDir(BRAND_FILENAME)))) {
            String[] line;
            while ((line = reader.readNext()) != null) {
                if (line[0].equals("brandName")) continue;

                final var brandName = line[0];

                Brand brand;
                var brandOp = brandRepository.findByTitle(brandName);

                brand = brandOp.orElseGet(() -> brandRepository.save(Brand.builder()
                        .title(brandName)
                        .build()));

                var models = line[1].split(";");
                Arrays.stream(models)
                        .forEach(s -> {
                            if (modelRepository.findByBrandBrandId(brand.getBrandId())
                                    .stream()
                                    .filter(model -> s.equals(model.getTitle()))
                                    .findFirst().isEmpty()) {
                                modelRepository.save(Model.builder()
                                        .brand(brand)
                                        .title(s)
                                        .build());
                            }
                        });
                log.info(String.valueOf(modelRepository.findAll().size()));
            }
        } catch (CsvValidationException e) {
            log.error("Ошибка чтения файла {}: {}", BRAND_FILENAME, e.toString());
        }
    }

    private void importUserFromCsv() throws IOException {
        try (CSVReader reader = new CSVReader(new FileReader(getBackupDir(USER_FILENAME)))) {
            String[] line;
            while ((line = reader.readNext()) != null) {
                if (line[0].equals("email")) continue;

                final var emailUser = line[0];
                final var phoneNumber = line[1];
                final var password = line[2];
                final var firstName = line[3];
                final var lastName = line[4];
                final var role = Role.valueOf(line[5]);
                final var blocked = Boolean.getBoolean(line[6]);

                if (accountRepository.findByEmail(emailUser).isPresent())
                    continue;

                var account = Account.builder()
                        .email(emailUser)
                        .phoneNumber(phoneNumber)
                        .password(password)
                        .role(role)
                        .blocked(blocked)
                        .build();

                account = accountRepository.save(account);

                var user = User.builder()
                        .account(account)
                        .firstName(firstName)
                        .lastName(lastName)
                        .build();

                userRepository.save(user);
            }
        } catch (CsvValidationException e) {
            log.error("Ошибка чтения {} файла: {}", USER_FILENAME, e.toString());
        }
    }


    private void importAds() throws IOException {
        try (CSVReader reader = new CSVReader(new FileReader(getBackupDir(ADS_FILENAME)))) {
            String[] line;
            while ((line = reader.readNext()) != null) {
                if (line[0].equals("adId")) continue;

                final var userEmail = line[1];
                final var title = line[2];
                final var description = line[3];
                final var prices = line[4];
                final var isActive = Boolean.parseBoolean(line[5]);
                final var createdAt = LocalDateTime.parse(line[6]);
                final var carVin = line[7];
                final var carYear = Integer.parseInt(line[8]);
                final var carMileage = Integer.parseInt(line[9]);
                final var bodyType = BodyType.valueOf(line[10]);
                final var drive = DriveMode.valueOf(line[11]);
                final var color = colorRepository.findByTitle(line[12])
                        .orElseThrow();
                final var brand = brandRepository.findByTitle(line[13])
                        .orElseThrow();
                final var modelName = line[14];
                final var model = modelRepository.findByBrandBrandId(brand.getBrandId()).stream()
                        .filter(m -> m.getTitle().equals(modelName))
                        .findFirst()
                        .orElseThrow();
                final var engineType = EngineType.valueOf(line[15]);
                final var transmissionType = TransmissionType.valueOf(line[16]);
                final var photos = line[17];
                final var chats = line[18];

                var user = userRepository.findByAccountEmail(userEmail)
                        .orElseThrow();

                var car = Car.builder()
                        .vin(carVin)
                        .year(carYear)
                        .mileage(carMileage)
                        .bodyType(bodyType)
                        .drive(drive)
                        .color(color)
                        .brand(brand)
                        .model(model)
                        .transmissionType(transmissionType)
                        .engineType(engineType)
                        .build();

                if (carRepository.findByVin(carVin).isPresent())
                    car.setCarId(carRepository.findByVin(carVin).get().getCarId());

                var ad = Ad.builder()
                        .title(title)
                        .description(description)
                        .isActive(isActive)
                        .createdAt(createdAt)
                        .user(user)
                        .build();

                if (adRepository.findByCarVin(carVin).isPresent())
                    ad.setAdId(adRepository.findByCarVin(carVin).get().getAdId());

                ad = adRepository.save(ad);

                car.setAd(ad);

                car = carRepository.save(car);

                savePhotoCars(photos, car);

                savePrices(prices, ad);

                if (chats.trim().isEmpty())
                    return;

                var chatsData = objectMapper.readValue(chats, new TypeReference<List<ChatListPayload>>() {
                });
                savingChats(chatsData, ad);
            }
        } catch (CsvValidationException e) {
            log.error("Ошибка чтения файла {}: {}", ADS_FILENAME, e.toString());
        }
    }

    private void savingChats(List<ChatListPayload> chatsData, Ad ad) {
        for (var c : chatsData) {
            var users = getUsersChats(c.getUserEmails());
            var adChats = chatRepository.findByAdAdId(ad.getAdId());

            var isExists = false;
            for (var c1 : adChats) {
                for (var u : c1.getUsers()) {
                    if (!c.getUserEmails().contains(u.getAccount().getEmail()))
                        continue;

                    isExists = true;
                }
                saveMessages(c1, c.getMessages());
            }
            if (!isExists) {
                var newChat = chatRepository.save(Chat.builder()
                        .users(users)
                        .ad(ad)
                        .build());
                saveMessages(newChat, c.getMessages());
            }
        }

        System.out.println(chatRepository.findAll());
    }

    private File exportAd() throws IOException {
        var ads = adRepository.findAll();
        var file = getBackupDir(ADS_FILENAME);
        try (CSVWriter csvWriter = new CSVWriter(new FileWriter(file))) {
            var headers = new String[]{"adId", "userEmail", "title", "description", "price", "isActive",
                    "createdAt", "carVin", "carYear", "carMileage", "carBodyType", "carDrive", "carColor", "carBrand",
                    "carModel", "carEngineType", "carTransmissionType", "photos", "chats", "chatUsersEmail"};
            csvWriter.writeNext(headers);
            for (Ad ad : ads) {
                var prices = new StringBuilder();
                var photos = new StringBuilder();
                var messagesChat = "";
                if (!ad.getChats().isEmpty()) {
                    messagesChat = objectMapper.writeValueAsString(ad.getChats()
                            .stream()
                            .map(chat -> mapChatListPayload(ad, chat))
                            .toList());
                    System.out.println(messagesChat);
                }

                ad.getPrices().forEach(price -> prices.append(price.getPrice())
                        .append("::")
                        .append(price.getCreatedAt())
                        .append(";"));

                ad.getCar().getPhotos().forEach(photo ->
                        photos.append(photo.getFilePath())
                                .append(";"));


                var adData = new String[]{
                        ad.getAdId().toString(),
                        ad.getUser().getAccount().getEmail(),
                        ad.getTitle(),
                        ad.getDescription(),
                        prices.toString(),
                        ad.getIsActive().toString(),
                        ad.getCreatedAt().toString(),
                        ad.getCar().getVin(),
                        ad.getCar().getYear().toString(),
                        ad.getCar().getMileage().toString(),
                        ad.getCar().getBodyType().name(),
                        ad.getCar().getDrive().name(),
                        ad.getCar().getColor().getTitle(),
                        ad.getCar().getBrand().getTitle(),
                        ad.getCar().getModel().getTitle(),
                        ad.getCar().getEngineType().name(),
                        ad.getCar().getTransmissionType().name(),
                        photos.toString(),
                        messagesChat,
                };
                csvWriter.writeNext(adData);
            }
            return file;
        }
    }

    private ChatListPayload mapChatListPayload(Ad ad, Chat chat) {
        return ChatListPayload.builder()
                .adId(ad.getAdId())
                .lastMessage(chat.getMessages().isEmpty() ? " " : chat.getMessages().getLast().getMessage())
                .messages(chat.getMessages()
                        .stream().map(message ->
                                MessagePayload.builder()
                                        .chatId(chat.getChatId())
                                        .senderName(message.getSenderName())
                                        .message(message.getMessage())
                                        .fromEmail(message.getFrom().getAccount().getEmail())
                                        .createdAt(message.getCreatedAt())
                                        .messageId(message.getMessageId())
                                        .build())
                        .toList())
                .userEmails(chat.getUsers().stream()
                        .map(u -> u.getAccount().getEmail())
                        .toList())
                .title(chat.getAd().getTitle())
                .build();
    }


    private File exportUserToCsv() throws IOException {
        var users = userRepository.findAll();
        var file = getBackupDir(USER_FILENAME);
        try (CSVWriter csvWriter = new CSVWriter(new FileWriter(file))) {
            var headers = new String[]{"email", "phoneNumber", "password", "firstName", "lastName", "role", "blocked"};
            csvWriter.writeNext(headers);
            for (User user : users) {
                var userData = new String[]{
                        user.getAccount().getEmail(),
                        user.getAccount().getPhoneNumber(),
                        user.getAccount().getPassword(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getAccount().getRole().name(),
                        String.valueOf(user.getAccount().isBlocked())
                };
                csvWriter.writeNext(userData);
            }
            return file;
        }
    }

    private File exportBrandAndModels() throws IOException {
        var brands = brandRepository.findAll();
        var file = getBackupDir(BRAND_FILENAME);
        try (CSVWriter csvWriter = new CSVWriter(new FileWriter(file))) {
            var headers = new String[]{"brandName", "models"};
            csvWriter.writeNext(headers);
            for (Brand brand : brands) {
                var models = new StringBuilder();
                brand.getModel()
                        .forEach(model ->
                                models.append(model.getTitle()).append(";"));
                var brandData = new String[]{
                        brand.getTitle(),
                        models.toString()
                };

                csvWriter.writeNext(brandData);
            }
            return file;
        }
    }

    private File exportColors() throws IOException {
        var colors = colorRepository.findAll();
        var file = getBackupDir(COLOR_FILENAME);
        try (CSVWriter csvWriter = new CSVWriter(new FileWriter(file))) {
            var headers = new String[]{"colorName"};
            csvWriter.writeNext(headers);
            for (Color color : colors) {
                var colorDat = new String[]{
                        color.getTitle()
                };
                csvWriter.writeNext(colorDat);
            }
            return file;
        }
    }

    private void savePhotoCars(String photos, Car car) {
        Arrays.stream(photos.split(";"))
                .forEach(s -> {
                    s = s.substring(AD_PHOTO_DIR.length());
                    s = AD_PHOTO_DIR + "/" + s;
                    var phs = photoRepository.findByFilePath(s);
                    if (phs.isEmpty())
                        photoRepository.save(Photo.builder()
                                .car(car)
                                .filePath(s)
                                .build());
                    else {
                        for (Photo p : phs) {
                            if (p.getCar().getCarId().equals(car.getCarId()) &&
                                p.getFilePath().equals(s))
                                continue;

                            photoRepository.save(Photo.builder()
                                    .car(car)
                                    .filePath(s)
                                    .build());
                        }
                    }
                });

    }


    private void savePrices(String prices, Ad ad) {
        Arrays.stream(prices.split(";"))
                .forEach(p -> {
                    var split = p.split("::");
                    System.out.println(Arrays.toString(split));
                    var price = Double.parseDouble(split[0]);
                    var createdAt = LocalDateTime.parse(split[1]);
                    var prs = priceRepository.findByCreatedAt(createdAt);
                    if (prs.isEmpty())
                        priceRepository.save(Price.builder()
                                .price(price)
                                .createdAt(createdAt)
                                .ad(ad)
                                .build());
                    else {
                        for (Price pr : prs) {
                            if (pr.getPrice().equals(price) &&
                                pr.getCreatedAt().equals(createdAt) &&
                                pr.getAd().getAdId().equals(ad.getAdId()))
                                continue;

                            priceRepository.save(Price.builder()
                                    .price(price)
                                    .createdAt(createdAt)
                                    .ad(ad)
                                    .build());
                        }
                    }
                });
    }

    private List<User> getUsersChats(List<String> emails) {
        return emails.stream()
                .map(s -> accountRepository.findByEmail(s.trim())
                        .orElseThrow()
                        .getUser())
                .toList();
    }

    private void saveMessages(Chat adChat, List<MessagePayload> messages) {
        messages.forEach(m -> {
            var fromUser = userRepository.findByAccountEmail(m.getFromEmail());

            if (fromUser.isEmpty())
                return;

            var senderName = m.getSenderName();
            var createdAt = m.getCreatedAt();
            var message = m.getMessage();
            var op = messageRepository.findByCreatedAt(createdAt);

            if (!op.isEmpty()) {
                for (var mes : op) {
                    if (!mes.getMessage().equals(message) && !mes.getCreatedAt().equals(createdAt)
                        && !senderName.equals(mes.getSenderName()) &&
                        fromUser.get().getAccount().getEmail().equals(m.getFromEmail()))
                        messageRepository.save(Message.builder()
                                .createdAt(createdAt)
                                .message(message)
                                .chat(adChat)
                                .senderName(senderName)
                                .from(fromUser.get())
                                .build());
                }
            } else
                messageRepository.save(Message.builder()
                        .createdAt(createdAt)
                        .message(message)
                        .chat(adChat)
                        .senderName(senderName)
                        .from(fromUser.get())
                        .build());

        });
    }

    private File createZip(List<File> files) throws IOException {
        var dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy-HH-mm");
        var zipFile = getBackupDir(BACKUP_ZIP.formatted(dtf.format(LocalDateTime.now())));

        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(zipFile));
             ZipOutputStream zos = new ZipOutputStream(bos)) {

            for (File file : files) {
                if (file != null && file.exists()) {
                    try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
                        var entry = new ZipEntry(file.getName());
                        zos.putNextEntry(entry);

                        var buffer = new byte[4096];
                        int read;

                        while ((read = bis.read(buffer, 0, buffer.length)) != -1) {
                            zos.write(buffer, 0, read);
                        }

                        zos.closeEntry();
                    }
                }
            }
            return zipFile;
        }
    }

    private void unZip(InputStream zipFile) throws IOException {
        try (ZipInputStream zipInputStream = new ZipInputStream(zipFile)) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                File outFile = new File(entry.getName());

                if (entry.isDirectory()) {
                    outFile.mkdirs();
                } else {
                    File parent = outFile.getParentFile();
                    if (parent != null && !parent.exists()) {
                        parent.mkdirs();
                    }

                    try (FileOutputStream fileOutputStream = new FileOutputStream(Path.of(BACKUP_DIR, outFile.getPath()).toFile())) {
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = zipInputStream.read(buffer)) != -1) {
                            fileOutputStream.write(buffer, 0, bytesRead);
                        }
                    }
                }
                zipInputStream.closeEntry();
            }
        }
    }

    private File getBackupDir(String filename) {
        File dir = new File(BACKUP_DIR);

        if (!dir.exists()) {
            dir.mkdirs();
        }
        return Path.of(dir.getPath(), filename).toFile();
    }

}
