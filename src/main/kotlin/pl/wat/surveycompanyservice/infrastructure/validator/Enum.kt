package pl.wat.surveycompanyservice.infrastructure.validator

import javax.validation.Constraint
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [EnumConstraintValidator::class])
annotation class Enum(
    val enumClass: KClass<out kotlin.Enum<*>>,
    val message: String = "Provided argument is not an UUID",
    val groups: Array<KClass<out Any>> = [],
    val payload: Array<KClass<out Any>> = []
) {}

class EnumConstraintValidator(
    private var enumValues: List<String>,
) : ConstraintValidator<Enum, String> {

    override fun initialize(annotation: Enum) {
        enumValues = annotation.enumClass.java.enumConstants.map { it.toString() }
    }

    override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean =
        enumValues.contains(value)
}
