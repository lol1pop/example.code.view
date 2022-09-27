package com.switips.us.algopix.response.product.identification

data class Dimensions(
        val width: Length? = null,
        val length: Length? = null,
        val height: Length? = null,
        val weight: Weight? = null
)

data class Length(
        val value: Double? = null,
        val unit: LengthUnit
)

enum class LengthUnit {
    INCH, CM
}

data class Weight(
        val value: Double? = null,
        val unit: WeightUnit
)

enum class WeightUnit {
    OUNCE, POUND
}
