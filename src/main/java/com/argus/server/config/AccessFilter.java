package com.argus.server.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// Controle de acesso simples, em duas camadas independentes e DESLIGADAS por padrão
// (propriedade vazia = camada desligada, comportamento idêntico ao de antes):
//
//  1) Chave de cliente (argus.security.client-key): exigida no header X-Argus-Key nas rotas
//     que o plugin e o ArgusVision chamam (/api/event, /api/session/start,
//     /api/session/active, /ws-command). Barra quem não tem o plugin instalado.
//
//  2) Login do professor (argus.security.professor-user/-password, HTTP Basic): exigido em
//     TUDO o resto — dashboards, /api/command, /api/session/end|exam e o /ws do dashboard.
//     É o que impede um aluno na mesma rede de encerrar a prova da turma ou assistir
//     a webcam dos colegas.
//
// Limite conhecido: a chave de cliente fica na máquina do aluno (config do plugin), então
// não impede um aluno decidido de forjar eventos; ela não dá acesso às rotas do professor.
@Component
public class AccessFilter extends OncePerRequestFilter {

    @Value("${argus.security.client-key:}")
    private String clientKey;

    @Value("${argus.security.professor-user:}")
    private String professorUser;

    @Value("${argus.security.professor-password:}")
    private String professorPassword;

    @PostConstruct
    void logStatus() {
        System.out.println("[SEGURANCA] chave de cliente: " + (clientKey.isBlank() ? "DESLIGADA" : "ligada")
                + " | login do professor: " + (professorProtected() ? "ligado" : "DESLIGADO"));
        if (clientKey.isBlank() || !professorProtected()) {
            System.out.println("[SEGURANCA] aviso: sem as duas camadas ligadas, qualquer máquina que alcance este "
                    + "servidor pode usar essas rotas. Use apenas em rede controlada.");
        }
    }

    private boolean professorProtected() {
        return !professorUser.isBlank() && !professorPassword.isBlank();
    }

    private static boolean isClientRoute(String method, String path) {
        if (path.startsWith("/ws-command/")) return true;
        if ("POST".equals(method) && (path.equals("/api/event") || path.equals("/api/session/start"))) return true;
        return "GET".equals(method) && (path.equals("/api/session/active") || path.startsWith("/api/session/active/"));
    }

    private static boolean same(String a, String b) {
        if (a == null || b == null) return false;
        return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }

    private boolean basicOk(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.regionMatches(true, 0, "Basic ", 0, 6)) return false;
        try {
            String decoded = new String(Base64.getDecoder().decode(header.substring(6).trim()), StandardCharsets.UTF_8);
            int colon = decoded.indexOf(':');
            if (colon < 0) return false;
            // sem "&&": avalia os dois para não vazar, pelo tempo, qual dos dois errou
            boolean userOk = same(decoded.substring(0, colon), professorUser);
            boolean passOk = same(decoded.substring(colon + 1), professorPassword);
            return userOk & passOk;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        if (isClientRoute(request.getMethod(), path)) {
            if (!clientKey.isBlank() && !same(request.getHeader("X-Argus-Key"), clientKey)) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Chave de cliente ausente ou inválida");
                return;
            }
        } else if (professorProtected() && !basicOk(request)) {
            response.setHeader("WWW-Authenticate", "Basic realm=\"Argus\", charset=\"UTF-8\"");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Login do professor necessário");
            return;
        }

        chain.doFilter(request, response);
    }
}
