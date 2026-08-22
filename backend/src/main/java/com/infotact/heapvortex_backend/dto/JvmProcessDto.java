package com.infotact.heapvortex_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JvmProcessDto {
    private String pid;
    private String displayName;
    private String mainClass;
}