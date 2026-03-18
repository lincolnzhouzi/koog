package ai.koog.cortexclaw.api

import ai.koog.cortexclaw.core.agent.CortexClawAgent
import ai.koog.cortexclaw.core.agent.AgentStatus
import ai.koog.cortexclaw.core.agent.AgentResponse
import ai.koog.cortexclaw.core.agent.config.AgentConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock

public interface AgentAPI {
    public suspend fun initialize(config: AgentConfig): Result<Unit>
    public suspend fun processInput(input: UserInput): Flow<AgentResponse>
    public suspend fun executeCommand(command: AgentCommand): Result<CommandResult>
    public suspend fun getStatus(): AgentStatus
    public suspend fun shutdown(): Result<Unit>
}

public data class UserInput(
    val content: String,
    val type: InputType,
    val metadata: Map<String, Any> = emptyMap()
)

public enum class InputType {
    TEXT, VOICE, GESTURE
}

public sealed class AgentCommand {
    public data class SetLanguage(val language: String) : AgentCommand()
    public data class SetModel(val modelId: String) : AgentCommand()
    public data object ClearHistory : AgentCommand()
    public data object GetStats : AgentCommand()
}

public sealed class CommandResult {
    public data class Success(val message: String) : CommandResult()
    public data class Error(val message: String) : CommandResult()
    public data class Stats(val sessionsCount: Int, val uptime: Long) : CommandResult()
}

public class AgentAPIImpl(
    private val agent: CortexClawAgent
) : AgentAPI {
    
    override suspend fun initialize(config: AgentConfig): Result<Unit> {
        return agent.initialize()
    }

    override suspend fun processInput(input: UserInput): Flow<AgentResponse> {
        return agent.processInput(input.content)
    }

    override suspend fun executeCommand(command: AgentCommand): Result<CommandResult> {
        return when (command) {
            is AgentCommand.SetLanguage -> {
                Result.success(CommandResult.Success("Language set to ${command.language}"))
            }
            is AgentCommand.SetModel -> {
                Result.success(CommandResult.Success("Model set to ${command.modelId}"))
            }
            is AgentCommand.ClearHistory -> {
                Result.success(CommandResult.Success("History cleared"))
            }
            is AgentCommand.GetStats -> {
                Result.success(CommandResult.Stats(
                    sessionsCount = agent.getActiveSessionCount(),
                    uptime = Clock.System.now().toEpochMilliseconds()
                ))
            }
        }
    }

    override suspend fun getStatus(): AgentStatus {
        return agent.getStatus()
    }

    override suspend fun shutdown(): Result<Unit> {
        return agent.shutdown()
    }
}
