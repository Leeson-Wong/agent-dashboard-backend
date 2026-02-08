package com.agent.monitor.service;

import com.agent.monitor.entity.SequenceGenerator;
import com.agent.monitor.mapper.SequenceGeneratorMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Sequence Generator Service
 * Handles atomic sequence number generation using sequence_generator table
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SequenceGeneratorService {

    private final SequenceGeneratorMapper sequenceGeneratorMapper;

    /**
     * Get the next sequence value for a given sequence name
     * This method atomically increments and returns the next value
     *
     * @param sequenceName the sequence name
     * @return the next sequence value
     */
    @Transactional
    public Long getNextValue(String sequenceName) {
        SequenceGenerator seq = sequenceGeneratorMapper.findBySequenceName(sequenceName);
        if (seq == null) {
            // Initialize sequence if it doesn't exist
            seq = new SequenceGenerator();
            seq.setSequenceName(sequenceName);
            seq.setCurrentValue(1L);
            sequenceGeneratorMapper.insert(seq);
            return 1L;
        }

        // Get current value and increment
        Long currentValue = seq.getCurrentValue();
        Long nextValue = currentValue + 1;

        // Update the sequence
        sequenceGeneratorMapper.updateValue(sequenceName, nextValue);

        return currentValue;
    }
}
