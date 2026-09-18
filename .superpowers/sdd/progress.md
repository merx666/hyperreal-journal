# Progress Ledger

Branch: `feature/stability-improvements`

## Project 1: Opioids Expansion & Dose Calculators (Completed)
- [x] Task 1: Use Case `DoseConverter` i testy jednostkowe (TDD) (commit 02bbb6f, review clean)
- [x] Task 2: Rozszerzenie bazy `substances.json` o gałąź opioidów (commit 311149d, review clean)
- [x] Task 3: Rozszerzenie `InteractionChecker` o bezpośrednie mapowanie nowych opioidów (commit c8a4175, review clean)
- [x] Task 4: UI Kalkulatora Dawek w `EnterDoseScreen` i `AddIngestionViewModel` (commit dc57531, review clean)
- [x] Task 5: Zapisanie artefaktu z promptem Grok-bota i weryfikacja całości (commit 07da3af, review clean)

## Project 2: Tolerance & Reset Engine (Completed)
Plan: `docs/superpowers/plans/2026-09-18-tolerance-reset-engine.md`
- [x] Task 1: Domain Models for Receptor Groups and Tolerance Status (commit 3211373, review clean)
- [x] Task 2: ToleranceCalculator Use Case with Unit Tests (TDD) (commit 8c95372, review clean)
- [x] Task 3: DataStore Persistence for Manual Date Overrides (commit feea5fe, review clean)
- [x] Task 4: Insights Screen Receptor Recovery Dashboard (commit 9a424d7, review clean)
- [x] Task 5: EnterDoseScreen Harm Reduction Contextual Alert (commit 904607a, review clean)
- [x] Task 6: End-to-End Verification & Full Regression Suite (75/75 unit tests green, assembleDebug verified)

## Project 3: Live Session Check-ins & Shulgin Rating (Completed)
Plan: `docs/superpowers/plans/2026-09-18-live-session-checkins-shulgin.md`
- [x] Task 1: Domain Models and Repository Interface (commit fa87c4d, review clean)
- [x] Task 2: Room Entity, DAO, Migration 1 -> 2, and Mapper (commit 9e1ed81, review clean)
- [x] Task 3: CheckInRepository Implementation & DI Wiring (commit 40d8e83, review clean)
- [x] Task 4: JournalViewModel Integration & Unit Tests (commit bce544d, review clean)
- [x] Task 5: UI: CheckInBottomSheet & Timeline in JournalScreen (commit d153339, review clean)
- [x] Task 6: End-to-End Verification & Full Regression (80/80 unit tests green, assembleDebug verified)
