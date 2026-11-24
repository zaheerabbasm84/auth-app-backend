package com.gen.auth.dto;

import com.gen.auth.helperutils.ModelDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RolesDto extends ModelDTO {
    private String name;
}
