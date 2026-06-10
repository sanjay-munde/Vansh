package com.vansh.familytree.domain.validation

import com.vansh.familytree.data.entity.Member
import com.vansh.familytree.data.entity.RelationshipType

class TimeParadoxValidator {

    fun validateMemberDates(birthDate: Long?, deathDate: Long?): ValidationResult {
        if (birthDate != null && deathDate != null && deathDate < birthDate) {
            return ValidationResult.Invalid("Death date cannot be before birth date.")
        }
        return ValidationResult.Valid
    }

    fun validateRelationshipDates(
        subject: Member,
        target: Member,
        type: RelationshipType
    ): ValidationResult {
        when (type) {
            RelationshipType.PARENT -> {
                // Subject is parent of Target
                if (subject.dateOfBirth != null && target.dateOfBirth != null) {
                    if (target.dateOfBirth <= subject.dateOfBirth) {
                        return ValidationResult.Invalid("A parent cannot be born after or on the same day as their child.")
                    }
                    // A reasonable biological constraint (e.g., 10 years min age diff) can be added here if needed,
                    // but for adoptive/step parents it might not apply. So we keep it strict to just born before.
                }
            }
            RelationshipType.CHILD -> {
                // Subject is child of Target
                if (subject.dateOfBirth != null && target.dateOfBirth != null) {
                    if (subject.dateOfBirth <= target.dateOfBirth) {
                        return ValidationResult.Invalid("A child cannot be born before or on the same day as their parent.")
                    }
                }
            }
            RelationshipType.SPOUSE -> {
                // No strict age-based paradox for spouses, maybe check if both were alive at some overlapping point?
            }
        }
        return ValidationResult.Valid
    }
}
