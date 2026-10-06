package com.banklab.fdservice.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.banklab.fdservice.entity.FdAccountSequence;
import com.banklab.fdservice.repository.FdAccountSequenceRepository;

import lombok.RequiredArgsConstructor;

/**
 * Generates the per-branch portion of FD account numbers (ERD §3: prefix + branch
 * code + zero-padded sequence). Concurrency safety uses a pessimistic DB row lock
 * (FdAccountSequenceRepository#lockByBranchCd) rather than @Version optimistic
 * locking — FDSQ_LAST_SEQ is a hot counter row under concurrent account creation,
 * and a lock-and-wait strategy avoids the retry storms optimistic locking would
 * cause when many callers target the exact same row.
 */
@Service
@RequiredArgsConstructor
public class AccountNumberGeneratorService {

    private final FdAccountSequenceRepository sequenceRepository;

    @Transactional
    public String generateAccountNumber(String branchCd) {
        FdAccountSequence sequence = sequenceRepository.lockByBranchCd(branchCd)
                .orElseThrow(() -> new AccountSequenceNotConfiguredException(branchCd));

        long nextSeq = sequence.getLastSeq() + 1;
        sequence.setLastSeq(nextSeq);

        return formatAccountNumber(sequence, nextSeq);
    }

    private String formatAccountNumber(FdAccountSequence sequence, long nextSeq) {
        String prefix = sequence.getPrefix() == null ? "" : sequence.getPrefix();
        String paddedSeq = String.format("%0" + sequence.getSeqWidth() + "d", nextSeq);
        return prefix + sequence.getBranchCd() + paddedSeq;
    }
}
