package com.trace.fraud.cases;

import java.time.Instant;

import com.trace.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;  
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(
        name = "investigation_notes",
        indexes = {
                @Index(
                        name = "idx_investigation_note_case",
                        columnList = "fraud_case_id"
                ),
                @Index(
                        name = "idx_investigation_note_author",
                        columnList = "author_id"
                ),
                @Index(
                        name = "idx_investigation_note_created_at",
                        columnList = "created_at"
                )
        }
)
public class InvestigationNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "fraud_case_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_investigation_note_case")
    )
    private FraudCase fraudCase;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "author_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_investigation_note_author")
    )
    private User author;

    @Column(nullable = false, length = 5000)
    private String content;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected InvestigationNote() {
    }

    public InvestigationNote(
            FraudCase fraudCase,
            User author,
            String content
    ) {
        this.fraudCase = fraudCase;
        this.author = author;
        this.content = content;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public FraudCase getFraudCase() {
        return fraudCase;
    }

    public User getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setContent(String content) {
        this.content = content;
    }
}