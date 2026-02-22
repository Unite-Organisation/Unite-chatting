package com.app.prod.messaging.service;

import com.app.prod.exceptions.exceptions.FailedFileUploadException;
import com.app.prod.messaging.dto.CreateMessageRequest;
import com.app.prod.messaging.dto.MessageResponse;
import com.app.prod.messaging.repository.MessageRepository;
import com.app.prod.messaging.repository.MessageType;
import com.app.prod.storage.AbstractStorage;
import com.app.prod.storage.file.FileService;
import com.app.prod.utils.Validate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.sources.tables.records.AppUserRecord;
import org.jooq.sources.tables.records.MessageRecord;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final Clock clock;
    private final Validate validate;
    private final FileService fileService;

    public MessageResponse createMessage(CreateMessageRequest request, AppUserRecord user) {
        LocalDateTime now = LocalDateTime.now(clock);

        MessageRecord record = new MessageRecord();
        record.setSenderId(user.getId());
        record.setConversationId(request.conversationId());
        record.setSendAt(now);
        record.setContent(request.content());
        record.setMessageType(MessageType.TEXT.name());

        messageRepository.insertOne(record);
        log.info("Created one message by {}, on {}", user.getId(), now);

        return new MessageResponse(
                record.getId(),
                record.getConversationId(),
                record.getSenderId(),
                user.getFirstName() + " " + user.getLastName(),
                record.getContent(),
                record.getSendAt(),
                MessageType.TEXT
        );
    }

    @Transactional
    public MessageResponse createFileMessage(UUID id, MultipartFile file, AppUserRecord user) {
        String filePath = null;

        try {
            filePath = fileService.validateAndSaveFile(file);
            if (filePath == null) {
                throw new RuntimeException("Failed to upload file");
            }
        } catch (Exception e) {
            log.error("Failed to upload file: {}", e.getMessage());
            throw new FailedFileUploadException(e.getMessage());
        }

        LocalDateTime now = LocalDateTime.now(clock);

        MessageRecord record = new MessageRecord();
        record.setSenderId(user.getId());
        record.setConversationId(id);
        record.setSendAt(now);
        record.setContent(filePath);
        record.setMessageType(MessageType.FILE.name());

        messageRepository.insertOne(record);
        log.info("Created one message by {}, on {}", user.getId(), now);

        String tokenizedFilePath = fileService.getFileUrl(filePath);
        return new MessageResponse(
                record.getId(),
                record.getConversationId(),
                record.getSenderId(),
                user.getFirstName() + " " + user.getLastName(),
                tokenizedFilePath,
                record.getSendAt(),
                MessageType.FILE
        );

    }
}
