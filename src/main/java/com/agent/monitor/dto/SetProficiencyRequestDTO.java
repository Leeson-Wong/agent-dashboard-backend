package com.agent.monitor.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Set Proficiency Request DTO
 */
@Data
public class SetProficiencyRequestDTO {
    private String toolId;
    private BigDecimal proficiency;
}
