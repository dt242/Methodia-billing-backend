package com.example.billing.service;

import com.example.billing.dto.SelfReportRequest;
import com.example.billing.exception.ResourceNotFoundException;
import com.example.billing.model.Reading;
import com.example.billing.model.ReadingStatus;
import com.example.billing.model.User;
import com.example.billing.repository.ReadingRepository;
import com.example.billing.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReadingService {

    private final ReadingRepository readingRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public ReadingService(ReadingRepository readingRepository, UserRepository userRepository, AuditService auditService) {
        this.readingRepository = readingRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    public List<Reading> getPendingReadings() {
        return readingRepository.findByStatusOrderByDateTimeAsc(ReadingStatus.UNVALIDATED);
    }

    @Transactional
    public void approveReading(String id) {
        Reading reading = readingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Отчетът не е намерен."));

        reading.setStatus(ReadingStatus.VALIDATED);
        readingRepository.save(reading);
        auditService.logAction("Usage Data", "Approved reading with ID: " + id);
    }

    @Transactional
    public void rejectReading(String id) {
        Reading reading = readingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Отчетът не е намерен."));

        reading.setStatus(ReadingStatus.REJECTED);
        readingRepository.save(reading);
        auditService.logAction("Usage Data", "Rejected reading with ID: " + id);
    }

    public List<Reading> getAllReadings(ReadingStatus status) {
        auditService.logAction("Usage Data", "Admin viewed all usage data readings");
        return readingRepository.findWithFilters(status);
    }

    @Transactional
    public void submitSelfReport(SelfReportRequest request, String reference) {
        User user = userRepository.findByReference(reference)
                .orElseThrow(() -> new ResourceNotFoundException("Потребителят не е намерен."));

        Reading reading = new Reading(
                user,
                request.product(),
                request.dateTime(),
                request.lastReading(),
                true,
                ReadingStatus.UNVALIDATED
        );

        readingRepository.save(reading);
        auditService.logAction("Usage Data", "Client submitted a self-report for product: " + request.product());
    }
}