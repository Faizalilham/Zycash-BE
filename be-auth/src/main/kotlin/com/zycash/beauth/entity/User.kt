package com.zycash.beauth.entity

import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator
import java.io.Serializable
import java.util.Date

@Entity(name = "User")
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(generator = "system-uuid")
    @UuidGenerator
    var id: String? = null,

    var name: String? = null,
    var phoneNumber: String? = null,
    var address: String? = null,
    var email: String? = null,
    var gender: String? = null,
    var username: String? = null,
    var password: String? = null,
    var avatar: String? = null,
    var createdBy: String? = null,
    var createdDate: Date? = null,
    var modifiedBy: String? = null,
    var modifiedDate: Date? = null,
    var isDeleted: Boolean? = null
) : Serializable {
    companion object {
        private const val serialVersionUID = 9178661439383356177L
    }
}
