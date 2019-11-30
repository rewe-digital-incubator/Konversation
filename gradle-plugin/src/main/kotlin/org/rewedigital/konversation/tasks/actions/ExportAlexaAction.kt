package org.rewedigital.konversation.tasks.actions

import org.gradle.api.logging.Logger
import org.gradle.workers.WorkAction
import org.rewedigital.konversation.KonversationApi
import org.rewedigital.konversation.KonversationProjectParameters
import org.rewedigital.konversation.createLoggingFacade
import org.rewedigital.konversation.project
import org.slf4j.LoggerFactory
import java.io.File

@Suppress("UnstableApiUsage")
abstract class ExportAlexaAction : WorkAction<KonversationProjectParameters> {
    private val logger = LoggerFactory.getLogger(ExportAlexaAction::class.java) as Logger

    override fun execute() {
        val api = KonversationApi("", "")
        api.inputFiles += project.inputFiles
        api.inputFiles += project.alexa.inputFiles
        api.logger = createLoggingFacade(LoggerFactory.getLogger(UpdateAlexaAction::class.java))
        api.invocationName = project.invocationNames.values.firstOrNull() ?: project.alexa.invocationNames.values.firstOrNull() ?: throw java.lang.IllegalArgumentException("Invationname not found")
        val target = File(project.outputDirectory, api.invocationName + ".json")
        logger.lifecycle("Exporting ${api.invocationName} to ${target.absolutePath}...")
        api.exportAlexaSchema(target, true)
        logger.info("Export finished")
    }
}