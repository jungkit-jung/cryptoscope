# CryptoScope — Project Memory & Architecture Context

## Overview
CryptoScope is an Android application built with **Jetpack Compose**, **Kotlin Coroutines/Flow**, and **Room Database**. It features Smart Money wallet discovery, 7-Layer Anti-Trap defense against malicious smart contracts/honeypots, live interactive candlestick charts, paper trading, and an **Agentic AI Autopilot** engine.

## Core Milestones (October 17, 2026)
1. **Fresh Start on Clean Install**:
   - Dummy trade seeding (`PaperTradingSimulator.getInitialTrades()`) removed from `CryptoScopeApp.kt`.
   - Starts with virtual $10,000 cash, 0 open trades, and 0% P&L.
   - Includes "Reset Portfolio" button and empty state prompts.
2. **Custom LLM Integration**:
   - Universal OpenAI-compatible API support (`LlmConfig.kt` & `CustomLlmClient.kt`).
   - Supports Ollama LAN (`http://192.168.x.x:11434/v1`), Termux local, Groq Cloud, OpenRouter, and OpenAI.
   - `android:usesCleartextTraffic="true"` configured in `AndroidManifest.xml`.
   - Live AI Analyst integrated into `TradeChartDetailScreen.kt`.
3. **Agentic AI Autopilot (1-Click Autonomous Loop)**:
   - Implemented in `AgenticEngine.kt` and `AiCopilotScreen.kt`.
   - Autonomous Cycle:
     1. **Perception**: Scans on-chain smart wallet candidate holdings.
     2. **7-Layer Anti-Trap Audit**: Automatically screens tokens; intercepts honeypots (e.g. 95% sell tax, mint exploit) and logs them to `trap_logs`.
     3. **Reasoning**: Queries Custom LLM for technical evaluation, risk allocation, Take-Profit (+15-25%), and Stop-Loss (-5%).
     4. **Action Execution**: Directly executes new paper trades into Room database (`copyTradeDao`).
     5. **Debrief**: Generates executive mission report with live stepper progress.
4. **CI/CD & Builds**:
   - Managed via GitHub Actions (`.github/workflows/build-apk.yml`).
   - Repository: `https://github.com/jungkit-jung/cryptoscope.git` (branch `main`).
   - Download location: `/storage/emulated/0/Download/CryptoScope-v1.2-agentic.apk` and `CryptoScope.apk`.
