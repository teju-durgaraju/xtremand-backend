package com.xtremand.domain.dto;

import lombok.Data;

@Data
public class ReplyRequest {
	
	private Long userId;
    private String messageId;
    private String replyBody;

}
