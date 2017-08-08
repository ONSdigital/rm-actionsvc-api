package uk.gov.ons.ctp.response.action.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.ons.ctp.response.action.domain.repository.ActionReportRepository;
import uk.gov.ons.ctp.response.action.service.ActionReportService;

/**
 * Create report via stored procedure
 */
@Service
@Slf4j
public class ActionReportServiceImpl implements ActionReportService {

    @Autowired
    private ActionReportRepository actionReportRepository;

    @Override
    public void createReport() {
        log.debug("Entering createReport...");
        boolean reportResult = actionReportRepository.miStoredProcedure();
        log.debug("Just ran the mi report and result is {}", reportResult);
    }
}
