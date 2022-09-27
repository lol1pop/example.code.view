package io.meorg.code.view.repository.mongo


import org.springframework.data.mongodb.repository.Query
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface ProductMongo : ReactiveMongoRepository<Product, String> {
    fun findTop1ByBarCodeIn(barCode: Set<Long>): Mono<Product>

    @Query(value = "{\$and:[$mpnQuery,$andBrandQuery]}")
    fun findByMpnAndBrandAdmin(mpn: String, brand: String): Flux<Product>
    fun findByMpnAndBrand(mpn: String, brand: String): Flux<Product>

    @Query(value = "{\$and:[$modelQuery,$andBrandQuery]}")
    fun findByModelAndBrandAdmin(model: String, brand: String): Mono<Product>
    fun findByModelAndBrand(model: String, brand: String): Mono<Product>

    @Query(value = "{\$or:[$mpnQuery,$andModelQuery]}")
    fun findByMpnOrModelAdmin(mpn: String, model: String): Mono<Product>
    fun findByMpnOrModel(mpn: String, model: String): Mono<Product>

    @Query(value = "{\$and:[$nameQuery,$andBrandQuery]}")
    fun findAllByNameAndBrandAdmin(name: String, brand: String): Flux<Product>
    fun findAllByNameAndBrand(name: String, brand: String): Flux<Product>
    fun findAllByOriginalNameAndBrand(originName: String, brand: String): Flux<Product>

    @Query(value = nameQuery)
    fun findAllByNameAdmin(name: String): Flux<Product>
    fun findAllByName(name: String): Flux<Product>

    @Query(value = "{\$or:[{categories: null},{categories:{}}]}")
    fun findAllWithNoCategories(): Flux<Product>

    @Query(value = ecoProductQuery)
    fun findAllByEcoProduct(ecoProduct: Boolean): Flux<Product>

    fun countByEcoProduct(ecoProduct: Boolean): Mono<Long>

    companion object {
        private const val andBrandQuery =
            "{\$or:[{\$and:[{'adminBrand':{\$exists:true}},{'adminBrand':?1}]},{\$and:[{'adminBrand':{\$exists:false}},{'brand':?1}]}]}]}"
        private const val mpnQuery =
            "{\$or:[{\$and:[{'adminMpn':{\$exists:true}},{'adminMpn':?0}]},{\$and:[{'adminMpn':{\$exists:false}},{'mpn':?0}]}]}]}"
        private const val modelQuery =
            "{\$or:[{\$and:[{'adminModel':{\$exists:true}},{'adminModel':?0}]},{\$and:[{'adminModel':{\$exists:false}},{'model':?0}]}]}]}"
        private const val andModelQuery =
            "{\$or:[{\$and:[{'adminModel':{\$exists:true}},{'adminModel':?1}]},{\$and:[{'adminModel':{\$exists:false}},{'model':?1}]}]}]}"
        private const val ecoProductQuery =
            "{\$or:[{\$and:[{'adminEcoProduct':{\$exists:true}},{'adminEcoProduct':?0}]},{\$and:[{'adminEcoProduct':{\$exists:false}},{'ecoProduct':?0}]}]}]}"
        private const val nameQuery =
            "{\$or:[{\$and:[{'adminName':{\$exists:true}},{'adminName':?0}]},{\$and:[{'adminName':{\$exists:false}},{'name':?0}]}]}]}"
    }
}
