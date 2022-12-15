package com.udacity.jwdnd.course1.cloudstorage.controller;

import com.udacity.jwdnd.course1.cloudstorage.mapper.UserMapper;
import com.udacity.jwdnd.course1.cloudstorage.model.CredentialForm;
import com.udacity.jwdnd.course1.cloudstorage.model.FileForm;
import com.udacity.jwdnd.course1.cloudstorage.model.NoteForm;
import com.udacity.jwdnd.course1.cloudstorage.model.User;
import com.udacity.jwdnd.course1.cloudstorage.services.CredentialService;
import com.udacity.jwdnd.course1.cloudstorage.services.FileService;
import com.udacity.jwdnd.course1.cloudstorage.services.NoteService;
import com.udacity.jwdnd.course1.cloudstorage.services.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/credentials")
public class CredentialsController {

    private final CredentialService credentialService;
    private final NoteService noteService;
    private final FileService fileService;
    private final UserService userService;
    private final UserMapper userMapper;

    public CredentialsController(
            CredentialService credentialService,
            NoteService noteService,
            FileService fileService,
            UserService userService,
            UserMapper userMapper) {
        this.credentialService = credentialService;
        this.noteService = noteService;
        this.fileService = fileService;
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @PostMapping("/save")
    public String addNewCredential(
            Authentication authentication,
            @ModelAttribute("newFile") FileForm newFile,
            @ModelAttribute("credentialForm") CredentialForm credentialForm,
            @ModelAttribute("newNote") NoteForm newNote,
            Model model) {

        String url = credentialForm.getUrl();
        String username = credentialForm.getUsername();
        String password = credentialForm.getPassword();

        String userName = authentication.getName();
        int userId = userMapper.getUser(userName).getUserId();

        if (credentialForm.getId() != null && !credentialForm.getId().isBlank()) {
            credentialService.updateCredential(credentialForm.getId(), username, url, password);
        } else {
            credentialService.createCredential(url, username, password, userId);
        }

        model.addAttribute("success", true);
        model.addAttribute("tab", "nav-credentials-tab");
        model.addAttribute("credentials", credentialService.getUserCredentials(userId));
        model.addAttribute("notes", noteService.getNotesByUser(userId));
        model.addAttribute("files", fileService.getAllFiles(userId));
        return "home";
    }

    @GetMapping("/deleteCredential/{credentialId}")
    public String deleteCredential(
            Authentication authentication,
            @PathVariable Integer credentialId,
            @ModelAttribute("newFile") FileForm newFile,
            @ModelAttribute("newNote") NoteForm newNote,
            @ModelAttribute("credentialForm") CredentialForm credentialForm,
            Model model) {

        credentialService.deleteCredential(credentialId);
        String userName = authentication.getName();
        User user = userService.getUser(userName);
        int userId = user.getUserId();

        model.addAttribute("credentials", credentialService.getUserCredentials(userId));
        model.addAttribute("tab", "nav-credentials-tab");
        model.addAttribute("result", "success");
        return "home";
    }
}