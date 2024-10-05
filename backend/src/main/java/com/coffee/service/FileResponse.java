package com.coffee.service;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FileResponse {
    private String fileName;
    private String fileUrl;
    private String fileType;
    private long size;
}
