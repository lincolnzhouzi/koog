package ai.koog.cortexclaw.android

import ai.koog.cortexclaw.core.agent.AgentStatus
import ai.koog.cortexclaw.core.agent.AgentResponse
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

public class MainViewModel : ViewModel() {
    
    private val _agentStatus = MutableStateFlow<AgentStatus>(AgentStatus.Idle)
    public val agentStatus: StateFlow<AgentStatus> = _agentStatus.asStateFlow()
    
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    public val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    public val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    public val error: StateFlow<String?> = _error.asStateFlow()

    public fun sendMessage(content: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            val now = Clock.System.now().toEpochMilliseconds()
            _messages.value = _messages.value + ChatMessage(
                id = now.toString(),
                content = content,
                isUser = true,
                timestamp = now
            )
            
            try {
                val application = CortexClawApplication.getInstance()
                val agent = application.agent
                
                if (agent == null) {
                    _error.value = "Agent not initialized"
                    return@launch
                }
                
                val responseBuilder = StringBuilder()
                val responseId = (Clock.System.now().toEpochMilliseconds() + 1).toString()
                
                _messages.value = _messages.value + ChatMessage(
                    id = responseId,
                    content = "",
                    isUser = false,
                    timestamp = Clock.System.now().toEpochMilliseconds(),
                    isStreaming = true
                )
                
                agent.processInput(content).collect { response ->
                    when (response) {
                        is AgentResponse.Text -> {
                            responseBuilder.append(response.content)
                            updateMessage(responseId, responseBuilder.toString())
                        }
                        is AgentResponse.Streaming -> {
                            responseBuilder.append(response.chunk)
                            updateMessage(responseId, responseBuilder.toString())
                        }
                        is AgentResponse.Error -> {
                            _error.value = response.message
                        }
                        else -> {}
                    }
                }
                
                updateMessage(responseId, responseBuilder.toString(), isStreaming = false)
                _agentStatus.value = agent.getStatus()
                
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun updateMessage(id: String, content: String, isStreaming: Boolean = false) {
        _messages.value = _messages.value.map { message ->
            if (message.id == id) {
                message.copy(content = content, isStreaming = isStreaming)
            } else {
                message
            }
        }
    }

    public fun clearMessages() {
        _messages.value = emptyList()
    }

    public fun clearError() {
        _error.value = null
    }
}

public data class ChatMessage(
    val id: String,
    val content: String,
    val isUser: Boolean,
    val timestamp: Long,
    val isStreaming: Boolean = false
)
