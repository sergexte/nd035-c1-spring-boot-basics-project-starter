package com.udacity.jwdnd.course1.cloudstorage.controller;

import com.udacity.jwdnd.course1.cloudstorage.mapper.UserMapper;
import com.udacity.jwdnd.course1.cloudstorage.model.File;
import com.udacity.jwdnd.course1.cloudstorage.model.FileForm;
import com.udacity.jwdnd.course1.cloudstorage.services.CredentialService;
import com.udacity.jwdnd.course1.cloudstorage.services.FileService;
import com.udacity.jwdnd.course1.cloudstorage.services.NoteService;
import com.udacity.jwdnd.course1.cloudstorage.services.UserService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@Controller
@RequestMapping("/file")
public class FileController {

    private final UserService userService;
    private final UserMapper userMapper;
    private final NoteService noteService;
    private final CredentialService credentialService;
    private final FileService fileService;

    public FileController(
            UserMapper userMapper,
            UserService userService,
            FileService fileService,
            NoteService noteService,
            CredentialService credentialService) {
        this.userService = userService;
        this.fileService = fileService;
        this.userMapper = userMapper;
        this.noteService = noteService;
        this.credentialService = credentialService;
    }

    @PostMapping("/upload")
    public String uploadFile(
            @ModelAttribute("newFile") FileForm newFile,
            Authentication authentication,
            Model model
    ) throws IOException {

        boolean isSuccess;
        String username = authentication.getName();
        int userid = userMapper.getUser(username).getUserId();

        MultipartFile fileUpload = newFile.getFile();

        if (fileUpload.isEmpty()) {
            model.addAttribute("errorMessage", "File is empty!");
        } else {
            if (!fileService.fileExists(fileUpload)) {
                isSuccess = fileService.createFile(fileUpload, userService.getLoggedInUserId(authentication)) > 0;
                if (isSuccess) {
                    model.addAttribute("successMessage", "File saved");
                } else {
                    model.addAttribute("errorMessage", "Error creating file");
                }
            } else {
                model.addAttribute("errorMessage", "File already exists");
            }
        }

        model.addAttribute("tab", "nav-files-tab");
        model.addAttribute("files", fileService.getAllFiles(userid));
        model.addAttribute("notes", noteService.getNotesByUser(userid));
        model.addAttribute("credentials", credentialService.getUserCredentials(userid));
        return "redirect:/home";
    }

    @GetMapping(value = "/view")
    public ResponseEntity<InputStreamResource> viewFile(
            @RequestParam("id") int fileId) {

        File file = fileService.getFile(fileId);

        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(file.getFiledata()));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment:fileName=" + file.getFilename())
                .contentType(MediaType.parseMediaType(file.getContentType()))
                .body(resource);
    }

    @GetMapping("/delete")
    public String deleteFile(@RequestParam("id") int fileId, RedirectAttributes redirectAttributes) {
        boolean isSuccess = fileId > 0;
        if (isSuccess) {
            fileService.deleteFile(fileId);
        }
        redirectAttributes.addFlashAttribute("tag", "nav-files-tab");
        redirectAttributes.addFlashAttribute("success", true);

        return "redirect:/home";
    }
}