package hu.progmasters.webshop.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.cloudinary.utils.ObjectUtils;
import hu.progmasters.webshop.domain.Image;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ImageUploadServiceTest {

    @Mock
    private Cloudinary cloudinary;

    @InjectMocks
    private ImageUploadService imageUploadService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void uploadImages_whenImageFilesAreNull_returnsEmptyList() {
        List<MultipartFile> imageFiles = null;

        List<Image> result = imageUploadService.uploadImages(imageFiles);

        assertTrue(result.isEmpty());
    }

    @Test
    void uploadImages_whenImageFilesAreEmpty_returnsEmptyList() {
        List<MultipartFile> imageFiles = new ArrayList<>();

        List<Image> result = imageUploadService.uploadImages(imageFiles);

        assertTrue(result.isEmpty());
    }

    @Test
    void uploadImages_whenImageFileIsEmpty_doesNotUpload() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);
        List<MultipartFile> imageFiles = Collections.singletonList(file);

        List<Image> result = imageUploadService.uploadImages(imageFiles);

        assertTrue(result.isEmpty());
        verify(file, never()).getBytes();
    }

    @Test
    void uploadImages_whenImageFileIsNotEmpty_uploadsAndReturnsImage() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getBytes()).thenReturn(new byte[0]);
        List<MultipartFile> imageFiles = Collections.singletonList(file);

        Uploader uploader = mock(Uploader.class);
        when(cloudinary.uploader()).thenReturn(uploader);

        Map<String, Object> uploadResult = new HashMap<>();
        uploadResult.put("secure_url", "http://test.com/test.jpg");
        when(uploader.upload(any(byte[].class), eq(ObjectUtils.emptyMap()))).thenReturn(uploadResult);

        List<Image> result = imageUploadService.uploadImages(imageFiles);

        assertEquals(1, result.size());
        assertEquals("http://test.com/test.jpg", result.get(0).getUrl());
    }

}