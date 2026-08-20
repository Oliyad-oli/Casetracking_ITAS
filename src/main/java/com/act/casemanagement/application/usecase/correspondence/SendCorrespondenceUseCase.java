package com.act.casemanagement.application.usecase.correspondence;

import com.act.casemanagement.application.context.RequestActorContext;
import com.act.casemanagement.application.port.CaseRepositoryPort;
import com.act.casemanagement.application.port.EventPublisherPort;
import com.act.casemanagement.application.port.OutboxPort;
import com.act.casemanagement.domain.aggregate.Case;
import com.act.casemanagement.domain.aggregate.OutboxEntry;
import com.act.casemanagement.domain.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

/**
 * Sends correspondence (EMAIL / SMS / LETTER / INTERNAL_MEMO) via the outbox.
 * The outbox dispatcher delivers to the notification engine — never inline.
 */
@Service
@RequiredArgsConstructor
public class SendCorrespondenceUseCase {

    private final CaseRepositoryPort caseRepository;
    private final OutboxPort         outboxPort;
    private final EventPublisherPort eventPublisher;

    @Transactional
    public String execute(Command cmd) {
        RequestActorContext actor = RequestActorContext.current();

        Case aCase = caseRepository.findById(cmd.caseId())
                .orElseThrow(() -> new ResourceNotFoundException("Case", cmd.caseId()));

        String refNo = "LTR-REV-" + java.time.Year.now().getValue()
                + "-" + String.format("%05d", (int)(Math.random() * 90000 + 10000));

        // Enqueue outbox entry — never call notification engine inline
        OutboxEntry entry = OutboxEntry.create(
                UUID.randomUUID(),
                "CORRESPONDENCE_SEND",
                Map.of(
                        "caseId",           cmd.caseId().toString(),
                        "caseNumber",       aCase.getCaseNumber(),
                        "referenceNo",      refNo,
                        "recipientName",    cmd.recipientName(),
                        "channel",          cmd.channel(),
                        "subject",          cmd.subject(),
                        "content",          cmd.content(),
                        "sentByActorId",    actor.getActorId().toString()
                ),
                cmd.caseId(),
                "Case",
                5
        );
        outboxPort.save(entry);

        // Add an activity note on the case for the audit trail
        aCase.addNote(
                UUID.randomUUID(),
                "[CORRESPONDENCE SENT] Channel: " + cmd.channel()
                        + " | Recipient: " + cmd.recipientName()
                        + " | Ref: " + refNo
                        + " | Subject: " + cmd.subject(),
                java.time.LocalDate.now(),
                actor.getActorId(),
                cmd.senderName(),
                actor.getRole(),
                null
        );

        Case saved = caseRepository.save(aCase);
        saved.pullEvents().forEach(eventPublisher::publish);
        return refNo;
    }

    public record Command(
            UUID caseId,
            String recipientName,
            String recipientAddress,
            String channel,
            String subject,
            String content,
            String senderName,
            String templateId
    ) {}
}
