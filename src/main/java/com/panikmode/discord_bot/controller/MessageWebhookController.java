package com.panikmode.discord_bot.controller;

import com.panikmode.discord_bot.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
public class MessageWebhookController {


    private MessageService messageService;

    public MessageWebhookController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/announce-boosters")
    public ResponseEntity<?> announceBoosters()
    {
        messageService.announceBoosters();
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Request SENT!: "
        ));

    }

}
