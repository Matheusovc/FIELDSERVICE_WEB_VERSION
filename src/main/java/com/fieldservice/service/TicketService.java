package com.fieldservice.service;

import com.fieldservice.domain.enums.Priority;
import com.fieldservice.domain.enums.TicketFilter;
import com.fieldservice.domain.enums.TicketStatus;
import com.fieldservice.domain.model.Technician;
import com.fieldservice.domain.model.Ticket;
import com.fieldservice.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class TicketService {

    private final TicketRepository ticketRepository;

    public TicketService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public List<Ticket> findAllByTechnician(Technician technician) {
        return ticketRepository.findByTechnicianOrderByCreatedAtDesc(technician);
    }

    public List<Ticket> findByFilter(Technician technician, TicketFilter filter) {
        if (filter == null || filter == TicketFilter.ALL) {
            return ticketRepository.findByTechnicianOrderByCreatedAtDesc(technician);
        }
        return ticketRepository.findByTechnicianAndStatusInOrderByCreatedAtDesc(technician, filter.getStatuses());
    }

    public Optional<Ticket> findByIdAndTechnician(Long id, Technician technician) {
        if (id == null || technician == null) return Optional.empty();
        return ticketRepository.findById(id)
                .filter(t -> t.getTechnician() != null && t.getTechnician().getId().equals(technician.getId()));
    }

    public Optional<Ticket> findById(Long id) {
        if (id == null) return Optional.empty();
        return ticketRepository.findById(id);
    }

    /**
     * RN006: Destacar chamados HIGH e CRITICAL ainda não concluídos, ordenados do maior para o menor
     * e limitados aos 3 mais relevantes.
     */
    public List<Ticket> findPriorityTickets(Technician technician) {
        List<Priority> priorities = List.of(Priority.CRITICAL, Priority.HIGH);
        List<TicketStatus> terminalStatuses = List.of(TicketStatus.COMPLETED, TicketStatus.CANCELLED);
        
        List<Ticket> priorityList = ticketRepository.findPriorityTickets(technician, priorities, terminalStatuses);
        return priorityList.stream().limit(3).collect(Collectors.toList());
    }

    /**
     * Contagens para os cards de resumo do dia (RF03 / RN005) e chips de filtros (RF05).
     */
    public Map<TicketFilter, Long> getFilterCounts(Technician technician) {
        long allCount = ticketRepository.countByTechnician(technician);
        long pendingCount = ticketRepository.countByTechnicianAndStatusIn(technician, TicketFilter.PENDING.getStatuses());
        long inProgressCount = ticketRepository.countByTechnicianAndStatusIn(technician, TicketFilter.IN_PROGRESS.getStatuses());
        long completedCount = ticketRepository.countByTechnicianAndStatusIn(technician, TicketFilter.COMPLETED.getStatuses());

        return Map.of(
            TicketFilter.ALL, allCount,
            TicketFilter.PENDING, pendingCount,
            TicketFilter.IN_PROGRESS, inProgressCount,
            TicketFilter.COMPLETED, completedCount
        );
    }

    /**
     * RN007 / RN008 / RN009: Avança o atendimento para a próxima etapa na máquina de estados.
     */
    @Transactional
    public Ticket advanceStatus(Long ticketId, Technician technician) {
        Ticket ticket = findByIdAndTechnician(ticketId, technician)
                .orElseThrow(() -> new IllegalArgumentException("Chamado não encontrado ou não pertence ao técnico"));

        if (ticket.getStatus().isTerminal()) {
            throw new IllegalStateException("Chamados concluídos ou cancelados não podem ser alterados.");
        }

        TicketStatus currentStatus = ticket.getStatus();
        TicketStatus nextStatus = currentStatus.getNextStatus()
                .orElseThrow(() -> new IllegalStateException("Não há próximo status definido para " + currentStatus.getDescription()));

        ticket.setStatus(nextStatus);
        return ticketRepository.save(ticket);
    }

    /**
     * RN008: Aceitar chamado especificamente (de ASSIGNED para ACCEPTED).
     */
    @Transactional
    public Ticket acceptTicket(Long ticketId, Technician technician) {
        Ticket ticket = findByIdAndTechnician(ticketId, technician)
                .orElseThrow(() -> new IllegalArgumentException("Chamado não encontrado ou não pertence ao técnico"));

        if (ticket.getStatus() != TicketStatus.ASSIGNED) {
            throw new IllegalStateException("Apenas chamados atribuídos podem ser aceitos.");
        }

        ticket.setStatus(TicketStatus.ACCEPTED);
        return ticketRepository.save(ticket);
    }

    @Transactional
    public Ticket save(Ticket ticket) {
        return ticketRepository.save(ticket);
    }

    /**
     * Gera o próximo número de chamado no formato "#NNNN", tomando como base
     * o maior número já existente (independente do técnico). Ex.: se o maior
     * for #1034, o próximo será #1035.
     */
    public String generateNextNumber() {
        int max = ticketRepository.findAll().stream()
                .map(Ticket::getNumber)
                .filter(Objects::nonNull)
                .map(n -> n.replaceAll("\\D", ""))
                .filter(s -> !s.isEmpty())
                .mapToInt(Integer::parseInt)
                .max()
                .orElse(1027);
        return "#" + (max + 1);
    }

    /**
     * Cadastro manual de chamado (RF novo). O chamado é criado já atribuído ao
     * técnico logado, com status ASSIGNED (pronto para ser aceito no fluxo RN007)
     * e data/hora atuais.
     */
    @Transactional
    public Ticket createManualTicket(Technician technician, String title, String description,
                                     String customerName, String address, Priority priority) {
        Ticket ticket = new Ticket(
                generateNextNumber(),
                title.trim(),
                description.trim(),
                customerName.trim(),
                address.trim(),
                priority,
                TicketStatus.ASSIGNED,
                LocalDateTime.now(),
                technician
        );
        return ticketRepository.save(ticket);
    }
}
