package com.example.ch06.profile

// Model domain untuk layar Profil
data class UserProfile(
    val username: String,
    val notificationsEnabled: Boolean
)

// Sinkron (tanpa suspend) karena datanya lokal
interface ProfileRepository {
    fun getProfile(): UserProfile
}

// Implementasi palsu untuk latihan
class FakeProfileRepository : ProfileRepository {
    override fun getProfile(): UserProfile = UserProfile(
        username = "Mahasiswa Android",
        notificationsEnabled = true
    )
}
