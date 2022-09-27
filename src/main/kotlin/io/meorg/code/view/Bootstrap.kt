package io.meorg.code.view

@Component
@Profile("!no-bootstrap")
class Bootstrap(
    private val mappingMongoConverter: MappingMongoConverter,
    private val mailTemplateService: MailTemplateService,
    private val feedSchedulerProps: FeedSchedulerProps,
    private val credentialsService: CredentialsService
) : CommandLineRunner {
    override fun run(vararg args: String?) {
        mappingMongoConverter.setMapKeyDotReplacement("#dot#")
        runBlocking {
            logger.info { "Affiliate feed without products scheduler enabled=${feedSchedulerProps.anyFeedWithoutProducts}" }
            logger.info { "Affiliate feed products scheduler enabled=${feedSchedulerProps.anyFeedProducts}" }
            credentialsService.feedDefault()
            mailTemplateService.feedFromResources("mail_templates", rewrite = true)
        }
    }

    companion object {
        private val logger = KotlinLogging.logger { }
    }
}
