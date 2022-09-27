package io.meorg.code.view.models.product

import org.springframework.stereotype.Component

@Component
class RawProductFiltersKeyRelations {

    private val filtersByKeyRelationsCommands = listOf(
            ::filterByBarCode,
            ::filterByBrandMpn,
            ::filterByBranModel,
            ::filterByMerchantSku
    )

    fun filterProduct(options: FilterProductOptions): Boolean {
        filtersByKeyRelationsCommands.forEach { command ->
            if (command(options)) {
                return true
            }
        }
        return false
    }

    private fun filterByBarCode(options: FilterProductOptions): Boolean = !options.barCode.isNullOrBlank()

    private fun filterByBrandMpn(options: FilterProductOptions): Boolean = !options.brand.isNullOrBlank() && !options.mpn.isNullOrBlank()

    private fun filterByBranModel(options: FilterProductOptions): Boolean = !options.brand.isNullOrBlank() && !options.model.isNullOrBlank()

    private fun filterByMerchantSku(options: FilterProductOptions): Boolean = !options.merchant.isNullOrBlank() && !options.sku.isNullOrBlank()

}

data class FilterProductOptions(
        val barCode: String? = null,
        val brand: String? = null,
        val mpn: String? = null,
        val model: String? = null,
        val sku: String? = null,
        val merchant: String? = null
)
