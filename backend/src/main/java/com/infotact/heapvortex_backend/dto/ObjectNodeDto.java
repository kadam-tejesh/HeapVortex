package com.infotact.heapvortex_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ObjectNodeDto {
    private String id;
    private String className;
    private long retainedSize;
    private boolean isGcRoot;
}
