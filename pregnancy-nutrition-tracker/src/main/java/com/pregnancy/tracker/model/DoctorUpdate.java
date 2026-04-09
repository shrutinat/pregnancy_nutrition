package com.pregnancy.tracker.model;

import java.time.LocalDate;

/**
 * DoctorUpdate entity storing medical notes and updates from the doctor.
 * Allows doctors to modify nutrition targets and flag risk conditions.
 */
public class DoctorUpdate {

    private int id;
    private int userId;
    private LocalDate updateDate;
    private String doctorName;
    private String notes;
    private String riskConditions;
    private String updatedTargets;  // JSON string of modified nutrition targets

    /** Default constructor */
    public DoctorUpdate() {
        this.updateDate = LocalDate.now();
    }

    /** Full constructor */
    public DoctorUpdate(int userId, String doctorName, String notes,
                        String riskConditions) {
        this.userId = userId;
        this.updateDate = LocalDate.now();
        this.doctorName = doctorName;
        this.notes = notes;
        this.riskConditions = riskConditions;
    }

    /**
     * Check if there are any risk conditions flagged.
     * @return true if risk conditions exist
     */
    public boolean hasRiskConditions() {
        return riskConditions != null && !riskConditions.trim().isEmpty();
    }

    // ==================== Getters & Setters ====================

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public LocalDate getUpdateDate() { return updateDate; }
    public void setUpdateDate(LocalDate updateDate) { this.updateDate = updateDate; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getRiskConditions() { return riskConditions; }
    public void setRiskConditions(String riskConditions) { this.riskConditions = riskConditions; }

    public String getUpdatedTargets() { return updatedTargets; }
    public void setUpdatedTargets(String updatedTargets) { this.updatedTargets = updatedTargets; }

    @Override
    public String toString() {
        return "DoctorUpdate{doctor='" + doctorName + "', date=" + updateDate +
               ", risks='" + riskConditions + "'}";
    }
}
