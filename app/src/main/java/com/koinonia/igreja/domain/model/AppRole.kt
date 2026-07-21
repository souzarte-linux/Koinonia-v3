package com.koinonia.igreja.domain.model

enum class AppRole {
    ADMIN, PASTOR, ANCIAO, DIACONO, TESOUREIRO, VIEWER, NONE;

    val hasFullAccess: Boolean
        get() = this == ADMIN || this == PASTOR || this == ANCIAO || this == DIACONO

    val hasTreasuryAccess: Boolean
        get() = this == ADMIN || this == PASTOR || this == ANCIAO || this == TESOUREIRO
}
