package com.hackerman.filehandler.service;

import com.hackerman.filehandler.exception.FileStorageException;
import com.hackerman.filehandler.exception.MyFileNotFoundException;
import com.hackerman.filehandler.property.FileStorageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

/**
 * Этот сервис реализует логику сохранения файлов на сервер и выдачу их клиенту
 */
@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    @Autowired
    public FileStorageService(FileStorageProperties fileStorageProperties) {
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
                .toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Не могу создать папку, в которую ты хочешь сохранить файл.", ex);
        }
    }

    // Метод который сохраняет файл
    public String storeFile(MultipartFile file) {
        // Приводим в порядок имя файла
        String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

        try {
            // Проверяем на непотребство в пути сохранения файла
            if(fileName.contains("..")) {
                throw new FileStorageException("Неверно указан путь до файла: " + fileName);
            }

            // Копируем файл в указанное место (Файл с одинаковым названием перезапишется)
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        } catch (IOException ex) {
            throw new FileStorageException("Не могу сохранить файл " + fileName + ". Попробуй еще разок)", ex);
        }
    }

    // Метод, который удаляет файл
    public void deleteFile(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            File file = new File(String.valueOf(filePath));
            Resource resource = new UrlResource(filePath.toUri());
            if (file.delete()) {
                System.out.println(file.getName() + " удалён!");
            } else {
                throw new FileStorageException("Не выпилить файл " + fileName + ". Попробуй еще разок)");
            }
        } catch (IOException ex) {
            throw new FileStorageException("Не выпилить файл " + fileName + ". Попробуй еще разок)", ex);
        }
    }

    // Метод который упаковывает файл как ресурс и отдает клиенту
    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new MyFileNotFoundException("Такого файла нет(( " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new MyFileNotFoundException("Такого файла нет(( " + fileName, ex);
        }
    }
}
