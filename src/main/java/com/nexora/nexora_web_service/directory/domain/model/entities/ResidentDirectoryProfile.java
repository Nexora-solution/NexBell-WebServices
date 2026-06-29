package com.nexora.nexora_web_service.directory.domain.model.entities;

import com.nexora.nexora_web_service.directory.domain.model.valueobjects.ContactChannel;
import com.nexora.nexora_web_service.directory.domain.model.valueobjects.ResidentDocument;
import com.nexora.nexora_web_service.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.*;

@Entity
@Table(name = "resident_directory_profiles")
public class ResidentDirectoryProfile extends AuditableModel {

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "documentNumber", column = @Column(name = "document_number", nullable = false))
    })
    private ResidentDocument document;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "email", column = @Column(name = "contact_email", nullable = false)),
        @AttributeOverride(name = "phone", column = @Column(name = "contact_phone", nullable = false))
    })
    private ContactChannel contact;

    // Resident's profile photo as a base64 data URL (optional, set from the
    // mobile app). TEXT so it fits.
    @Column(name = "photo_url", columnDefinition = "TEXT")
    private String photoUrl;

    public ResidentDirectoryProfile() {}

    public ResidentDirectoryProfile(Long userId, String fullName, ResidentDocument document, ContactChannel contact) {
        this.userId = userId;
        this.fullName = fullName;
        this.document = document;
        this.contact = contact;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public ResidentDocument getDocument() {
        return document;
    }

    public void setDocument(ResidentDocument document) {
        this.document = document;
    }

    public ContactChannel getContact() {
        return contact;
    }

    public void setContact(ContactChannel contact) {
        this.contact = contact;
    }

    public void updateContact(ContactChannel contact) {
        this.contact = contact;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}
