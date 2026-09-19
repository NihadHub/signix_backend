CREATE TABLE user (
                      id BIGINT AUTO_INCREMENT PRIMARY KEY,
                      email VARCHAR(50) NOT NULL UNIQUE,
                      password VARCHAR(255) NOT NULL,
                      full_name VARCHAR(255) NOT NULL,
                      role VARCHAR(20) NOT NULL,
                      created_at DATETIME NOT NULL
);

CREATE TABLE document (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          title VARCHAR(255) NOT NULL,
                          file_path VARCHAR(255) NOT NULL,
                          signed_file_path VARCHAR(255),
                          status VARCHAR(20) NOT NULL,
                          created_at DATETIME NOT NULL,
                          sent_at DATETIME,
                          signed_at DATETIME,
                          owner_id BIGINT NOT NULL,
                          CONSTRAINT fk_document_owner FOREIGN KEY (owner_id) REFERENCES user(id)
);

CREATE TABLE signing_request (
                                 id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 signer_email VARCHAR(255) NOT NULL,
                                 token VARCHAR(255) NOT NULL UNIQUE,
                                 expiration_date DATETIME NOT NULL,
                                 signature_image_base64 LONGTEXT,
                                 signed_at DATETIME,
                                 document_id BIGINT NOT NULL UNIQUE,
                                 CONSTRAINT fk_signing_request_document FOREIGN KEY (document_id) REFERENCES document(id)
);

CREATE TABLE audit_log (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           action VARCHAR(30) NOT NULL,
                           actor VARCHAR(255) NOT NULL,
                           ip_address VARCHAR(50),
                           timestamp DATETIME NOT NULL,
                           document_id BIGINT NOT NULL,
                           CONSTRAINT fk_audit_log_document FOREIGN KEY (document_id) REFERENCES document(id)
);