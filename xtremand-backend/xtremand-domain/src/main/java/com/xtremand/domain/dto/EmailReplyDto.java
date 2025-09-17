package com.xtremand.domain.dto;

import java.util.List;

import lombok.Data;

@Data
public class EmailReplyDto {
    private Long id;
    private String subject;
    private String body;
    private String fromEmail;
    private String toEmail;
    private String messageId;
    private String inReplyTo;
    private boolean isIncoming;
    private List<EmailReplyDto> replies;

}



