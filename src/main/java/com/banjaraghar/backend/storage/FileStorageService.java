package com.banjaraghar.backend.storage;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    String storeImage(MultipartFile file, String folder);

    void delete(String url);
}
