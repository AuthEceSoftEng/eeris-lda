## Security and Privacy for Smart Metering Systems 
### Techniques clustering per type/domain of application
### Reference
[Best Available Techniques Reference Document for the cyber-security and privacy of the 10 minimum functional
requirements of the Smart Metering Systems](https://ec.europa.eu/energy/sites/ener/files/documents/bat_wp4_bref_smart-metering_systems_final_deliverable.pdf)


| Domain | Application | Techniques |
| :----- | :---------- | :--------- |
| Cryptography | Symmetric Ciphers | AES, DES |
| Cryptography | Asymmetric Ciphers | ECC-Brainpool or NIST Curves, RSA |
| Cryptography | Symmetric Cipher Modes and MACs | CTR, CBC, ECB, GCM, CMAC, CCM, HMAC |
| Cryptography | Key Management Algorithms or Protocols | ECDH, PSK, IKE, MQV, RFC3904 |
| Cryptography | Digital Signature Algorithms | ECDSA |
| Cryptography | Cryptographic Hashes | SHA-1, SHA-2 |
| Security Architecture | Software Maintenance | Firmware Update |
| Security Architecture | Key Management Mechanisms | PKI, CMP, Remote Key Renewal, PSK |
| Security Architecture | Key Provisioning Mechanisms | Initial Key Loading, Manufacturer/Customer Key Exchange |
| Security Architecture | Random Number Generator | Random Number Generator |
| Security Architecture | Misc. Storage | LDAP, Private Cloud |
| Security Architecture | Defence in Depth | Plausibility Check, Switching Commands Validation, Network Segregation, Local Processing |
| Security Architecture | Network Architecture | Application Gateway, Router, Firewall/IPS |
| Hardware Security | Secure Storage | HSM, Encrypted Storage, μP Hardening |
| Hardware Security | Tamper Detect | Magnetic Field Sensor, Tamper Switch, Geometric Low-Relieves, Hot Blade Welding, Embedded RFID Tag, Seal |
| Hardware Security | Secure Operation | Detection of Abnormal Chip Operating Conditions, SPA/DPA Protected Executable |
| Access Control | Physical Protection | Radio, Local Interface, Read Only Interface, Local Storage, Local Display |
| Access Control | Network Defense in Depth | VPN, Firewall/IPS |
| Access Control | Malware Protection | Application Whitelisting |
| Access Control | Authentication Mechanisms | PKI, Client Certificate, One Time Password, Multi Factor, Profile Based, Shared secrets (TACACS+, Kerberos, LDAP, Password, PIN, OpenId) |
| Monitoring | Device Tampering | Tamper Detect Sensor, Tamper Switch, Event Log, Net Frequency |
| Monitoring | Head End System | Audit Trail |
| Monitoring | Analysis and Detection | SIEM, Alarm, Lock-out |
| Transport | Transport Format | XML, CMS, M-Bus, DLMS, SEP, FTP, EDIFACT, SMS |
| Transport | Secure Transport | ZigBee, DLMS, CMS, TLS, IPSEC, SFTP, Broadcast |
| Transport | Transport Medium | Ethernet, M-Bus, OMS4, Radio Mesh, LTE, GSM, GPRS, CDMA, ZigBee, PLC |
| Time Synchronization | Time Synchronization Assurance | Synchronization Period, Network Time Resilience |
| Privacy | Frequency | Transmission, Reading |
| Privacy | Privacy Preservation | Transparency: Local Processing, Aggregation, Privacy by Design, Pseudonymization |
| Privacy | Purpose Limitation | Level of Detail, Transmission Frequency, Reading Frequency |
| Privacy | Compliance to Requirements | Retention |
| Privacy | Legitimacy of Processing Personal Data | Consumer Choice: Opt-In/Opt-Out |
