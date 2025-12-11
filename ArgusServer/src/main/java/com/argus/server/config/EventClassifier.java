package com.argus.server.config;

import com.argus.server.model.Event;

public class EventClassifier {

	// parâmetros ajustáveis
	private static final long PAUSE_THRESHOLD_MS = 120_000L; // 120s
	private static final long FOCUS_RED_MS = 5_000L; // 5s

	/**
	 * Classifica evento como "green", "yellow" ou "red". Usa combinação de
	 * type/action/time e campos auxiliares do Event.
	 */
	public static String classify(Event e) {
		if (e == null)
			return "green";

		String type = safe(e.getType()).toLowerCase();
		String action = safe(e.getAction()).toLowerCase();

		// 1) Eventos de foco
		if ("focus".equals(type)) {
			if ("focus_lost".equals(action)) {
				// Se o cliente enviou duração do foco perdido (campo time_diff) > FOCUS_RED_MS
				if (e.getTime() > 0 && e.getTimestamp() > 0) {
					// time -> cliente timestamp; timestamp -> servidor recepção
					long elapsed = e.getTimestamp() - e.getTime();
					if (elapsed >= FOCUS_RED_MS)
						return "red";
				}
				// default: red (perda de foco é sempre um sinal forte)
				return "red";
			}
			if ("focus_gained".equals(action))
				return "green";
		}

		// 2) ALT e combinações
		if ("keyboard".equals(type)) {
			if ("alt".equals(action))
				return "yellow"; // alt sozinho -> suspeita
			if (action.contains("alt+tab") || action.contains("alt_tab") || action.contains("alttab"))
				return "red";
			if ("ctrl+c".equals(action) || "ctrl+v".equals(action) || action.contains("copy")
					|| action.contains("paste")) {
				// copy/paste: suspicious; multiple occurrences -> escalate (precisa contar no
				// redis)
				return "yellow";
			}
			// TAB by itself is normal
			if ("tab".equals(action) || "enter".equals(action) || action.length() == 1)
				return "green";
		}

		// 3) Mouse clicks: outside area heuristics could be applied later
		if ("mouse".equals(type)) {
			// click coords present -> green by default
			return "green";
		}

		// 4) Pausa (cliente envia um evento 'pause' com duration)
		if ("pause".equals(type)) {
			// assume action contains duration in ms (optional), else use event.time
			// difference
			try {
				long pauseMs = Long.parseLong(action.replaceAll("\\D", ""));
				if (pauseMs >= PAUSE_THRESHOLD_MS)
					return "yellow";
			} catch (Exception ignore) {
			}
			return "yellow";
		}

		// 5) ArgusVision
		if ("vision".equals(type)) {
			// Ação: Face perdida ou muito tempo sem face (alto risco)
			if ("face_lost".equals(action) || "no_face_long".equals(action))
				return "red";

			// Ação: Olhando para fora da tela (risco médio)
			if (action.contains("looking_"))
				return "yellow";

			// Ação: Múltiplas faces ou faces desconhecidas (risco alto)
			if ("multiple_faces".equals(action) || "unknown_person".equals(action))
				return "red";

			// Ação: Movimento corporal excessivo/fora da tela
			if ("motion_detected".equals(action))
				return "yellow";

			// Ação: Rosto detectado e olhando para a tela (baixo risco)
			if ("face_ok".equals(action) || "face_detected".equals(action))
				return "green";
		}

		// 6) fallback
		return "green";
	}

	private static String safe(String s) {
		return s == null ? "" : s;
	}
}
