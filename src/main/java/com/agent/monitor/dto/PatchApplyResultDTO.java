package com.agent.monitor.dto;

import lombok.Data;

/**
 * Patch Apply Result DTO
 */
@Data
public class PatchApplyResultDTO {
    private Boolean success;
    private String message;
    private Integer appliedCount;
    private Integer failedCount;

    public static PatchApplyResultDTO success(String message, int appliedCount, int failedCount) {
        PatchApplyResultDTO result = new PatchApplyResultDTO();
        result.setSuccess(true);
        result.setMessage(message);
        result.setAppliedCount(appliedCount);
        result.setFailedCount(failedCount);
        return result;
    }

    public static PatchApplyResultDTO failure(String message) {
        PatchApplyResultDTO result = new PatchApplyResultDTO();
        result.setSuccess(false);
        result.setMessage(message);
        result.setAppliedCount(0);
        result.setFailedCount(0);
        return result;
    }
}
