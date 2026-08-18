package com.alya.aplikasilansia.data;

/**
 * BARU: model untuk 1 entri riwayat pengingat yang SUDAH pernah bunyi/trigger.
 * Berbeda dengan Reminder (jadwal yang masih akan datang), ReminderHistory adalah
 * catatan historis -- dibuat otomatis oleh ReminderReceiver setiap kali notifikasi
 * reminder benar-benar muncul ke user.
 */
public class ReminderHistory {
    private String id;
    private String reminderId; // id dari reminder asal (untuk referensi/debug)
    private String userId;
    private String title;
    private String desc;
    private int icon;
    private String triggeredAt; // waktu notifikasi benar-benar muncul, format "yyyy-MM-dd HH:mm:ss"

    public ReminderHistory() {
        // Wajib ada constructor kosong untuk Firestore deserialization otomatis
    }

    public ReminderHistory(String reminderId, String userId, String title, String desc, int icon, String triggeredAt) {
        this.reminderId = reminderId;
        this.userId = userId;
        this.title = title;
        this.desc = desc;
        this.icon = icon;
        this.triggeredAt = triggeredAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getReminderId() {
        return reminderId;
    }

    public void setReminderId(String reminderId) {
        this.reminderId = reminderId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getTriggeredAt() {
        return triggeredAt;
    }

    public void setTriggeredAt(String triggeredAt) {
        this.triggeredAt = triggeredAt;
    }
}