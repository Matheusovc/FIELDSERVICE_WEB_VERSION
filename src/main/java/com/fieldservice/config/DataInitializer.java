package com.fieldservice.config;

import com.fieldservice.domain.enums.Priority;
import com.fieldservice.domain.enums.TicketStatus;
import com.fieldservice.domain.model.Technician;
import com.fieldservice.domain.model.Ticket;
import com.fieldservice.repository.TechnicianRepository;
import com.fieldservice.repository.TicketRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final TechnicianRepository technicianRepository;
    private final TicketRepository ticketRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(TechnicianRepository technicianRepository,
                           TicketRepository ticketRepository,
                           PasswordEncoder passwordEncoder) {
        this.technicianRepository = technicianRepository;
        this.ticketRepository = ticketRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (technicianRepository.count() > 0) {
            return;
        }

        String rawPassword = "123456";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // 1. Cadastrar Técnicos
        Technician joao = new Technician(
                "João Silva",
                "tecnico@fieldservice.com",
                encodedPassword,
                "Técnico de Campo"
        );

        Technician matheus = new Technician(
                "Matheus Carvalho",
                "matheus@fieldservice.com",
                encodedPassword,
                "Técnico de Campo"
        );

        joao = technicianRepository.save(joao);
        matheus = technicianRepository.save(matheus);

        LocalDateTime now = LocalDateTime.now();

        // 2. Cadastrar os 7 Chamados Mock (atribuídos ao técnico principal João Silva para espelhar o app)
        // 1. #1028 | Servidor indisponível | "Servidor não inicializa após queda de energia." | Empresa XYZ | Brasília - DF | HIGH | ASSIGNED | hoje
        Ticket t1 = new Ticket(
                "#1028",
                "Servidor indisponível",
                "Servidor não inicializa após queda de energia.",
                "Empresa XYZ",
                "Brasília - DF",
                Priority.HIGH,
                TicketStatus.ASSIGNED,
                now.minusHours(2),
                joao
        );

        // 2. #1029 | Falha de conexão de rede | "Estações perdem conexão com a rede local intermitentemente." | Empresa ABC | São Paulo - SP | MEDIUM | ACCEPTED | ontem
        Ticket t2 = new Ticket(
                "#1029",
                "Falha de conexão de rede",
                "Estações perdem conexão com a rede local intermitentemente.",
                "Empresa ABC",
                "São Paulo - SP",
                Priority.MEDIUM,
                TicketStatus.ACCEPTED,
                now.minusDays(1).minusHours(3),
                joao
        );

        // 3. #1030 | Impressora indisponível | "Impressora do setor financeiro não liga." | Empresa Delta | Belo Horizonte - MG | LOW | COMPLETED | 3 dias atrás
        Ticket t3 = new Ticket(
                "#1030",
                "Impressora indisponível",
                "Impressora do setor financeiro não liga.",
                "Empresa Delta",
                "Belo Horizonte - MG",
                Priority.LOW,
                TicketStatus.COMPLETED,
                now.minusDays(3).minusHours(1),
                joao
        );

        // 4. #1031 | Sistema de backup falhando | "Rotina de backup noturno falhou nas últimas três execuções." | Empresa Omega | Curitiba - PR | CRITICAL | OPEN | hoje
        Ticket t4 = new Ticket(
                "#1031",
                "Sistema de backup falhando",
                "Rotina de backup noturno falhou nas últimas três execuções.",
                "Empresa Omega",
                "Curitiba - PR",
                Priority.CRITICAL,
                TicketStatus.OPEN,
                now.minusHours(4),
                joao
        );

        // 5. #1032 | Instalação de novo equipamento | "Instalação e configuração de nova estação de trabalho." | Empresa Prime | Porto Alegre - RS | MEDIUM | IN_PROGRESS | 2 dias atrás
        Ticket t5 = new Ticket(
                "#1032",
                "Instalação de novo equipamento",
                "Instalação e configuração de nova estação de trabalho.",
                "Empresa Prime",
                "Porto Alegre - RS",
                Priority.MEDIUM,
                TicketStatus.IN_PROGRESS,
                now.minusDays(2).minusHours(5),
                joao
        );

        // 6. #1033 | Manutenção preventiva | "Manutenção preventiva trimestral do parque de máquinas." | Empresa Nova | Recife - PE | LOW | COMPLETED | 5 dias atrás
        Ticket t6 = new Ticket(
                "#1033",
                "Manutenção preventiva",
                "Manutenção preventiva trimestral do parque de máquinas.",
                "Empresa Nova",
                "Recife - PE",
                Priority.LOW,
                TicketStatus.COMPLETED,
                now.minusDays(5).minusHours(2),
                joao
        );

        // 7. #1034 | Rede Wi-Fi instável | "Sinal de Wi-Fi cai constantemente no 2º andar." | Empresa Zenith | Brasília - DF | HIGH | TRAVELING | hoje
        Ticket t7 = new Ticket(
                "#1034",
                "Rede Wi-Fi instável",
                "Sinal de Wi-Fi cai constantemente no 2º andar.",
                "Empresa Zenith",
                "Brasília - DF",
                Priority.HIGH,
                TicketStatus.TRAVELING,
                now.minusHours(1),
                joao
        );

        ticketRepository.save(t1);
        ticketRepository.save(t2);
        ticketRepository.save(t3);
        ticketRepository.save(t4);
        ticketRepository.save(t5);
        ticketRepository.save(t6);
        ticketRepository.save(t7);
    }
}
