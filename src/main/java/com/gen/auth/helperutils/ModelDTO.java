package com.gen.auth.helperutils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ModelDTO {
    private Long id;
    private Long createdAt;
    private Long updatedAt;
    private Long createdBy;
    private Long updatedBy;
}
