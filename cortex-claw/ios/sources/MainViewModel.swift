import Foundation
import SwiftUI

@MainActor
public class MainViewModel: ObservableObject {
    @Published public var messages: [ChatMessage] = []
    @Published public var isLoading: Bool = false
    @Published public var error: String?
    @Published public var agentStatus: AgentStatus = .idle
    
    private let client: CortexClawClient
    
    public init(client: CortexClawClient) {
        self.client = client
    }
    
    public func sendMessage(_ content: String) async {
        isLoading = true
        error = nil
        
        let userMessage = ChatMessage(
            id: UUID().uuidString,
            content: content,
            isUser: true,
            timestamp: Date()
        )
        messages.append(userMessage)
        
        let responseId = UUID().uuidString
        var responseContent = ""
        
        let streamingMessage = ChatMessage(
            id: responseId,
            content: "",
            isUser: false,
            timestamp: Date(),
            isStreaming: true
        )
        messages.append(streamingMessage)
        
        do {
            let response = try await client.processInput(content)
            responseContent = response
            
            if let index = messages.firstIndex(where: { $0.id == responseId }) {
                messages[index] = ChatMessage(
                    id: responseId,
                    content: responseContent,
                    isUser: false,
                    timestamp: Date(),
                    isStreaming: false
                )
            }
        } catch {
            self.error = error.localizedDescription
        }
        
        isLoading = false
    }
    
    public func clearMessages() {
        messages.removeAll()
    }
    
    public func clearError() {
        error = nil
    }
}

public struct ChatMessage: Identifiable {
    public let id: String
    public let content: String
    public let isUser: Bool
    public let timestamp: Date
    public var isStreaming: Bool = false
    
    public init(
        id: String,
        content: String,
        isUser: Bool,
        timestamp: Date,
        isStreaming: Bool = false
    ) {
        self.id = id
        self.content = content
        self.isUser = isUser
        self.timestamp = timestamp
        self.isStreaming = isStreaming
    }
}

public enum AgentStatus {
    case idle
    case processing
    case error
}
