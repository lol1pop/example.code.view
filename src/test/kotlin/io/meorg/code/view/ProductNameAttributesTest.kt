package io.meorg.code.view

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest


@SpringBootTest
@DisabledIfEnvironmentVariable(named = "SPACE_PIPELINE", matches = "true")
internal class ProductNameAttributesTest {

	@Autowired
	private val elasticSearchClient: ElasticSearchClient? = null

	@Autowired
	private val objectMapper: ObjectMapper? = null

	@Autowired
	private val service: ProductNameAttributesService? = null

	private val rawFile by lazy { File("src/test/resources/raw.json") }

	@Test
	fun testConfigs() {
		ProductCategoryAttributesConfigs.configs.forEach { config ->
			config.configs.forEach { innerConfig ->
				innerConfig.testCases.forEach {
					Assertions.assertEquals(
						it.expected?.copy(unusedPart = ""),
						innerConfig.process(it.got)?.copy(unusedPart = "")
					)
				}
			}
		}
	}

	@Test
	@Disabled
	fun saveProducts() {
		if (objectMapper == null) throw IllegalStateException("no object mapper")
		runBlocking {
			rawFile.writer().use {
				getProducts(true).collect { product ->
					it.appendLine(objectMapper.writeValueAsString(product))
				}
			}
		}
	}

	@Test
	@Disabled
	fun test() {
		if (objectMapper == null) throw IllegalStateException("no object mapper")
		if (elasticSearchClient == null) throw IllegalStateException("no restClient")
		if (service == null) throw IllegalStateException("no service")

		runBlocking {
			File("src/test/resources/resolved_name.txt").writer().use { writer ->
				val subAttribute = "test"
				val parent = File("src/test/resources/attributes").apply { deleteRecursively(); mkdirs() }
				val configMap = ProductCategoryAttributesConfigs.configs
					.map { config ->
						config.category to config.configs.map {
							Attribute(
								parent,
								config.category,
								it.attribute
							)
						}
					}
					.toMap()
					.toMutableMap()
				val categoryCountMap = mutableMapOf<String, Int>()

				ProductCategoryAttributesConfigs.configs.forEach { config ->
					config.additionalConfigs.forEach { innerConfig ->
						File(parent, "${config.category}/${innerConfig.attribute}").mkdirs()
					}
				}
				getProducts()
					.collect { product ->
						product.categories?.get("1")?.let { category ->
							categoryCountMap[category] = categoryCountMap[category]?.plus(1) ?: 1
						}

						service.process(product, subAttribute).let { processed ->
							processed.categories?.get("1")
								?.let { category ->
									processed.attributes[subAttribute]?.keys?.let { keys ->
										if (!configMap.containsKey(category))
											configMap[category] = ProductCategoryAttributesConfigs.allConfig.configs
												.map { config -> Attribute(parent, category, config.attribute) }

										val notFound =
											configMap[category]?.map { it.attribute }?.filterNot { keys.contains(it) }
												?: throw IllegalStateException("configMap[$category] is null")

										keys.forEach { attribute ->
											configMap[category]
												?.firstOrNull { it.attribute == attribute }
												?.resolved(
													product.name ?: "",
													processed.attributes[subAttribute]?.get(attribute)
												)
										}
										notFound.forEach { attribute ->
											configMap[category]
												?.firstOrNull { it.attribute == attribute }
												?.notResolved(product.name ?: "")
										}
									}
								}
							writer.appendLine(product.name + " | ${product.categories?.get("1")} | " + processed.name)
							writer.flush()
						}
					}
				configMap.forEach { (category, attributeModels) ->
					println("Distribution $category:")
					attributeModels.forEach { model ->
						val percent =
							if (model.totalCounter <= 0) 0
							else (model.resolvedCounter.toDouble() / model.totalCounter * 100).setScale()
						println("\t${model.attribute} - $percent% (resolved=${model.resolvedCounter},not=${model.notResolvedCounter},total=${model.totalCounter})")
					}
					println()
				}
				println("Category count:")
				categoryCountMap
					.toList()
					.sortedByDescending { it.second }
					.forEach { (category, count) -> println("\t$category: $count") }
			}
		}
	}

	private fun getProducts(fromElastic: Boolean = false): Flow<SearchProduct> {
		if (objectMapper == null) throw IllegalStateException("no object mapper")
		if (elasticSearchClient == null) throw IllegalStateException("no restClient")
		return if (fromElastic || !rawFile.exists())
			elasticSearchClient.all<SearchProduct>().consumeAsFlow()
		else flow {
			rawFile.useLines { seq -> seq.forEach { json -> emit(objectMapper.readValue<SearchProduct>(json)) } }
		}
	}

	internal class Attribute(
		parent: File,
		category: String,
		val attribute: String,
	) : AutoCloseable {
		var totalCounter = 0
		var resolvedCounter = 0
		var notResolvedCounter = 0

		private val resolved by lazy {
			File(parent, "$category/$attribute/resolved.txt").apply { parentFile.mkdirs() }.writer()
		}
		private val notResolved by lazy {
			File(
				parent,
				"$category/$attribute/not_resolved.txt"
			).apply { parentFile.mkdirs() }.writer()
		}

		fun resolved(name: String, attribute: Any?) {
			resolved.appendLine("$name | $attribute")
			resolved.flush()
			totalCounter++
			resolvedCounter++
		}

		fun notResolved(name: String) {
			notResolved.appendLine(name)
			notResolved.flush()
			totalCounter++
			notResolvedCounter++
		}

		override fun close() {
			resolved.close()
			notResolved.close()
		}
	}
}

