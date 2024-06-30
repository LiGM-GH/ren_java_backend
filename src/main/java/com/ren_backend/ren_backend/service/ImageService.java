package com.ren_backend.ren_backend.service;

import com.ren_backend.ren_backend.model.ImageEntity;
import com.ren_backend.ren_backend.repository.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ImageService {

    private final ImageRepository imageRepository;

    @Autowired
    public ImageService(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    @Transactional
    public ImageEntity saveImage(byte[] imageData, String status) {
        ImageEntity imageEntity = new ImageEntity();
        imageEntity.setImageData(imageData);
        imageEntity.setStatus(status);
        return imageRepository.save(imageEntity);
    }

    public ImageEntity getImageById(Long id) {
        return imageRepository.findById(id).orElse(null);
    }
}
