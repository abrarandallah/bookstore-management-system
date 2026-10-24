# Wazuh SIEM — Centralized Security Monitoring & Threat Detection

*Projet de Fin d'Études — Final-year cybersecurity project*

A centralized Wazuh-based SIEM (Security Information and Event Management) deployment for real-time collection, detection, and investigation of Linux security events, with seven custom detection rules mapped to MITRE ATT&CK.

> **Note on this repository:** the live environment was decommissioned after the final presentation (it had grown to ~70GB of logs/indices on a personal machine and had no further need to stay running). This repository documents the working implementation through its technical architecture, verified rule output, and dashboard evidence captured while it was operational.

---

## Architecture

```
Linux Endpoint (Ubuntu 24.04 LTS, WSL2)
  auth.log / syslog / journald
            │
            ▼
     Wazuh Manager            → decode, analyze, correlate, apply rules
            │
            ▼
  Wazuh Indexer (OpenSearch)   → port 9200, store & index events
            │
            ▼
     Wazuh Dashboard           → port 5601, visualization & investigation
            │
     Wazuh API                → port 55000, programmatic access
```

**Environment:** Wazuh 4.12.0, all-in-one deployment, Ubuntu 24.04 LTS on WSL2 (Windows host), bridge networking at `192.168.1.7`, 5.8GB RAM / 71GB storage / 4GB swap.

**Deployment history:** the environment went through three iterations (VM1 → deleted, VM2 → failed, VM3 → the successful final deployment documented here) — largely due to WSL/systemd quirks and an initial SSL/TLS certificate mismatch between the dashboard and indexer, resolved by disabling TLS verification for the development environment (documented trade-off — not appropriate for production, acceptable for an academic single-user lab).

---

## Custom Detection Rules

All 7 rules were implemented, tested, and confirmed firing via live queries against the Wazuh Indexer (`rule.id:<id>` in Discover). Levels, descriptions and MITRE mappings come from the alerts shown in the screenshots; the `frequency`/`timeframe` values for rules 100004 and 100006 come from the `local_rules.xml` excerpt in the technical architecture report. The original rules file was lost when the environment was decommissioned.

| Rule | Detects | Level | Threshold | MITRE ATT&CK | Verified |
|------|---------|-------|-----------|---------------|----------|
| 100001 | SSH brute force (multiple auth failures) | 10 | built on if_sid 5710, 5760 | T1110.001, T1021.004 | ✅ 3 hits, real failed-login events |
| 100002 | Individual sudo / privileged command | 12 | single event | T1548.003 | ✅ 3 hits, sudo command capture |
| 100003 | Root session opened | 10 | single event | T1548.003 | ✅ 25 hits over test period |
| 100004 | Excessive sudo activity | 12 | 5 commands / 300s, same user | T1548.003 | ✅ confirmed firing on threshold |
| 100005 | Multiple root sessions | 12 | 3 sessions / 180s | T1548.003 | ✅ 3 hits, "potential compromise" |
| 100006 | Network scanning / reconnaissance | 12 | 10 events / 300s, same source IP | T1046, T1595 | ✅ 3 hits, correlated SSH failures |
| 100007 | Potential data leakage / exfiltration | 12 | behavioral (privileged command volume) | T1041, T1560 | ✅ 2 hits, elevated command activity |

Rules build on Wazuh's built-in decoders (sshd, sudo, PAM) rather than custom decoders — an initial custom decoder attempt was abandoned in favor of the built-in, proven decoders for reliability.

**Correlation techniques used:** `same_source_ip` (IP-based correlation), `same_user` (user-based correlation), `if_matched_sid` (rule chaining/composite rules), `frequency` + `timeframe` (time-windowed behavioral thresholds).

---

## Results

- **738+ total alerts** generated during testing
  - High severity (level 12+): 3
  - Medium severity (level 8–11): 214
  - Low severity (level 3–7): 521
- MITRE ATT&CK dashboard (1-week view) shows real attack-technique distribution across the test period: Defense Evasion and Privilege Escalation as the dominant tactics, with Credential Access, Initial Access, Persistence, and Lateral Movement also represented — driven by the sudo/sudo-caching and SSH brute-force test activity.

---

## Screenshots

### MITRE ATT&CK dashboard (1-week view)
![MITRE ATT&CK dashboard](screenshots/MITRE_ATT_CK_of_a_week.jpg)

### Each custom rule firing (Discover, `rule.id:<id>`)
| Rule | Evidence |
|------|----------|
| 100001 SSH brute force | ![100001](screenshots/rule_100001_working.jpg) |
| 100002 Sudo / privilege escalation | ![100002](screenshots/rule_100002_working.jpg) |
| 100003 Root session | ![100003](screenshots/rule_100003_working.jpg) |
| 100004 Excessive sudo | ![100004](screenshots/rule_100004_working_.jpg) |
| 100005 Multiple root sessions | ![100005](screenshots/rule_100005_working_.jpg) |
| 100006 Network scanning | ![100006](screenshots/rule_100006_working.jpg) |
| 100007 Data leakage | ![100007](screenshots/rule_100007_working.jpg) |

### Service health
- `screenshots/wazuh-manager_running.jpg`
- `screenshots/wazuh-indexer_running.jpg`
- `screenshots/wazuh-dashboard_runnning.jpg`
- `screenshots/all_services_are_running.jpg`

### Troubleshooting evidence
- `screenshots/Dashboard_Installation_Failure.jpg`
- `screenshots/Clean_Configuration_File__deleted_lines_caused_SSL_certificate_errors_.jpg`
- `screenshots/network_troubleshooting_methodology.jpg`

---

## Lessons Learned

This project broke and required a full environment rebuild more than once during development — most notably days before the final presentation, which forced a decision to rely on pre-captured screenshots and documentation rather than a live demo. In hindsight, the key process gaps were:

- **No configuration validation before restarts.** A single malformed rule in `local_rules.xml` could prevent the manager from starting, with no early warning.
- **Verification wasn't always real verification.** Some later-stage testing relied on scripts that logged a fixed "success" message rather than actually confirming system state — a mistake that hid failures until they became visible the hard way.
- **No version control on configuration changes**, so a bad edit meant re-deriving the working state from memory instead of rolling back one step.

If rebuilding: validate config (`wazuh-control configcheck`) before every restart, verify every rule with `wazuh-logtest` against a real log line rather than trusting a script's own success message, and commit working configuration to git after every verified change.

---

## Compliance Mapping (ISO 27001)

| Control | Area | Implemented via |
|---------|------|------------------|
| A.12.6.1 | Technical vulnerabilities | Rules 100001, 100006 |
| A.9.2.6 | Access rights management | Rules 100002–100005 |
| A.12.4.1 | Event logging | Full log processing pipeline |
| A.16.1.4 | Security event assessment | Rule correlation engine |
| A.13.1.1 | Network controls | Rule 100006 |
