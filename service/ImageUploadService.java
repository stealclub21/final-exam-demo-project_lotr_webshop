package hu.progmasters.webshop.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import hu.progmasters.webshop.domain.Image;
import hu.progmasters.webshop.exception.CloudinaryException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Transactional
@AllArgsConstructor
public class ImageUploadService {

    private final Cloudinary cloudinary;

    public List<Image> uploadImages(List<MultipartFile> imageFiles)  {

        List<Image> images = new ArrayList<>();
        AtomicReference<Map> uploadResult = new AtomicReference<>();

        if (imageFiles == null) {
            return images;
        }
        imageFiles.forEach(file -> {
            if (file != null && !file.isEmpty()) {
                try {
                    uploadResult.set(cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap()));
                } catch (IOException e) {
                    throw new CloudinaryException("Error uploading file");
                }
                String imageUrl = (String) uploadResult.get().get("secure_url");
                images.add(new Image(imageUrl, file.getOriginalFilename()));
            }
        });

        return images;
    }
}
